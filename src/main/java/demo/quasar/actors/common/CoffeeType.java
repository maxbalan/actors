package demo.quasar.actors.common;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public enum CoffeeType {

    ESPRESSO("espresso", 2),
    CAPPUCCINO("cappuccino", 3),
    MOCHA("mocha", 5),
    LATTE("latte", 15),
    AMERICANO("americano", 7);

    private final String value;
    private final int time;

    private CoffeeType(String value, int time) {
        this.value = value;
        this.time = time;
    }

    public String getValue() {
        return this.value;
    }

    public int getTime() {
        return time;
    }

}
