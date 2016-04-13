package demo.quasar.actors.customer.message;

import demo.quasar.actors.common.CoffeeType;

/**
 * Created on 12/04/16.
 *
 * @author Maxim Balan
 */
public class TakeCoffee {
    public final CoffeeType coffeeType;

    public TakeCoffee(CoffeeType coffeeType) {
        this.coffeeType = coffeeType;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }
}
