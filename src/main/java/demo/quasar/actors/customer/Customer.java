package demo.quasar.actors.customer;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.Order;
import demo.quasar.actors.common.CoffeeType;
import demo.quasar.actors.customer.message.Messages;
import demo.quasar.actors.customer.message.TakeCoffee;

import java.util.Random;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Customer extends BasicActor {

    private final ActorRef baristaRef;

    public Customer(ActorRef baristaRef, String name) {
        super(name);
        this.baristaRef = baristaRef;
    }


    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive();

            if(m instanceof TakeCoffee) {
                leaveShop((TakeCoffee) m);
            } else if(m == Messages.PlaceOrder) {
                makeAnOrder();
            } else {
                System.out.println("Unknown message!");
            }
        }
    }

    private void makeAnOrder() throws SuspendExecution {
        int i = new Random().nextInt(5)+1;
        CoffeeType coffeeType = CoffeeType.getCoffee(i);

        this.baristaRef.send(new Order(coffeeType, self().getName()));

    }

    private void leaveShop(TakeCoffee takeCoffee) {
        System.out.println(String.format("%s: Thank you! bye!", self().getName()));

    }
}
