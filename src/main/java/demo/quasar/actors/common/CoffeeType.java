package demo.quasar.actors.common;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public enum CoffeeType {

    ESPRESSO    (1,"espresso", 2),
    CAPPUCCINO  (2,"cappuccino", 3),
    MOCHA       (3,"mocha", 5),
    LATTE       (4,"latte", 15),
    AMERICANO   (5,"americano", 7);

    private final String value;
    private final int time;
    private final int menuOrder;

    private CoffeeType(String value, int time, int menuOrder) {
        this.value = value;
        this.time = time;
        this.menuOrder = menuOrder;
    }

    public static CoffeeType getCoffee(int orderId) {
        for(CoffeeType coffeeType : values()) {
            if(coffeeType.getMenuOrder() == orderId)
                return coffeeType;
        }

        return null;
    }

    public String getValue() {
        return this.value;
    }

    public int getTime() {
        return time;
    }

    public int getMenuOrder() {
        return menuOrder;
    }
}
