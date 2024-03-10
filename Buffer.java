public class Buffer {
    String tag;
    int busy;
    int effectiveAddress;
    int startCycle;
    int duration;
    int executeEndCycle;
    boolean readyToWrite;
    Instruction instruction;

    public Buffer(String tagCharacter) {
        this.busy = 0;
        this.tag = tagCharacter ;
        this.effectiveAddress = -1;
        this.startCycle = -1;
        this.duration = -1;
        this.executeEndCycle=-1;
    }
    
    public void print()
    {
        System.out.print("Tag: " + tag +
                         "\tBusy: " + busy +
                         "\tEffective Address: " + effectiveAddress +
                         "\tStart Cycle: " + startCycle +
                         "\t Execute End Cycle: " + executeEndCycle +
                         "\tDuration left: " + duration +
                         "\tReady To Write: " + readyToWrite);
    
    }
    
}

