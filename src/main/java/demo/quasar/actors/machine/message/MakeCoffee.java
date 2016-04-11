package demo.quasar.actors.machine.message;

import demo.quasar.actors.common.CoffeeType;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class MakeCoffee {

    private final CoffeeType coffeeType;
    private final String customerName;

    public MakeCoffee(CoffeeType coffeeType, String customerName) {
        this.coffeeType = coffeeType;
        this.customerName = customerName;
    }

    public CoffeeType getCoffeeType() {
        return this.coffeeType;
    }

    public String getCustomerName() {
        return customerName;
    }

}
