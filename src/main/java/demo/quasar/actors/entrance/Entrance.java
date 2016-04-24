package demo.quasar.actors.entrance;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.actors.ExitMessage;
import co.paralleluniverse.actors.LifecycleMessage;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.NewCustomer;
import demo.quasar.actors.customer.Customer;
import demo.quasar.actors.customer.message.Messages;
import demo.quasar.actors.entrance.message.EntranceMessages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Entrance extends BasicActor {

    private int customerCounter = 0;

    private final ActorRef baristaRef;
    private final Map<Object, ActorRef> watchedCustomers;

    public Entrance(ActorRef baristaRef) {
        super("Entrance");
        this.baristaRef = baristaRef;
        this.watchedCustomers = new HashMap<>();
    }

    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive(1, TimeUnit.SECONDS);
            if(m == null) {
                spawnCustomer();
            } else if (m == EntranceMessages.CloseShop) {
                closeShop();
                break;
            } else {
                System.out.println("[Entrance]: received unknown message");
            }
        }

        return null;
    }

    private void spawnCustomer() throws SuspendExecution {
        System.out.println("[Entrance]: New customer has entered Customer_" + customerCounter);
        ActorRef customerRef = new Customer(baristaRef, "Customer_" + customerCounter).spawn();
        Object id = watch(customerRef);
        this.watchedCustomers.put(id, customerRef);
        this.baristaRef.send(new NewCustomer(customerRef));
        customerCounter++;
    }

    private void closeShop() throws SuspendExecution {
        for (Map.Entry<Object, ActorRef> entry : this.watchedCustomers.entrySet())
            entry.getValue().send(Messages.OrderCancelled);
    }

    @Override
    protected Object handleLifecycleMessage(LifecycleMessage m) {
        if (m instanceof ExitMessage) {
            ExitMessage exitMessage = (ExitMessage) m;
            if (exitMessage.getWatch() != null)
                this.watchedCustomers.remove(exitMessage.getWatch());
        }

        return super.handleLifecycleMessage(m);
    }
}
