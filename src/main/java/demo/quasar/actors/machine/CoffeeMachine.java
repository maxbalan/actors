package demo.quasar.actors.machine;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.common.util.Exceptions;
import co.paralleluniverse.fibers.SuspendExecution;
import demo.quasar.actors.barista.message.CoffeeReady;
import demo.quasar.actors.machine.common.NoMoreCoffeeException;
import demo.quasar.actors.machine.common.RestartException;
import demo.quasar.actors.machine.message.MakeCoffee;

/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class CoffeeMachine extends BasicActor {

    private int capsules;
    private int restartIn;

    private final ActorRef baristaRef;

    public CoffeeMachine(ActorRef baristaRef, CoffeeMachineResources resources) {
        this.baristaRef = baristaRef;
        this.capsules = resources.getCapsules();
        this.restartIn = resources.getRestartIn();
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
        }

        if(checkRestartTime(makeCoffee.getCoffeeType().getTime())) {
            Exceptions.rethrow(new RestartException("Coffee machine requires a restart"));
        }

        System.out.println(String.format("%d seconds required to make %s for %s",makeCoffee.getCoffeeType().getTime(), makeCoffee.getCoffeeType().getValue(), makeCoffee.getCustomerName()));

        restartIn = restartIn - makeCoffee.getCoffeeType().getTime();
        capsules--;

        System.out.println(String.format("Coffee machine restarts in %d", restartIn));
        System.out.println(String.format("Coffee machine capsules left %d", capsules));

        baristaRef.send(new CoffeeReady(makeCoffee.getCustomerName(), makeCoffee.getCoffeeType()));
    }

    private boolean checkRestartTime(int preparationTime) {
        return restartIn < preparationTime;
    }

    private boolean checkCapsules() {
        return capsules == 0;
    }

}
