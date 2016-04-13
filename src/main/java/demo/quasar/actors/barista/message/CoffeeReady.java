package demo.quasar.actors.barista.message;

import demo.quasar.actors.common.CoffeeType;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class CoffeeReady {

    private final String customerName;
    private final CoffeeType coffeeType;


    public CoffeeReady(String customerName, CoffeeType coffeeType) {
        this.customerName = customerName;
        this.coffeeType = coffeeType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

}
