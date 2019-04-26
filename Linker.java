/**
 *
 * @author Connie Guan
 *
 */


import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


/**
 *Two Pass Linker
 *
 *First pass: finds base address for each module and produces symbol table.
 *Second pass: relocates relative addresses and resolves external references. It produces a memory map.
 *
 */
public class Linker {
    
    
    public static void main(String[] args){
        
        Scanner pass1 = new Scanner(System.in);
       
        //****************PASS ONE****************
        //find base address for each module and produce symbol table
        
        //variables
        int numOfMods = pass1.nextInt();
        int numOfDefs = 0; //number of definitions in module
        int numOfUses = 0; //number of uses in module
        int baseAddress = 0;
        int definedAt = 0; //relative address of definition
        String symbol = null;
        int moduleSize = 0; //number of addresses
        int usedAt = 0; //relative address of use
        String type = null;
        int relativeAdd = 0;
        
        //error variables
        boolean isMultiplyDefined = false;
        
        //lists
        ArrayList<String> definitions = new ArrayList<String>();
        ArrayList<Integer> definitionsAddress = new ArrayList<Integer>();
        ArrayList<Integer> numberOfUses = new ArrayList<Integer>();
        ArrayList<String> allUsed = new ArrayList<String>(); //list of symbols used, including repetitions
        ArrayList<Integer> allUsedAddresses = new ArrayList<Integer>();
        ArrayList<Integer> moduleSizes = new ArrayList<Integer>(); //list of module sizes per module
        ArrayList<String> types = new ArrayList<String>(); //list of types per module
        ArrayList<Integer> relativeAddresses = new ArrayList<Integer>(); //list of text entries per module
        
        System.out.println();
        System.out.print("Symbol Table");
        
        //iterate through each module to get definition list
        for(int i = 0; i < numOfMods; i++){
            numOfDefs = pass1.nextInt();
            for(int j = 0; j < numOfDefs; j++){ //for each definition pair, get symbol and relative location
                symbol = pass1.next();
                definedAt = pass1.nextInt() + baseAddress;
                
                if(definitions.contains(symbol)){ //Error: Multiply defined
                    isMultiplyDefined = true;
                    int index = definitions.indexOf(symbol);
                    //remove previous definition, replace with current one
                    definitions.remove(index);
                    definitionsAddress.remove(index);
                }
                definitions.add(symbol);
                definitionsAddress.add(definedAt);
                
            }
            
            //get use list
            numOfUses = pass1.nextInt();
            numberOfUses.add(numOfUses);
            for(int k = 0; k<numOfUses; k++){
                symbol = pass1.next();
                usedAt = pass1.nextInt();
                allUsed.add(symbol);
                allUsedAddresses.add(usedAt);
            }
            
            //get text entries list
            moduleSize = pass1.nextInt();
            baseAddress+=moduleSize; //get new base address
            moduleSizes.add(moduleSize);
            for(int l = 0; l<moduleSize; l++){
                type = pass1.next();
                relativeAdd = pass1.nextInt();
                types.add(type);
                relativeAddresses.add(relativeAdd);
            }
        }
        
        //print symbol table
        for(int i = 0; i<definitions.size(); i++){
            System.out.println();
            System.out.print(definitions.get(i) + "=" + definitionsAddress.get(i));
            //check if multiply defined
            if(isMultiplyDefined){
                System.out.println(" Error: This symbol is multiply defined. Last value is used.");
            }
            isMultiplyDefined = false;
        }
        
        System.out.println();
        
        pass1.close();
        
        
        //****************PASS TWO****************
        //relocate relative addresses and resolve external references; produce memory map
        
        //variables
        int absoluteAddress = 0; //absolute address
        int symbolIndex = 0; //index used to locate relative address in symbol table of symbol being used
        baseAddress=0; //reset baseAddress
        int sum = 0;
        int sum2 = 0;
        int sum3=0;
        
        ArrayList<Integer> multipleUsedAddress = new ArrayList<Integer>();
        
        System.out.print("\nMemory Map");
        
        for(int i = 0; i < numOfMods; i++){
            
            int x = numberOfUses.get(i);
            
            sum+=x;
            
            //use list for current module
            ArrayList<String> used = new ArrayList<String>(); //list of symbols used, includes repetitions
            ArrayList<Integer> usedAddress = new ArrayList<Integer>(); //list of relative address of use of symbol

            //get use list
            for(int j = (sum-x); j<sum; j++){
                symbol = allUsed.get(j);
                usedAt = allUsedAddresses.get(j);
                
                //error: if multiple used
                if(usedAddress.contains(usedAt)){
                    multipleUsedAddress.add(usedAt+sum);
                    int index = usedAddress.indexOf(usedAt);
                    used.set(index, symbol);
                }
                
                used.add(symbol);
                usedAddress.add(usedAt);

            }
            
            //get text entries
            moduleSize = moduleSizes.get(i);
            sum2 += moduleSize;
            sum3= sum2-moduleSize;
            
            for(int l = (sum2-moduleSize); l<sum2; l++){
                
                int increment = l-sum3;
                
                //error variables
                boolean absAddExceeds = false;
                boolean typeRExceeds = false;
                boolean usedNotDefined = false;
                boolean multipleUsed = false;
                
                type = types.get(l);
                relativeAdd = relativeAddresses.get(l);
                
                //immediate
                if(type.equals("I")){
                    absoluteAddress = relativeAdd;
                    
                //absolute
                }else if(type.equals("A")){
                    //Error: if absolute address exceeds machine size
                    if(relativeAdd%1000 >= 300){
                        absAddExceeds = true;
                        relativeAdd = (int) (relativeAdd/1000 * 1000 + 299);
                    }
                    absoluteAddress = relativeAdd;
                }
                
                //relative
                else if(type.equals("R")){
                    //Error: if relative address exceeds machine size
                    if(relativeAdd%1000 > moduleSize+baseAddress){
                        typeRExceeds = true;
                        relativeAdd = (int) (relativeAdd/1000 * 1000);
                    }
                    absoluteAddress = relativeAdd + baseAddress;
                }
                
                //external
                else if (type.equals("E")){
                    int increment2 = increment;
                    int relAdd2 = 0;
                    int lastDigit = 0;
                    
                    //look for address that corresponds to current reference
                    while(!usedAddress.contains(increment)){
                        if(types.get(increment2+sum3).equals("E")){
                            relAdd2 = relativeAddresses.get(increment2+sum3);
                            lastDigit = relAdd2 % 10;
                        }
                        if(lastDigit == increment){
                            increment = increment2;
                        }
                        if(increment2 >= (moduleSize-1)){
                            increment2=0;
                        }else{
                            increment2++;
                        }
                        
                    }
                    symbolIndex = usedAddress.indexOf(increment);
                    symbol = used.get(symbolIndex);
                    
                    //error: if multiple used
                    if(multipleUsedAddress.contains(increment+sum3)){
                        multipleUsed=true;
                    }
                    //error: if used but not defined
                    if(!definitions.contains(symbol)){
                        relativeAdd = relativeAdd/1000 * 1000 + 111;
                        usedNotDefined=true;
                    }else{
                        relativeAdd = relativeAdd/1000 * 1000 + definitionsAddress.get(definitions.indexOf(symbol));
                    }
                    
                    absoluteAddress = relativeAdd;
                    
                }
                
                
                System.out.println();
                //print memory map
                System.out.print((l) + ": " + absoluteAddress);

                //error messages
                if(usedNotDefined){
                    System.out.print(" Error: symbol is not defined. 111 used.");
                }
                if(absAddExceeds){
                    System.out.print(" Error: type A address exceeds size of machine. Largest legal value used.");
                }
                if(typeRExceeds){
                    System.out.print(" Error: type R address exceeds size of module. Treated as 0 (relative).");
                }
                if(multipleUsed){
                    System.out.print(" Error: multiple symbols used here. Last one used.");
                }
                
            }
            baseAddress += moduleSize; //get new base address 
            
        }
        
        System.out.println();
        System.out.println();
        
        //warning: defined but not used
        for(int i = 0; i<definitions.size(); i++){
            if(!allUsed.contains(definitions.get(i))){
                System.out.println("Warning: "+definitions.get(i)+ " was defined but never used.");
                
            }
        }
        
    }
    
}
