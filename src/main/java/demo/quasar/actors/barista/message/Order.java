package demo.quasar.actors.barista.message;

import demo.quasar.actors.common.CoffeeType;

/**
 * Created on 13/04/16.
 *
 * @author Maxim Balan
 */
public class Order {
    private final CoffeeType coffeeType;
    private final String customerName;

    public Order(CoffeeType coffeeType, String customerName) {
        this.coffeeType = coffeeType;
        this.customerName = customerName;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    public String getCustomerName() {
        return customerName;
    }
}
