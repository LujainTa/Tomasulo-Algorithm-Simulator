public class CommonDataBus {
    
    String tag;
    String value;

    public CommonDataBus() {
        this.tag = "";
        this.value = "";
    }

    public void print()
    {
        System.out.print("Tag: " + tag +
                         "\tValue: " + value);
    
    }
}
