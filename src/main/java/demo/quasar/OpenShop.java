package demo.quasar;

import co.paralleluniverse.actors.LocalActor;
import demo.quasar.actors.barista.Barista;

import java.util.concurrent.ExecutionException;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class OpenShop {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        LocalActor.join(new Barista().spawn());
    }
}
