package dk.cphbusiness.soft.mrv.benchmark;

/**
 * @author mrv
 * This class is heavily inspired by the Timer class from Peter Sestoft's paper, "Microbenchmarks in Java and C#".
 */
public class Timer {
    private long start, spent;
    public Timer(){ play(); }
    public double check() { return (System.nanoTime() - start + spent)/1e6; }
    public void pause() { spent += (System.nanoTime() - start); }
    public void play() { start = System.nanoTime(); }
}
