public class Instruction {
    
    InstructionType instructionType;
    int destinationRegister;  //reg number
    int j; //mul add  sub div addi subi
    int k; // mull add  sub div etc
    int effectiveAddress;
    int immediate; //for addi subi etc
    boolean issued;
    int issueCycle;
    int executeStartCycle;
    int executeEndCycle;
    int jumpInstructionNumber; //for bnez
    boolean executed;
    int duration; // (based on instruction type ; switch case)
    int position; //the position in the buffer 
    boolean written;
    boolean finished;
    float result;
    String tag;
    public String instructionString;



    // LD F2 0(R1)   
    // SD F2 -5(R1)
    // MUL F4 F2 F0
    // ADD F6 F4 F8
    // SUB F10 F6 F12
    // DIV F14 F10 F16
    // ADDI F18 F14 4 
    // SUBI F20 F18 4
    // BNEZ F22 10 


    //latencies
    static int bnezLatency =1;
    static int addiLatency =1;
    static int addLatency;
    static int subiLatency =1;
    static int subLatency;
    static int mulLatency;
    static int divLatency;
    static int loadLatency;
    static int storeLatency;
   
    

//     insucion class --> instcution type , and has its own counter according to the stages of the intrsuctin type
// taken to load (starts decrementing once issued and exec started) (boolean exec staryted)

    public Instruction(String [] words,Microprocessor microprocessor, int[] latencies, String data) {

        //set latencies
        addLatency=latencies[0];
        subLatency=latencies[1];
        mulLatency=latencies[2];
        divLatency=latencies[3];
        loadLatency=latencies[4];
        storeLatency=latencies[5];

        this.instructionType = InstructionType.valueOf(words[0].toUpperCase());
        setAttributes(words);
        this.issued = false;
        this.issueCycle = -1;
        this.executed = false;
        this.duration = setDuration(words); //based on instruction type
        this.position = -1;
        this.destinationRegister= Integer.parseInt(words[1].substring(1)); // extract the register number
        //bnez reg number to check
        
        this.written=false;
        this.finished=false;
        this.executeStartCycle=-1;
        this.executeEndCycle=-1;
        this.result=-1;
        this.tag="";
        this.instructionString=data;

    }
    public Instruction()
    {
        
    }

   

    public void setAttributes(String [] words)
    {
            switch(words[0])
            {
                case "LD": case "SD":

                this.effectiveAddress=Integer.parseInt(words[2]); // extract the register number   
                break;

                case "ADD": case "SUB": case "MUL": case "DIV":
                    this.j=Integer.parseInt(words[2].substring(1)); // extract the register number
                    this.k=Integer.parseInt(words[3].substring(1)); // extract the register number
                     break;
                case "ADDI": case "SUBI":
                    this.j=Integer.parseInt(words[2].substring(1)); // extract the register number
                    this.immediate=Integer.parseInt(words[3]); // extract the register number
                    break;
                case "BNEZ":
                  //  this.jumpInstructionNumber=Integer.parseInt(words[2]); // extract the register number
                    // this.j=words[1].substring(1); // extract the register number
                    // this.jump=Integer.parseInt(words[2]);
                    // this.regToJump=Integer.parseInt(words[3].substring(1));
                    // break;
                
            }
    }

    public int setDuration(String [] words)
    {
        switch(words[0])
        {
            case "LD":
                return loadLatency;
            case "SD":
                return storeLatency;
            case "ADD":
                return addLatency;
            case "SUB":
                return subLatency;
            case "SUBI":
                return subiLatency;    
            case "MUL":
                return mulLatency;
            case "DIV":
                return divLatency;
            case "ADDI":
                return addiLatency;
            case "BNEZ":
                return bnezLatency;
            default:
                return 0;
        }
    }

    public void print()
    {
        System.out.println(instructionString);
        System.out.print("Instruction Type: " + instructionType +
                         "\tDestination Register: " + destinationRegister +
                         "\tJ: " + j +
                         "\tK: " + k +
                         "\tEffective Address: " + effectiveAddress +
                         "\tImmediate: " + immediate +
                         "\tIssued: " + issued +
                         "\tIssue Cycle: " + issueCycle +
                         "\tExecuted: " + executed +
                         "\tDuration: " + duration +
                         "\tPosition: " + position +
                         "\tWritten: " + written +
                         "\tFinished: " + finished +
                         "\tExecute Start Cycle: " + executeStartCycle +
                         "\tExecute End Cycle: " + executeEndCycle +
                         "\tResult: " + result +
                         "\tTag: " + tag);
    
    }

    public int setDurationAgain()
    {
        switch(instructionType)
        {
            case LD:
                return loadLatency;
                
            case SD:
                return storeLatency;
                
            case ADD:
                return addLatency;
                
            case SUB:
                return subLatency;
                
            case SUBI:
                return subiLatency;
                    
            case MUL:
                return mulLatency;
                
            case DIV:
                return divLatency;
                
            case ADDI:
                return addiLatency;
                
            case BNEZ:
                return bnezLatency;
                
            default:
                return 0;
        }
    }



}
