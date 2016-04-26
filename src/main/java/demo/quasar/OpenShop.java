package demo.quasar;

import co.paralleluniverse.actors.LocalActor;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.Barista;

import java.util.concurrent.ExecutionException;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class OpenShop {
    public static void main(String[] args) throws ExecutionException, InterruptedException, SuspendExecution {
        LocalActor.join(new Barista().spawn());
    }
}