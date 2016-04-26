package demo.quasar.actors.machine;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.common.util.Exceptions;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import demo.quasar.actors.barista.message.CoffeeReady;
import demo.quasar.actors.machine.common.NoMoreCoffeeException;
import demo.quasar.actors.machine.common.RestartException;
import demo.quasar.actors.machine.message.MakeCoffee;

import java.util.concurrent.*;


/**
 * Created on 11/04/16.
 *
 * @author Maxim Balan
 */
public class CoffeeMachine extends BasicActor {

    private int capsules;
    private int restartIn;
    private final ScheduledExecutorService scheduler;
    private final ActorRef baristaRef;

    public CoffeeMachine(ActorRef baristaRef, int capsules) {
        this.baristaRef = baristaRef;
        this.capsules = capsules;
        this.restartIn = 3;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    @Suspendable
    protected Object doRun() throws InterruptedException, SuspendExecution {
        ScheduledFuture<Object> future;
        for(;;) {
            Object m = receive();

            future = scheduler.schedule(new FutureReceive(m), 2, TimeUnit.SECONDS);
            try {
                m = future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(m instanceof MakeCoffee) {

                    makeCoffee((MakeCoffee) m);

            } else {
                System.out.println("Unknown message type: "+ m);
            }
        }
    }

    private void makeCoffee(MakeCoffee makeCoffee) throws SuspendExecution{
        if(checkCapsules()) {
            Exceptions.rethrow(new NoMoreCoffeeException("[CoffeeMachine]: No more coffee capsules"));
        }

        if(checkRestartTime(makeCoffee.getCoffeeType().getTime())) {
            Exceptions.rethrow(new RestartException("[CoffeeMachine]: Coffee machine requires a restart"));
        }

        System.out.println(String.format("[CoffeeMachine]: %d seconds required to make %s for %s",makeCoffee.getCoffeeType().getTime(), makeCoffee.getCoffeeType().getValue(), makeCoffee.getCustomerName()));

        restartIn = restartIn - makeCoffee.getCoffeeType().getTime();
        capsules--;

        System.out.println(String.format("[CoffeeMachine]: restarts in %d", restartIn));
        System.out.println(String.format("[CoffeeMachine]: capsules left %d", capsules));

        baristaRef.send(new CoffeeReady(makeCoffee.getCustomerName(), makeCoffee.getCoffeeType()));
    }

    private boolean checkRestartTime(int preparationTime) {
        return restartIn < preparationTime;
    }

    private boolean checkCapsules() {
        return capsules == 0;
    }

}

@Suspendable
class FutureReceive implements Callable<Object> {

    private final Object receive;

    FutureReceive(Object receive) {
        this.receive = receive;
    }

    @Suspendable
    public Object call() throws Exception {
        return receive;
    }

}