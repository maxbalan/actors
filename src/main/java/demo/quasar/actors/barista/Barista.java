package demo.quasar.actors.barista;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.actors.ExitMessage;
import co.paralleluniverse.actors.LifecycleMessage;
import co.paralleluniverse.common.util.Exceptions;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import demo.quasar.actors.barista.message.CoffeeReady;
import demo.quasar.actors.barista.message.NewCustomer;
import demo.quasar.actors.barista.message.Order;
import demo.quasar.actors.barista.message.Setup;
import demo.quasar.actors.customer.message.Messages;
import demo.quasar.actors.customer.message.TakeCoffee;
import demo.quasar.actors.entrance.Entrance;
import demo.quasar.actors.machine.CoffeeMachine;
import demo.quasar.actors.machine.CoffeeMachineResources;
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
    private final Map<String, Object> nameToWatchId;
    private final Map<String, NewCustomer> pendingCustomers;
    private final LinkedList<MakeCoffee> placedOrders;
    private ActorRef entranceRef;
    private ActorRef coffeeMachine;
    private final CoffeeMachineResources coffeeMachineResources;
    private boolean isCoffeeMachineReady = false;

    private NewCustomer currentCustomer;

    public Barista() throws SuspendExecution {
        super("Barista");
        this.pendingOrders = new HashMap<>();
        this.nameToWatchId = new HashMap<>();
        this.pendingCustomers = new HashMap<>();
        this.placedOrders = new LinkedList<>();
        coffeeMachineResources = new CoffeeMachineResources(10, 15);
    }

    private void setupBarista() {
        System.out.println("setup Barista");
        entranceRef = new Entrance(self()).spawn();
        coffeeMachine = new CoffeeMachine(self(), this.coffeeMachineResources).spawn();
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
            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }
    }

    private void processOrder(Order order) throws SuspendExecution {
        System.out.println("Barista accepted order from customer: " + order.getCustomerName());
        MakeCoffee coffee = new MakeCoffee(order.getCoffeeType(), order.getCustomerName());
        this.coffeeMachine.send(coffee);
        this.placedOrders.add(coffee);
    }

    private void greetNewCustomer(NewCustomer newCustomer) throws SuspendExecution {
        if(isCoffeeMachineReady) {
            System.out.println(String.format("Hello %s! what would you like to order?", newCustomer.getCustomerRef().getName()));

            //watch new customer
            Object id = watch(newCustomer.getCustomerRef());
            this.nameToWatchId.put(newCustomer.getCustomerRef().getName(), id);

            //register for queue
            this.pendingOrders.put(newCustomer.getCustomerRef().getName(), newCustomer.getCustomerRef());

            newCustomer.getCustomerRef().send(Messages.PlaceOrder);
        } else {
            System.out.println(String.format("Hello %s! please wait a minute we got an issue!", newCustomer.getCustomerRef().getName()));
            this.pendingCustomers.put(newCustomer.getCustomerRef().getName(), newCustomer);
        }
    }

    private void deliverCoffee(CoffeeReady coffeReady) throws SuspendExecution {
        ActorRef customerRef = this.pendingOrders.remove(coffeReady.getCustomerName());
        System.out.println(String.format("Coffee is to be delivered for %s", customerRef.getName()));
        this.coffeeMachineResources.updateCapsules(1);
//        System.out.println(String.format("Actor address is %s", customerRef));

        //unwatch customer
        Object id = this.nameToWatchId.remove(customerRef.getName());
        unwatch(customerRef, id);

        customerRef.send(new TakeCoffee(coffeReady.getCoffeeType()));
        if(!this.placedOrders.isEmpty())
            this.placedOrders.removeFirst();
        this.pendingCustomers.remove(customerRef.getName());


    }

    @Override
    protected Object handleLifecycleMessage(LifecycleMessage m) {
        System.out.println("ERROR: "+m);

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
                        restartCoffeeMachine();
                    } else if (runtimeException.getCause() instanceof NoMoreCoffeeException) {

                    }
                }
            }
        }

        return super.handleLifecycleMessage(m);
    }

    @Suspendable
    private void restartCoffeeMachine() {
        System.out.println("Capsules = "+coffeeMachineResources.getCapsules());
        coffeeMachine = new CoffeeMachine(self(), this.coffeeMachineResources).spawn();
        Object id2 = watch(coffeeMachine);
        nameToWatchId.put(coffeeMachine.getName(), id2);
        isCoffeeMachineReady = true;

        while (!placedOrders.isEmpty()) {
            try {
                coffeeMachine.send(placedOrders.removeFirst());
            } catch (SuspendExecution suspendExecution) {
                Exceptions.rethrow(suspendExecution);
            }
        }


        for(Map.Entry<String, NewCustomer> entry: pendingCustomers.entrySet()) {
            try {
                self().send(entry.getValue());
            } catch (SuspendExecution suspendExecution) {
                Exceptions.rethrow(suspendExecution);
            }
        }

        pendingCustomers.clear();

        System.out.println("Coffee machine was successfully restarted!");
    }
}
