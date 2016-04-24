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











//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//
//        ScheduledExecutorService scheduler = Executors
//                .newScheduledThreadPool(1);
//        ScheduledFuture<String> future = scheduler.schedule(
//                new ScheduledPrinter(), 5, TimeUnit.SECONDS);
//        System.out.println("1 "+future.get());
//
//       future = scheduler.schedule(
//                new ScheduledPrinter(), 3, TimeUnit.SECONDS);
//        System.out.println("2 "+future.get());
//
//        future = scheduler.schedule(
//                new ScheduledPrinter(), 2, TimeUnit.SECONDS);
//        System.out.println("3 "+future.get());
//
//        future = scheduler.schedule(
//                new ScheduledPrinter(), 1, TimeUnit.SECONDS);
//        System.out.println(future.get());
//


//    }
}

// class ScheduledPrinter implements Callable<String> {
//     public String call() throws Exception {
//
//        return "somethhing";
//    }
//}