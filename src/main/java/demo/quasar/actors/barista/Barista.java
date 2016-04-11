package demo.quasar.actors.barista;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.CoffeReady;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class Barista extends BasicActor {
    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive();

            if(m instanceof CoffeReady){

            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }
    }
}
