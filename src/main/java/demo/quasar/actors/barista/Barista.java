package demo.quasar.actors.barista;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.CoffeeReady;
import demo.quasar.actors.barista.message.NewCustomer;
import demo.quasar.actors.barista.message.Order;
import demo.quasar.actors.customer.message.Messages;
import demo.quasar.actors.customer.message.TakeCoffee;
import demo.quasar.actors.entrance.Entrance;
import demo.quasar.actors.machine.CoffeeMachine;
import demo.quasar.actors.machine.message.MakeCoffee;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Barista extends BasicActor {
    private final Map<String, ActorRef> pendingOrders;
    private final Map<String, Object> nameToWatchId;
//    private final LinkedList<Order> orderQueue;

//    private boolean machineFree = true;
//    private Order currentOrder;
    private ActorRef entranceRef;
    private ActorRef coffeeMachine;

    public Barista() {
        super("Barista");
        this.pendingOrders = new HashMap<>();
        this.nameToWatchId = new HashMap<>();
//        orderQueue = new LinkedList<>();
        init();
    }

    private void init() {
        entranceRef = new Entrance(self()).spawn();
        coffeeMachine = new CoffeeMachine(self()).spawn();
        Object id1 = watch(entranceRef);
        Object id2 = watch(coffeeMachine);
        nameToWatchId.put(entranceRef.getName(), id1);
        nameToWatchId.put(coffeeMachine.getName(), id2);
    }

    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive();

            if(m instanceof CoffeeReady){
                deliverCoffee((CoffeeReady) m);
            } else if(m instanceof NewCustomer) {
                greetNewCustomer((NewCustomer) m);
            } else if(m instanceof Order) {
                processOrder((Order) m);
            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }
    }

    private void processOrder(Order order) throws SuspendExecution {
        this.coffeeMachine.send(new MakeCoffee(order.getCoffeeType(), order.getCustomerName()));
    }

//    private void makeCoffee(Order order) throws SuspendExecution {
//        this.currentOrder = order;
//        machineFree = false;
//
//
//    }

    private void greetNewCustomer(NewCustomer newCustomer) throws SuspendExecution {
        System.out.println(String.format("Hello %s! what would you like to order?", newCustomer.getCustomerRef().getName()));

        //watch new customer
        Object id = watch(newCustomer.getCustomerRef());
        this.nameToWatchId.put( newCustomer.getCustomerRef().getName(), id);

        //register for queue
        this.pendingOrders.put(newCustomer.getCustomerRef().getName(), newCustomer.getCustomerRef());

        newCustomer.getCustomerRef().send(Messages.PlaceOrder);
    }

    private void deliverCoffee(CoffeeReady coffeReady) throws SuspendExecution {
        ActorRef customerRef = this.pendingOrders.remove(coffeReady.getCustomerName());
        customerRef.send(new TakeCoffee(coffeReady.getCoffeeType()));

        //unwatch customer
        Object id = this.nameToWatchId.remove(customerRef.getName());
        unwatch(customerRef, id);
    }

}
