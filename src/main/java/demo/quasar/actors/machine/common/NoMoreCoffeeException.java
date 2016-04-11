package demo.quasar.actors.machine.common;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class NoMoreCoffeeException extends Exception {
    public NoMoreCoffeeException(String msg) {
        super(msg);
    }
}
