package jifa.racecarsandstuff;

public class Options {
    public int trackFlag, lapCount;
    public boolean oil, cracks, ai;
    public String car;

    public Options(){
        // default level options
        trackFlag = 0;
        lapCount = 3;
        oil = false;
        cracks = false;
        ai = false;
        car = "blue";
    }
}
