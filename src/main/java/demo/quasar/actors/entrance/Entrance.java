package demo.quasar.actors.entrance;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.NewCustomer;
import demo.quasar.actors.customer.Customer;

import java.util.concurrent.TimeUnit;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Entrance extends BasicActor {

    private int customerCounter = 0;

    private final ActorRef baristaRef;

    public Entrance(ActorRef baristaRef) {
        super("Entrance");
        this.baristaRef = baristaRef;
    }

    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive(3, TimeUnit.SECONDS);
            if(m == null) {
                System.out.println("New customer has entered Customer_"+customerCounter);
                ActorRef customerRef = new Customer(baristaRef, "Customer_" + customerCounter).spawn();
                this.baristaRef.send(new NewCustomer(customerRef));
//                customerRef.send(Messages.PlaceOrder);
                customerCounter++;
            } else {
                System.out.println(String.format("%s: received unknown message", self().getName()));
            }
        }
    }
}
