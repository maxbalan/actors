package demo.quasar.actors.barista;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.actors.ExitMessage;
import co.paralleluniverse.actors.LifecycleMessage;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.*;
import demo.quasar.actors.customer.message.Messages;
import demo.quasar.actors.customer.message.TakeCoffee;
import demo.quasar.actors.entrance.Entrance;
import demo.quasar.actors.entrance.message.EntranceMessages;
import demo.quasar.actors.machine.CoffeeMachine;
import demo.quasar.actors.machine.common.NoMoreCoffeeException;
import demo.quasar.actors.machine.common.RestartException;
import demo.quasar.actors.machine.message.MakeCoffee;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Barista extends BasicActor {
    private final Map<String, ActorRef> pendingOrders;
    private final Map<Object, ActorRef> watchedCustomers;
    private final Map<String, Object> nameToWatchId;
    private final Map<String, NewCustomer> pendingCustomers;
    private final LinkedList<MakeCoffee> placedOrders;
    private ActorRef entranceRef;
    private ActorRef coffeeMachine;
    private boolean isCoffeeMachineReady;
    private int capsuleCounter;

    public Barista() throws SuspendExecution {
        super("Barista");
        this.isCoffeeMachineReady = false;
        this.capsuleCounter = 10;
        this.pendingOrders = new HashMap<>();
        this.nameToWatchId = new HashMap<>();
        this.pendingCustomers = new HashMap<>();
        this.watchedCustomers = new HashMap<>();
        this.placedOrders = new LinkedList<>();
    }

    private void setupBarista() {
        System.out.println("setup Barista");
        entranceRef = new Entrance(self()).spawn();
        coffeeMachine = new CoffeeMachine(self(), this.capsuleCounter).spawnThread();
        Object id1 = watch(entranceRef);
        Object id2 = watch(coffeeMachine);
        nameToWatchId.put(entranceRef.getName(), id1);
        nameToWatchId.put(coffeeMachine.getName(), id2);
        isCoffeeMachineReady= true;
    }

    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        setupBarista();
        for(;;) {
            Object m = receive();


            if(m instanceof CoffeeReady){
                deliverCoffee((CoffeeReady) m);
            } else if(m instanceof NewCustomer) {
                greetNewCustomer((NewCustomer) m);
            } else if(m instanceof Order) {
                processOrder((Order) m);
            } else if (m instanceof Setup) {
                setupBarista();
            } else if (m == BaristaMessages.RestartCoffeeMachine) {
                restartCoffeeMachine();
            } else if (m == BaristaMessages.StopApplication) {
                stopApplication();
                break;
            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }

        return null;
    }

    private void stopApplication() throws SuspendExecution {
        System.out.println("[Barista]: will shutdown the application");
        this.entranceRef.send(EntranceMessages.CloseShop);
    }

    private void processOrder(Order order) throws SuspendExecution {
        System.out.println("[Barista]: accepted order from customer: " + order.getCustomerName());
        MakeCoffee coffee = new MakeCoffee(order.getCoffeeType(), order.getCustomerName());
        this.coffeeMachine.send(coffee);
        this.placedOrders.add(coffee);
    }

    private void greetNewCustomer(NewCustomer newCustomer) throws SuspendExecution {
        //watch new customer
        Object id = watch(newCustomer.getCustomerRef());
        this.watchedCustomers.put(id, newCustomer.getCustomerRef());
        this.nameToWatchId.put(newCustomer.getCustomerRef().getName(), id);

        if(isCoffeeMachineReady) {
            System.out.println(String.format("[Barista]: Hello %s! what would you like to order?", newCustomer.getCustomerRef().getName()));

            //register for queue
            this.pendingOrders.put(newCustomer.getCustomerRef().getName(), newCustomer.getCustomerRef());

            newCustomer.getCustomerRef().send(Messages.PlaceOrder);
        } else {
            System.out.println(String.format("[Barista]: Hello %s! please wait a minute we got an issue!", newCustomer.getCustomerRef().getName()));
            this.pendingCustomers.put(newCustomer.getCustomerRef().getName(), newCustomer);
        }
    }

    private void deliverCoffee(CoffeeReady coffeReady) throws SuspendExecution {
        ActorRef customerRef = this.pendingOrders.remove(coffeReady.getCustomerName());
        if (customerRef != null) {
            System.out.println(String.format("[Barista]: Coffee is to be delivered for %s", customerRef.getName()));
            this.capsuleCounter --;
//        System.out.println(String.format("Actor address is %s", customerRef));

            //unwatch customer
            Object id = this.nameToWatchId.remove(customerRef.getName());
            unwatch(customerRef, id);

            customerRef.send(new TakeCoffee(coffeReady.getCoffeeType()));
            if (!this.placedOrders.isEmpty())
                this.placedOrders.removeFirst();
            this.pendingCustomers.remove(customerRef.getName());
        } else {
            System.out.println("[Barista]: Customer left, throw away the order");
        }

    }

    @Override
    protected Object handleLifecycleMessage(LifecycleMessage m) {
        System.out.println("[Barista]: ERROR: "+m);

        if(m instanceof ExitMessage){
            ExitMessage exitMessage = (ExitMessage) m;

            if(exitMessage.getCause() != null && exitMessage.getWatch() != null) {
                RuntimeException runtimeException = (RuntimeException) exitMessage.getCause();

                String watchedChiled = null;
                for(Map.Entry<String, Object> entry: this.nameToWatchId.entrySet()){
                    if(entry.getValue().equals(exitMessage.getWatch())) {
                        watchedChiled = entry.getKey();
                        break;
                    }
                }
                Object watchID = this.nameToWatchId.remove(watchedChiled);
                if(runtimeException.getCause() != null && watchID != null) {
                    if(runtimeException.getCause() instanceof RestartException) {
                        isCoffeeMachineReady = false;
                        RestartException restartException = (RestartException) runtimeException.getCause();
                        System.out.println(restartException.getMessage());

                        return BaristaMessages.RestartCoffeeMachine;
//                        self().send(BaristaMessages.RestartCoffeeMachine);
                    } else if (runtimeException.getCause() instanceof NoMoreCoffeeException) {
                        return BaristaMessages.StopApplication;
                    }
                }
            }

            if(exitMessage.getWatch() != null) {
                if(this.watchedCustomers.containsKey(exitMessage.getWatch())) {
                    ActorRef ref = this.watchedCustomers.remove(exitMessage.getWatch());
                    this.pendingOrders.remove(ref.getName());
                    this.pendingCustomers.remove(ref.getName());
                    this.nameToWatchId.remove(ref.getName());
                }

            }

        }

        return super.handleLifecycleMessage(m);
    }

    private void restartCoffeeMachine() throws SuspendExecution {
        System.out.println("[Barista]: Capsules = "+this.capsuleCounter);
        coffeeMachine = new CoffeeMachine(self(), this.capsuleCounter).spawnThread();
        Object id2 = watch(coffeeMachine);
        nameToWatchId.put(coffeeMachine.getName(), id2);
        isCoffeeMachineReady = true;

        while (!placedOrders.isEmpty()) {
            coffeeMachine.send(placedOrders.removeFirst());
        }

        for(Map.Entry<String, NewCustomer> entry: pendingCustomers.entrySet()) {
            self().send(entry.getValue());
        }

        pendingCustomers.clear();

        System.out.println("[Barista]: Coffee machine was successfully restarted! +++++++++++++++++++++++++++++++++++++++++++++");
    }
}
