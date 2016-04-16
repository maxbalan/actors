package demo.quasar.actors.machine;

/**
 * Created on 4/16/2016
 *
 * @author Maxim Balan
 */
public class CoffeeMachineResources {
    private int capsules;
    private int restartIn;

    public CoffeeMachineResources(int capsules, int restartIn) {
        this.capsules = capsules;
        this.restartIn = restartIn;
    }

    public int getCapsules() {
        return capsules;
    }

    public int getRestartIn() {
        return restartIn;
    }

    public void updateState(int capsules, int restartIn) {
        this.capsules = capsules;
        this.restartIn = restartIn;
    }

    public void updateCapsules(int decreaseby) {
        capsules -=decreaseby;
    }

}
