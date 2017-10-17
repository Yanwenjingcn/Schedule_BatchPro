package org.schedule;

import java.io.FileWriter;
import java.io.IOException;

public class PrintResult {
	
	static String fifoResult="fifo.txt";
	static String edfResult="edf.txt";
	static String stfResult="stf.txt";
	static String etfResult="etf.txt";
	static String lrebResult="lreb.txt";
	static String lrebWithoutInsertResult="lrebWithoutInsert.txt";

	public static void printToTxt(String[][] rate,String resultPath){

		FileWriter fifoResultWriter = null; 
		FileWriter edfResultWriter = null; 
		FileWriter stfResultWriter = null; 
		FileWriter etfResultWriter = null; 
		
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
    		String fifoResultFileName=resultPath+fifoResult;		
    		fifoResultWriter = new FileWriter(fifoResultFileName, true);     
    		fifoResultWriter.write(rate[0][0]+"\t"+rate[0][1]+"\t"+rate[0][2]+"\t"+rate[0][3]+"\n"); 
            
            
    		String edfResultFileName=resultPath+edfResult;		
    		edfResultWriter = new FileWriter(edfResultFileName, true);     
    		edfResultWriter.write(rate[1][0]+"\t"+rate[1][1]+"\t"+rate[1][2]+"\t"+rate[1][3]+"\n"); 
            

    		String stfResultFileName=resultPath+stfResult;		
    		stfResultWriter = new FileWriter(stfResultFileName, true);     
    		stfResultWriter.write(rate[2][0]+"\t"+rate[2][1]+"\t"+rate[2][2]+"\t"+rate[2][3]+"\n"); 
            

    		String etfResultFileName=resultPath+etfResult;		
    		etfResultWriter = new FileWriter(etfResultFileName, true);     
    		etfResultWriter.write(rate[3][0]+"\t"+rate[3][1]+"\t"+rate[3][2]+"\t"+rate[3][3]+"\n"); 
           
            
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(fifoResultWriter != null){  
                	fifoResultWriter.close();     
                }  
                if(edfResultWriter != null){  
                	edfResultWriter.close();     
                } 
                if(stfResultWriter != null){  
                	stfResultWriter.close();     
                } 
                if(etfResultWriter != null){  
                	etfResultWriter.close();     
                } 
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }   
		
	}
	
	
	public static void printLREBToTxt(String[][] rate,String resultPath){
		
		FileWriter lrebResultWriter = null; 

		
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
    		String lrebResultFileName=resultPath+lrebResult;		
    		lrebResultWriter = new FileWriter(lrebResultFileName, true);     
    		lrebResultWriter.write(rate[0][0]+"\t"+rate[0][1]+"\t"+rate[0][2]+"\t"+rate[0][3]+"\n"); 
          
            
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(lrebResultWriter != null){  
                	lrebResultWriter.close();     
                }  

            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }   
	}
	
	
	
	
public static void printLREBWithoutInsertToTxt(String[][] rate,String resultPath){
		
		FileWriter lrebWithoutInsertWriter = null; 

		
        try {     
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
    		String lrebWithoutInsertResultFileName=resultPath+lrebWithoutInsertResult;		
    		lrebWithoutInsertWriter = new FileWriter(lrebWithoutInsertResultFileName, true);     
    		lrebWithoutInsertWriter.write(rate[0][0]+"\t"+rate[0][1]+"\t"+rate[0][2]+"\t"+rate[0][3]+"\n"); 
          
            
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(lrebWithoutInsertWriter != null){  
                	lrebWithoutInsertWriter.close();     
                }  

            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }   
	}


}
