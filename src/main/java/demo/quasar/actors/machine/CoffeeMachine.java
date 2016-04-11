package demo.quasar.actors.machine;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.common.util.Exceptions;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.CoffeReady;
import demo.quasar.actors.machine.common.NoMoreCoffeeException;
import demo.quasar.actors.machine.common.RestartException;
import demo.quasar.actors.machine.message.MakeCoffee;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class CoffeeMachine extends BasicActor {

    private int capsules = 10;
    private int restartIn = 30;

    private final ActorRef baristaRef;

    public CoffeeMachine(ActorRef baristaRef) {
        this.baristaRef = baristaRef;
    }


    @Override
    protected Object doRun() throws InterruptedException, SuspendExecution {
        for(;;) {
            Object m = receive();

            if(m instanceof MakeCoffee) {
                makeCoffee((MakeCoffee) m);
            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }
    }

    private void makeCoffee(MakeCoffee makeCoffee) throws SuspendExecution {
        if(checkCapsules()) {
            Exceptions.rethrow(new NoMoreCoffeeException("No more coffee capsules"));
            return;
        }

        if(checkRestartTime(makeCoffee.getCoffeeType().getTime())) {
            Exceptions.rethrow(new RestartException("Coffee machine requires a restart"));
        }

        restartIn = restartIn - makeCoffee.getCoffeeType().getTime();
        capsules--;

        baristaRef.send(new CoffeReady(makeCoffee.getCustomerName(), makeCoffee.getCoffeeType()));
    }

    private boolean checkRestartTime(int preparationTime) {
        return restartIn > preparationTime;
    }

    private boolean checkCapsules() {
        return capsules == 0;
    }


    public void restartCoffeeMachine() {
        this.restartIn = 30;
    }

}
