package org.algorithm.result;

import java.io.File;
import java.io.IOException;

import org.generate.BuildParameters;
import org.generate.DagBuilder;

/**
 * 
* @ClassName: ResultDirGenerate
* @Description: ���ɽ���ļ���
* @author YanWenjing
* @date 2017-9-22 ����1:06:27
 */
public class ResultDirGenerate {


	static File fileResult;
	static File resultTxt;
	
	static String fifoResult="fifo.txt";
	static String edfResult="edf.txt";
	static String stfResult="stf.txt";
	static String etfResult="etf.txt";
	static String lrebResult="lreb.txt";
	
	

	/**
	 * @throws IOException 
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws IOException {

		int[] dagAverageSize = { 20, 40, 60 };
		for (int i = 0; i < dagAverageSize.length; i++) {
			BuildParameters.setDagAverageSize(dagAverageSize[i]);
			String basePathResult="G:\\DagCasesResult\\dagAverageSize"+ dagAverageSize[i] + "\\";
			cycle(basePathResult);

		}
		
		
		int[] dagLevelFlag = { 1,2,3 };
		for (int i = 0; i < dagLevelFlag.length; i++) {
			BuildParameters.setDagLevelFlag(dagLevelFlag[i]);
			String basePathResult="G:\\DagCasesResult\\dagLevelFlag"+ dagLevelFlag[i] + "\\";
			cycle(basePathResult);
		}
		

		
		// ����Ԫ�ĸ�����2,4,8,16,32��String processorNumber = String.valueOf(BuildParameters.processorNumber);
		int[] processorNumber = {4,8,16 };
		for (int i = 0; i < processorNumber.length; i++) {
			BuildParameters.setProcessorNumber(processorNumber[i]);
			String basePathResult="G:\\DagCasesResult\\processorNumber"+ processorNumber[i] + "\\";
			cycle(basePathResult);
		}
		

		// �����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
		//String taskAverageLength = String.valueOf(BuildParameters.taskAverageLength);
		
		int[] taskAverageLength = { 20, 40, 60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			BuildParameters.setTaskAverageLength(taskAverageLength[i]);
			
			String basePathResult="G:\\DagCasesResult\\taskAverageLength"+ taskAverageLength[i] + "\\";
			cycle(basePathResult);
		}
		
		
		
		
		// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
		//String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

	
		double[] deadLineTimes = {1.05,1.1,1.2 };
		
		for (int i = 0; i < deadLineTimes.length; i++) {
			BuildParameters.setDeadLineTimes(deadLineTimes[i]);
			
			String basePathResult="G:\\DagCasesResult\\deadLineTimes"+ deadLineTimes[i] + "\\";
			cycle(basePathResult);
			
		}
		
		System.out.println("ִ�н���");
	}


	/**
	 * @throws IOException 
	 * 
	* @Title: cycle
	* @Description: TODO
	* @param @param basePathResult
	* @return void
	* @throws
	 */
	public static void cycle(String basePathResult) throws IOException {

		String pathResult = basePathResult;
		fileResult=new File(pathResult);
		fileResult.mkdirs();
		
		
		resultTxt=new File(basePathResult+fifoResult);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+edfResult);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+stfResult);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+etfResult);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+lrebResult);
		resultTxt.createNewFile();
		
	}

}
