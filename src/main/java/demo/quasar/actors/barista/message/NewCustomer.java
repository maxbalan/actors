package demo.quasar.actors.barista.message;

import co.paralleluniverse.actors.ActorRef;

/**
 * Created on 12/04/16.
 *
 * @author Maxim Balan
 */
public class NewCustomer {
    private final ActorRef customerRef;

    public NewCustomer(ActorRef customerRef) {
        this.customerRef = customerRef;
    }

    public ActorRef getCustomerRef() {
        return customerRef;
    }
}
