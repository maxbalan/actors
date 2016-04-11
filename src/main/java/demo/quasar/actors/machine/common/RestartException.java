package demo.quasar.actors.machine.common;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class RestartException extends Exception {
    public RestartException(String msg) {
        super(msg);
    }
}
