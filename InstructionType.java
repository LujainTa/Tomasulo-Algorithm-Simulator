public enum InstructionType {
        ADD,SUB,MUL,DIV,LD,SD,ADDI,SUBI,BNEZ
}

/* 

Format of the instructions

The number is the reg file position
float 2d array 0.0 and 1.0 for busy none busy, second the actual value


LD F2 0(R1) //changed to immediate addresses   
SD F2 -5(R1) 
MUL F4 F2 F0
ADD F6 F4 F8
SUB F10 F6 F12
DIV F14 F10 F16
ADDI F18 F14 4 
SUBI F20 F18 4
BNEZ F22 10 



*/

