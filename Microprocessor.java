

import java.util.*;
import java.io.*;


public class Microprocessor {

    static ArrayList<Instruction> instructions;
    static ArrayList<Instruction> instructionsCopy;
    static Queue<Instruction> instructionsToWrite; //got issued
    static ArrayList<Instruction> currInstructions; //got issued
    static int clockCycle = 0;
    static ReservationStation[] Adder;
    static ReservationStation[] Multiplier;
    static Buffer[] Load;
    static Buffer[] Store;
    static RegisterFile[] RegisterFile; // reg number is the position
    static CommonDataBus CDB; // one value has tag, the other has the actual value
    static double[] Memory;
    static boolean stall=false;
    static boolean stallBranch=false;
    static int pc=0;
    static boolean setPC=false;

   public Microprocessor(int adderSize, int multiplierSize, int loadSize, int storeSize, int registerSize, int memorySize)
   {
    instructions= new ArrayList<>();
    instructionsCopy= new ArrayList<>();
    instructionsToWrite=new LinkedList<>();
    currInstructions=new ArrayList<>();
    Adder = new ReservationStation[adderSize];
    populateReservationStation(Adder, "A");
    Multiplier = new ReservationStation[multiplierSize];
    populateReservationStation(Multiplier, "M");
    Load = new Buffer[loadSize];
    populateBuffer(Load, "L");
    Store = new Buffer[storeSize];
    populateBuffer(Store, "S");

    RegisterFile = new RegisterFile[registerSize];
    populateRegisterfile(RegisterFile);
    


    CDB = new CommonDataBus(); 
    Memory = new double[memorySize];
    
       
       

   }

    public static void populateReservationStation(ReservationStation[] reservationStation, String tagCharacter)
    {
         for(int i=0; i<reservationStation.length; i++)
         {
            // makes every position have the correct tag; m1 m2 m3 etc
              reservationStation[i] = new ReservationStation(tagCharacter+ (i+1),-1);
         }
    }

    public static void populateRegisterfile(RegisterFile[] registerFile)
    {
         for(int i=0; i<registerFile.length; i++)
         {
            // makes every position have the correct tag; m1 m2 m3 etc
              registerFile[i] = new RegisterFile();
         }
    }

    public static void populateBuffer(Buffer[] buffer, String tagCharacter)
    {
         for(int i=0; i<buffer.length; i++)
         {
             // makes every position have the correct tag; L1 L2 L3 etc
              buffer[i] = new Buffer(tagCharacter + (i+1));
         }
    }
    
    public static void checkCleanup(){
    //go thro the instructions getting executed rn and update stuff according yto cycke
    //if it is done, we remove it from the reservation station call method emptyBuffer, and remove from the instruction queue


   }

   public static void issue(Instruction instruction){
    if (instruction.issued==false)
  {
    //Available reservation station
     if(reservationStation(instruction) != -1 && !stallBranch)
     {
       stall=false;
       System.out.println("stall is now false : " + stall);
        issueInstruction(instruction);  
      //  currInstructions.add(instruction);   
           
     } 
     else{
        if(reservationStation(instruction) == -1)
          {  stall=true;
            System.out.println("--------Stall due to Issue-------------------------------------------------");}
        
     }
   }
}

   public static void execute(){

    
    for(ReservationStation reservationStation: Multiplier)
    {
        Instruction instruction= reservationStation.instruction;
        if(instruction!=null &&!instruction.executed && instruction.issued && instruction.issueCycle!=clockCycle) //check instruction has not been executed
    {
        if(instruction.executeStartCycle == -1) //execution has not started
       { if(canExecute(instruction))
        {
            System.out.println("CAN EXECUTE MULTIPLY " + instruction.instructionString);
            //start execution
            instruction.executeStartCycle = clockCycle;
            startExecute(instruction);
        }
         }
    else{
            if(instruction.duration>0)
            {
        instruction.duration--;
         updateDurationInStation(instruction);
            }

        if(instruction.duration == 0)
        {
            instruction.executed = true;
            instruction.executeEndCycle = clockCycle;
            executeInstruction(instruction);
            instructionsToWrite.add(instruction);

           
        }   
    } 
    }
      } 

    
    
    
    
    
    for(ReservationStation reservationStation: Adder)
    {
        Instruction instruction= reservationStation.instruction;
        if(instruction!=null && !instruction.executed && instruction.issued && instruction.issueCycle!=clockCycle) //check instruction has not been executed
    {
        if(instruction.executeStartCycle == -1) //execution has not started
       { if(canExecute(instruction))
        {
            //start execution
            instruction.executeStartCycle = clockCycle;
            startExecute(instruction);
            
        }
         }
    else{
           
        if(instruction.duration>0)
        {
            instruction.duration--;
            System.out.println("instruction duration inside instruction : " +instruction.duration);
            updateDurationInStation(instruction);
        }

        if(instruction.duration == 0)
        {
            instruction.executed = true;
            instruction.executeEndCycle = clockCycle;
            executeInstruction(instruction);
            instructionsToWrite.add(instruction);
        }
    
        
    } 
    }

      

    }
    
      for (Buffer buffer: Load)
    {
        Instruction instruction= buffer.instruction;
        if(instruction!=null &&!instruction.executed && instruction.issued && instruction.issueCycle!=clockCycle)  //check instruction has not been executed
    {
        if(instruction.executeStartCycle == -1) //execution has not started
       { if(canExecute(instruction))
        {
            //start execution
            instruction.executeStartCycle = clockCycle;
            startExecute(instruction);
        }
         }
    else{

        if(instruction.duration>0)
        {
            instruction.duration--;
            updateDurationInStation(instruction);
        }

        if(instruction.duration == 0)
        {
            instruction.executed = true;
            instruction.executeEndCycle = clockCycle;
            executeInstruction(instruction);
            instructionsToWrite.add(instruction);

        }

    }
}
    }
    for (Buffer buffer: Store)
    {
        Instruction instruction= buffer.instruction;
        if(instruction!=null &&!instruction.executed && instruction.issued && instruction.issueCycle!=clockCycle) //check instruction has not been executed
    {
        if(instruction.executeStartCycle == -1) //execution has not started
       { if(canExecute(instruction))
        {
            //start execution
            instruction.executeStartCycle = clockCycle;
            startExecute(instruction);
        }
         }
    else{
        if(instruction.duration>0)
        {
            instruction.duration--;
            updateDurationInStation(instruction);
        }

        if(instruction.duration == 0)
        {
            System.out.println("sTORE FINISHED-------");
            instruction.executed = true;
            instruction.executeEndCycle = clockCycle;
            executeInstruction(instruction);
            instruction.written=true;
            emptyBuffer(Store, instruction.position);
           
        }

    }
}
    }

}

    public static void startExecute(Instruction instruction)
    {
        switch(instruction.instructionType)
        {
            //FIXME bnez??
            case ADD: case SUB: case ADDI: case SUBI: case BNEZ:  //FIXME bnez here?
                Adder[instruction.position].startCycle = clockCycle;
                Adder[instruction.position].executeEndCycle = clockCycle + instruction.duration  ;
                break;
            case MUL: case DIV: 
                Multiplier[instruction.position].startCycle = clockCycle;
                Multiplier[instruction.position].executeEndCycle = clockCycle + instruction.duration  ;

                break;
            case LD:
                Load[instruction.position].startCycle = clockCycle;
                Load[instruction.position].executeEndCycle = clockCycle + instruction.duration  ;
                break;
            case SD:
                Store[instruction.position].startCycle = clockCycle;
                Store[instruction.position].executeEndCycle = clockCycle + instruction.duration  ;
                break;
            default: 
                break;
        }
        instruction.duration--;
    }

    public static void updateDurationInStation(Instruction instruction)
    {
        switch(instruction.instructionType)
        {
            //FIXME bnez??
            case ADD: case SUB: case ADDI: case SUBI: case BNEZ:  //FIXME bnez here?
                Adder[instruction.position].duration--;
                break;
            case MUL: case DIV: 
                Multiplier[instruction.position].duration--;
                break;
            case LD:
                Load[instruction.position].duration--;
                break;
            case SD:
                Store[instruction.position].duration--;
               break;
            default: 
                break;
        }
    }

   public static boolean canExecute(Instruction instruction)
   {
         switch(instruction.instructionType)
         {
              case ADD: case SUB:   
                return Adder[instruction.position].qj=="" && Adder[instruction.position].qk=="";
              case ADDI: case SUBI: 
                return Adder[instruction.position].qj==""; //immediate is always available
              case MUL: case DIV: 
                return Multiplier[instruction.position].qj=="" && Multiplier[instruction.position].qk=="";
              case LD:
                return true;
              case SD:
                return RegisterFile[instruction.destinationRegister].busy==0; 
              case BNEZ: 
                return RegisterFile[instruction.destinationRegister].busy==0; 
              default: return false;
                    
                }
                
         }
   
   public static void executeInstruction(Instruction instruction)
   {
        switch(instruction.instructionType)
        {
            case ADD:   //FIXME bnez here?
                instruction.result = Float.parseFloat(Adder[instruction.position].vj) + Float.parseFloat(Adder[instruction.position].vk);
                Adder[instruction.position].readyToWrite = true;
                break;
            case SUB: 
                instruction.result = Float.parseFloat(Adder[instruction.position].vj) - Float.parseFloat(Adder[instruction.position].vk);
                Adder[instruction.position].readyToWrite = true;
                break;
            case ADDI:
                instruction.result = Float.parseFloat(Adder[instruction.position].vj) + instruction.immediate;
                Adder[instruction.position].readyToWrite = true;
                break;
            case SUBI:
                instruction.result = Float.parseFloat(Adder[instruction.position].vj) - instruction.immediate;
                Adder[instruction.position].readyToWrite = true;
                break; 
            
            case MUL:
                instruction.result = Float.parseFloat(Multiplier[instruction.position].vj) * Float.parseFloat(Multiplier[instruction.position].vk);
                Multiplier[instruction.position].readyToWrite = true;
                break;
            case DIV: 
                instruction.result = Float.parseFloat(Multiplier[instruction.position].vj) / Float.parseFloat(Multiplier[instruction.position].vk);
                Multiplier[instruction.position].readyToWrite = true;
                break;
            case LD: 
               // RegisterFile[instruction.destinationRegister].value = Memory[instruction.effectiveAddress];
                Load[instruction.position].readyToWrite = true;
                System.out.println("Made load read to write");
                break;
            case SD:
                 Memory[instruction.effectiveAddress] = RegisterFile[instruction.destinationRegister].value;
                 
                break;
            case BNEZ: //TODO bnez??
                
                  if(RegisterFile[instruction.destinationRegister].value != 0)
                  {
                    setPC=true;
                  }
                  Adder[instruction.position].readyToWrite = true;
                  
                
            break;
            default: 
                break;
        }
   }
   
   public static void write()
   {
    {

       if(instructionsToWrite.isEmpty())
       {
           return;
       }
      
       else
       {
        Instruction instructionToWrite = instructionsToWrite.peek();
        if(instructionToWrite.executeEndCycle!=clockCycle)
        {
            if(instructionToWrite.instructionType == InstructionType.BNEZ)
                {stallBranch=false;}
            writeInstruction(instructionToWrite);
            System.out.println("Instruction to be written: " + instructionToWrite.instructionString);
            instructionsToWrite.poll(); 
            
        }
       }
       
       
      
    }


   }

   
//    public static int findIndex(Instruction instruction)
//    {
//        int i=0;
//          for(Instruction ins: instructions)
//          {
//               if(ins.equals(instruction))
//               {
//                 return i;
//               }
//               i++;
//          }
//        return -1;
//    }

   public static void emptyReservationStation(ReservationStation[] reservationStation, int position)
   {
       reservationStation[position].readyToWrite = false;
       reservationStation[position].busy = 0;
       reservationStation[position].instructionType = null;
       reservationStation[position].vj = "";
       reservationStation[position].vk = "";
       reservationStation[position].qj = "";
       reservationStation[position].qk = "";
       reservationStation[position].A = "";
       reservationStation[position].startCycle=-1;
       reservationStation[position].duration=-1;
       reservationStation[position].instruction=null;
       reservationStation[position].executeEndCycle=-1;

   }

   public static void emptyBuffer(Buffer[] buffer, int position)
   {
       buffer[position].busy = 0;
       buffer[position].effectiveAddress = -1;
       buffer[position].startCycle = -1;
         buffer[position].readyToWrite = false;
            buffer[position].instruction=null;
            buffer[position].executeEndCycle=-1;
            buffer[position].duration=-1;
   }

   public static void writeInstruction(Instruction instruction)
   {
        
        
        instruction.written=true;
        instruction.finished=true; 
        //instructionsToWrite.remove(instruction);
       // currInstructions.remove(instruction); 
          switch(instruction.instructionType)
        {
            case ADD: case SUB: case ADDI: case SUBI:  //FIXME bnez here?
                if(Adder[instruction.position].readyToWrite)
                {
                  
                    CDB.tag = Adder[instruction.position].tag;
                    CDB.value = instruction.result + "";
                    emptyReservationStation(Adder, instruction.position);
                    instruction.written = true;
                }
                break;
            case MUL: case DIV: 
                if(Multiplier[instruction.position].readyToWrite)
                {
                  
                    CDB.tag = Multiplier[instruction.position].tag;
                    CDB.value = instruction.result +"";
                    emptyReservationStation(Multiplier, instruction.position);
                    instruction.written = true;
                }
                break;
            case LD:
                if(Load[instruction.position].readyToWrite)
                {
                  
                    CDB.tag = Load[instruction.position].tag;
                    CDB.value = Memory[instruction.effectiveAddress] + "";
                    System.out.println("Value of memory is: " + Memory[instruction.effectiveAddress] + " and effective address is: " + instruction.effectiveAddress);
                    RegisterFile[instruction.destinationRegister].value = Memory[instruction.effectiveAddress];
                    emptyBuffer(Load, instruction.position);
                    instruction.written = true;
                }
                break;
            case BNEZ:
                if(Adder[instruction.position].readyToWrite)
                {
                    int position=instruction.position;
                    //FIXME write to CDB
                    if(setPC)
                        {pc=0;
                        setPC=false;
                            setAllInstructionsToNotIssued();
                    }
                    CDB.tag = Adder[position].tag;
                    stallBranch=false;
                    CDB.value = "PC value is: " + pc;
                    emptyReservationStation(Adder, position);
                    instruction.written = true;
                    
                }
                break;
            default: 
                break;
        }
        updateStations(instruction);
   }

   public static void setAllInstructionsToNotIssued()
   {
    int i=0;
       for(Instruction instruction: instructions)
       {
           instruction.issued = false;
           instruction.issueCycle = -1;
           instruction.executed = false;
           instruction.duration = instruction.setDurationAgain(); //based on instruction type
           instruction.written=false;
           instruction.executeStartCycle=-1;
           
           instruction.result=-1;
        //    instruction.effectiveAddress=-1;
           //instruction.tag="";
       
       }

    //    instructions.addAll(instructionsCopy);
    //    System.out.println("Instructions size is: " + instructions.size());
   }
   public static void updateStations(Instruction instruction)
   {
       for(ReservationStation reservationStation: Adder)
       {
           if(reservationStation.qj.equals(instruction.tag))
           {
               reservationStation.qj = "";
               reservationStation.vj = CDB.value+ "";
           }
           if(reservationStation.qk.equals(instruction.tag))
           {
               reservationStation.qk = "";
               reservationStation.vk = CDB.value+ "";
           }
       }
       for(ReservationStation reservationStation: Multiplier)
       {
           if(reservationStation.qj.equals(instruction.tag))
           {
               reservationStation.qj = "";
               reservationStation.vj = CDB.value+ "";
           }
           if(reservationStation.qk.equals(instruction.tag))
           {
               reservationStation.qk = "";
               reservationStation.vk = CDB.value+ "";
           }
       }
    //    for(Buffer buffer: Store)
    //    {
    //        if(buffer.tag.equals(instruction.tag))
    //        {
    //            buffer.readyToWrite = false;
    //            buffer.busy = 0;
    //            buffer.effectiveAddress = -1;
    //            buffer.startCycle = -1;
    //        }
    //   }
    for(int i=0; i<RegisterFile.length; i++)
    {
        if(RegisterFile[i].tag.equals(instruction.tag))
        {
            RegisterFile[i].busy = 0;
            RegisterFile[i].tag = "";
            RegisterFile[i].value = Float.parseFloat(CDB.value);
        }
    }

}


   public static int availableReservationStation(ReservationStation[] reservationStation)
   {
       for(int i=0; i<reservationStation.length; i++)
       {
           if(reservationStation[i].busy == 0)
           {
               return i;
           }
       }
       return -1;
   } 

   public static int availableBuffer(Buffer [] buffer)
   {
       for(int i=0; i<buffer.length; i++)
       {
           if(buffer[i].busy == 0)
           {
               return i;
           }
       }
       return -1;
   }

   //FIXME floating point in same reg file right not separate?

   // return the position of the reservation station that is available , else -1 if no free slot
   public static int reservationStation(Instruction instruction)
   {
        switch(instruction.instructionType)
        {
            case ADD: case SUB: case ADDI: case SUBI: case BNEZ:  //FIXME bnez here?
                return availableReservationStation(Adder);
            case MUL: case DIV: 
                return availableReservationStation(Multiplier);
            case LD:
                return availableBuffer(Load);
            case SD:
                return availableBuffer(Store);
            default: return -1;
                   
                }
                
        }

   public static void checkAvailability(int j, int k, ReservationStation[] reservationStation, int position)
        {
            if(RegisterFile[j].busy == 0)
            {  //register value is available
                reservationStation[position].vj = RegisterFile[j].value + "";
                reservationStation[position].qj = "";
            }
            else
            { //register value is not available so we add the reg tag of the reservation station then
                reservationStation[position].qj = RegisterFile[j].tag;
                reservationStation[position].vj = "";
            }
            if(RegisterFile[k].busy == 0)
            {
                reservationStation[position].vk = RegisterFile[k].value + "";
                reservationStation[position].qk = "";
            }
            else
            {
                reservationStation[position].qk = RegisterFile[k].tag;
                reservationStation[position].vk = "";
            }
        }
        
   
public static void checkAvailabilityImmediate(int j, int immediate, ReservationStation[] reservationStation, int position)
 {
            if(RegisterFile[j].busy == 0)
            {  //register value is available
                reservationStation[position].vj =RegisterFile[j].value + "";
                reservationStation[position].qj = "";
            }
            else
            { //register value is not available so we add the reg tag of the reservation station then
                reservationStation[position].qj = RegisterFile[j].tag;
                reservationStation[position].vj = "";
            }
            
                reservationStation[position].vk = immediate + "";
                reservationStation[position].qk = "";
            
            
        }

    public static void issueInstruction(Instruction instruction)
        {
            int position = reservationStation(instruction);
            
            switch(instruction.instructionType)
            {
                case ADD: case SUB:   //FIXME bnez here?
                    Adder[position].busy = 1;
                    Adder[position].instructionType = instruction.instructionType;
                    Adder[position].duration = instruction.duration ;
                    Adder[position].instruction=instruction;
                    checkAvailability(instruction.j, instruction.k, Adder, position);
                    RegisterFile[instruction.destinationRegister].busy = 1;
                    RegisterFile[instruction.destinationRegister].tag = Adder[position].tag;
                    instruction.tag = Adder[position].tag;
                    break;

                case MUL: case DIV: 
                   Multiplier[position].busy = 1;
                   Multiplier[position].instructionType = instruction.instructionType;
                    Multiplier[position].duration = instruction.duration ;
                    Multiplier[position].instruction=instruction;
                   checkAvailability(instruction.j, instruction.k, Multiplier, position);
                    RegisterFile[instruction.destinationRegister].busy = 1;
                    RegisterFile[instruction.destinationRegister].tag = Multiplier[position].tag;
                    instruction.tag = Multiplier[position].tag;
                    break;

                case ADDI: case SUBI: 
                    Adder[position].busy = 1;
                    Adder[position].instructionType = instruction.instructionType;
                    Adder[position].duration = instruction.duration ;
                    Adder[position].instruction=instruction;
                    checkAvailabilityImmediate(instruction.j, instruction.immediate, Adder, position);
                    RegisterFile[instruction.destinationRegister].busy = 1;
                    RegisterFile[instruction.destinationRegister].tag = Adder[position].tag;
                    instruction.tag = Adder[position].tag;

                   break;

                case LD:
                    Load[position].busy = 1;
                    Load[position].effectiveAddress= instruction.effectiveAddress;
                    Load[position].instruction=instruction;
                    Load[position].duration = instruction.duration ;
                    RegisterFile[instruction.destinationRegister].busy = 1; 
                    RegisterFile[instruction.destinationRegister].tag = Load[position].tag;
                    instruction.tag = Load[position].tag;
                    break;
                case SD:
                    Store[position].busy = 1;
                    Store[position].effectiveAddress= instruction.effectiveAddress;
                    Store[position].instruction=instruction;
                    Store[position].duration = instruction.duration ;
                   // RegisterFile[instruction.destinationRegister].busy = 1; 
                   // RegisterFile[instruction.destinationRegister].tag = Store[position].tag;
                    instruction.tag = Store[position].tag;
                    break;
                case BNEZ:
                    Adder[position].busy = 1;
                    Adder[position].instructionType = instruction.instructionType;
                    Adder[position].duration = instruction.duration ;
                    Adder[position].instruction=instruction;
                    checkAvailabilityBranch(instruction.destinationRegister, Adder, position);
                    //RegisterFile[instruction.destinationRegister].busy = 1;
                    //RegisterFile[instruction.destinationRegister].tag = Adder[position].tag;
                    instruction.tag = Adder[position].tag;
                    System.out.println("--------Stall due to Branch-----------");
                    stallBranch=true;

                break;
            }
            instruction.issued=true;
            instruction.issueCycle=clockCycle;
            System.out.println("Issue cycle of " + instruction.instructionString + " is " + instruction.issueCycle);
            instruction.position=position; 
        }


        public static void checkAvailabilityBranch(int destinationRegister , ReservationStation[] reservationStation, int position)
        {
            if(RegisterFile[destinationRegister].busy == 0)
            {  //register value is available
                reservationStation[position].vj =RegisterFile[destinationRegister].value + "";
                reservationStation[position].qj = "";
            }
            else
            { //register value is not available so we add the reg tag of the reservation station then
                reservationStation[position].qj = RegisterFile[destinationRegister].tag;
                reservationStation[position].vj = "";
            }
            
                reservationStation[position].vk = "";
                reservationStation[position].qk = "";
            
            
        }
    public static void printWords(String[] words){
        for(String word: words){
            System.out.println(word);
        }
    }
    public static void loadInstructions(Microprocessor microprocessor, int[] latencies){
         //read the file
        try {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
              String[] words = data.split("[,\\s]+");
             // printWords(words);
              Instruction instruction = new Instruction(words,microprocessor,latencies,data);
             // Instruction instruction2=new Instruction(words,microprocessor,latencies,data);
              instructions.add(instruction);
             // instructionsCopy.add(instruction2);
             // currInstructions.add(instruction);
            }
            myReader.close();
          } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
        /* Instruction instruction = new Instruction();
            instruction.initialise(); */
    }

    public static void print(int pc, int clockCycle)
    {
        //print content of reg file
        //print current instruction
        //print reservation stations
        //print buffers
        //print CDB
        //print memory
        //print clock cycle
        //print instructions
        //print instructions to write
            System.out.println("Clock Cycle: " + clockCycle);
            System.out.println("PC Value: " + pc);
        
            // Print content of reservation stations
            System.out.println("Reservation Stations:");
            System.out.println("Adder:");
            int i=0;
            for (ReservationStation rs : Adder) {
                
                    System.out.print("Adder position"+ i+": " );
                    rs.print();
                    System.out.println();
                    i++;
            }
            System.out.println("Multiplier:");
            i=0;
            for (ReservationStation rs : Multiplier) {
            
                    System.out.println("Multiplier position"+ i+": " );
                    rs.print();
                    System.out.println();
                    i++;
            }
        
            // Print content of buffers
            System.out.println("Load Buffers:");
            i=0;
            for (Buffer buffer : Load) {
                
                    System.out.println("Load buffer position"+ i+": " );
                    buffer.print();
                    System.out.println();
                    // Instruction instruction=buffer.instruction;
                    // if(instruction!=null)
                    // System.out.println("instruction duration inside instruction : " +buffer.instruction.duration);
                    i++;
            }
            System.out.println("Store Buffers:");
            i=0;
            for (Buffer buffer : Store) {
                
                    System.out.println("Store buffer position"+ i+": " );
                    buffer.print();
                    System.out.println();
                    i++;
            }
        
            // Print content of Register File
            i=0;
            System.out.println("Register File:");
            for (RegisterFile reg : RegisterFile) {
                System.out.print("R"+i+": " );
                reg.print();
                System.out.println();
                i++;
            }
        
            // Print content of Memory
            i=0;
            System.out.println("Memory:");
            for (double mem : Memory) {
                System.out.print("M"+i+": " );
                System.out.println(mem);
                i++;
            }
        
            // Print content of Common Data Bus
            System.out.println("Common Data Bus:");
            CDB.print();
            System.out.println();
        
            // Print current instruction being issued
            if(pc<instructions.size())
              {  Instruction currentInstruction = (Instruction) instructions.get(pc);
                System.out.println("Current Instruction in PC: " + currentInstruction.instructionString); }
            
        
            // Print instructionsToWrite
            System.out.println("Instructions To Write:");
            for (Instruction instruction : instructionsToWrite) {
                
                System.out.println(instruction.instructionString);
            }

            //print stallbranch
            System.out.println("Stall Branch: " + stallBranch);
            System.out.println("Stall: " + stall);
        
            System.out.println("---------------------------------------");
        }
        
        public static boolean allStationsEmpty()
        {
            for(ReservationStation reservationStation: Adder)
            {
                if(reservationStation.busy == 1)
                {
                    return false;
                }
            }
            for(ReservationStation reservationStation: Multiplier)
            {
                if(reservationStation.busy == 1)
                {
                    return false;
                }
            }
            for(Buffer buffer: Load)
            {
                if(buffer.busy == 1)
                {
                    return false;
                }
            }
            for(Buffer buffer: Store)
            {
                if(buffer.busy == 1)
                {
                    return false;
                }
            }

            for(RegisterFile registerFile: RegisterFile)
            {
                if(registerFile.busy == 1)
                {
                    return false;
                }
            }
            return true;
        }

    
    
    public static void main(String[] args) {
        //initialise the variables in this arch at the top
        //loop  --> check instructions loop
        //if there are previous instructions , we do the checks
        //check onstruction valid
        //see if issued do this etc
           // for(int i=0;)
       
        //TODO gui for this , input and output
        int mulLatency=6;
        int loadLatency=1;
        int storeLatency=1;
        int divLatency=4;
        int subLatency=4;
        int addLatency=4;

        int registerSize=17;
        int memorySize=7;

        int adderSize=3;
        int multiplierSize=2;
        int loadSize=3;
        int storeSize=3;
        int latencies[]={addLatency,subLatency,mulLatency,divLatency,loadLatency,storeLatency};

        
        Microprocessor microprocessor=new Microprocessor(adderSize,multiplierSize,loadSize,storeSize,registerSize,memorySize);
        loadInstructions(microprocessor,latencies);
       
       

      Memory[1]=1;
      Memory[2]=2;
    RegisterFile[1].value=  1;
    RegisterFile[2].value=  2;
    RegisterFile[4].value=  3;
    RegisterFile[6].value=  4;
    RegisterFile[8].value=  5;
    RegisterFile[9].value=  6;

    //RegisterFile[20].value=  3;
//     RegisterFile[15].value=15;
//     RegisterFile[2].value=3;
//     RegisterFile[4].value=1;
//    RegisterFile[16].value=10;


        int end=instructions.size();
        System.out.println("end is "+end);
        //print(pc, clockCycle);
        clockCycle=1;
        while (!allStationsEmpty() || pc<end ) {
            
            if(pc<end)
            {Instruction instruction= new Instruction();
                {
                    instruction.instructionType=((Instruction)instructions.get(pc)).instructionType;
                    instruction.destinationRegister= ((Instruction)instructions.get(pc)).destinationRegister;
                    instruction.j=((Instruction)instructions.get(pc)).j;
                    instruction.k=((Instruction)instructions.get(pc)).k;
                    instruction.effectiveAddress=((Instruction)instructions.get(pc)).effectiveAddress;
                    instruction.immediate=((Instruction)instructions.get(pc)).immediate;
                    instruction.issued=((Instruction)instructions.get(pc)).issued;
                    instruction.issueCycle=((Instruction)instructions.get(pc)).issueCycle;
                    instruction.executeStartCycle=((Instruction)instructions.get(pc)).executeStartCycle;
                    instruction.executeEndCycle=((Instruction)instructions.get(pc)).executeEndCycle;
                    instruction.jumpInstructionNumber=((Instruction)instructions.get(pc)).jumpInstructionNumber;
                    instruction.executed=((Instruction)instructions.get(pc)).executed;
                    instruction.duration=((Instruction)instructions.get(pc)).duration;
                    instruction.position=((Instruction)instructions.get(pc)).position;
                    instruction.written=((Instruction)instructions.get(pc)).written;
                    instruction.finished=((Instruction)instructions.get(pc)).finished;
                    instruction.result=((Instruction)instructions.get(pc)).result;
                    instruction.tag=((Instruction)instructions.get(pc)).tag;
                    instruction.instructionString=((Instruction)instructions.get(pc)).instructionString;
                   







                }
                System.out.println("Instruction to be issued: " + instruction.instructionString + " " + instruction.issued);
            if( !instruction.issued && !stallBranch)
                {issue(instruction);
                     if(!stall)
                        pc++;
                }
                     
            }
            execute();
            if(stall)
            {
                System.out.println("STALL");
                
            }
            
             write();

             //System.out.println("Instruction to write size : " + instructionsToWrite.size());
            print(pc, clockCycle);
            clockCycle++;

            if(clockCycle==15)
            break;

            
            
            

            
            
          }

        //   System.out.println(allStationsEmpty() + " stations are empty in cycle" + " " + clockCycle);
        //   Multiplier[1].print();
}

}