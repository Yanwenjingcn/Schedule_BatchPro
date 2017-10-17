package org.algorithm.result;

import java.io.File;
import java.io.IOException;

import org.generate.BuildParameters;
import org.generate.DagBuilder;

/**
 * 
* @ClassName: ResultDirGenerateWithoutInsert 
* @Description: 产生最后一个对比实验的实验结果
* @author YWJ
* @date 2017-10-2 下午3:51:11
 */
public class ResultDirGenerateWithoutInsert {


	static File fileResult;
	static File resultTxt;
	

	static String lrebWithoutInsertResult="lrebWithoutInsert.txt";
	
	

	/**
	 * @throws IOException 
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws IOException {

		int[] dagAverageSize = { 20, 40, 60};
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
		

		
		// 处理单元的个数（2,4,8,16,32）String processorNumber = String.valueOf(BuildParameters.processorNumber);
		int[] processorNumber = { 4,8,16 };
		for (int i = 0; i < processorNumber.length; i++) {
			BuildParameters.setProcessorNumber(processorNumber[i]);
			String basePathResult="G:\\DagCasesResult\\processorNumber"+ processorNumber[i] + "\\";
			cycle(basePathResult);
		}
		

		// 任务的平均长度（20,30,40,50 默认值30）
		//String taskAverageLength = String.valueOf(BuildParameters.taskAverageLength);
		
		int[] taskAverageLength = {20, 40, 60};
		for (int i = 0; i < taskAverageLength.length; i++) {
			BuildParameters.setTaskAverageLength(taskAverageLength[i]);
			
			String basePathResult="G:\\DagCasesResult\\taskAverageLength"+ taskAverageLength[i] + "\\";
			cycle(basePathResult);
		}
		
		
		
		
		// deadline的倍数值 （1.1，1.3，1.6，2.0）
		//String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

	
		double[] deadLineTimes = {1.05,1.1,1.2 };
		
		for (int i = 0; i < deadLineTimes.length; i++) {
			BuildParameters.setDeadLineTimes(deadLineTimes[i]);
			
			String basePathResult="G:\\DagCasesResult\\deadLineTimes"+ deadLineTimes[i] + "\\";
			cycle(basePathResult);
			
		}
		
		System.out.println("执行结束");

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
		//fileResult=new File(pathResult);
		//fileResult.mkdirs();

		
		resultTxt=new File(basePathResult+lrebWithoutInsertResult);
		resultTxt.createNewFile();
		
	}

}
