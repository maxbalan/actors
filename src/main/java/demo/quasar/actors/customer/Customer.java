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
    private boolean isOrderPlaced;

    public Customer(ActorRef baristaRef, String name) {
        super(name);
        this.baristaRef = baristaRef;
        this.isOrderPlaced = false;
    }


    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive();

            if(m instanceof TakeCoffee) {
                leaveShop((TakeCoffee) m);
                break;
            } else if(m == Messages.PlaceOrder) {
                makeAnOrder();
            } else if (m == Messages.OrderCancelled) {
                cancelledOrder();
                break;
            } else {
                System.out.println("Unknown message!");
            }
        }

        return null;
    }

    private void cancelledOrder() {
        if(this.isOrderPlaced) {
            System.out.println(String.format("[%s]: fu*k this coffee shop", self().getName()));
        } else {
            System.out.println(String.format("[%s]: ok will come tomorrow again", self().getName()));
        }
    }

    private void makeAnOrder() throws SuspendExecution {
        int i = new Random().nextInt(5)+1;
        CoffeeType coffeeType = CoffeeType.getCoffee(i);

        this.baristaRef.send(new Order(coffeeType, self().getName()));
        this.isOrderPlaced = true;
        System.out.println(String.format("[%s]: placed an order for %s", self().getName(), coffeeType.getValue()));
    }
    private void leaveShop(TakeCoffee takeCoffee) {
        System.out.println(String.format("[%s]: Thank you! bye!", self().getName()));
    }
}
