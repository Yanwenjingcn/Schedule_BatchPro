package org.dagcase.generate;

import java.io.File;

import org.generate.BuildParameters;
import org.generate.DagBuilder;

public class GenerateDags {

	static File fileXML;
	static File fileTXT;
	static File fileResult;

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) {
		
		/**
		 *       public static int timeWindow=40000;//时间窗 默认40000
      public static int taskAverageLength=40;//DAG子任务平均长度ATL：任务的平均长度（20，40，60 默认值40）
      public static int dagAverageSize=40;//DAG平均子任务个数ADS：dag的平均大小（20，40，60 默认值40）
      public static int dagLevelFlag=2;//DAG串行度等级：(1,2,3)代表([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
      public static double deadLineTimes=1.1;//deadline的倍数值 （1.05，1.1，1,2）
      public static int processorNumber=8;//处理单元的个数（4,8,16）
		 */

		int[] dagAverageSize = { 20,40,60 };
		for (int i = 0; i < dagAverageSize.length; i++) {
			BuildParameters.setDagAverageSize(dagAverageSize[i]);
			
			String basePathXML = "G:\\DagCasesXML\\dagAverageSize"+ dagAverageSize[i] + "\\";
			String basePathTXT = "G:\\DagCasesTXT\\dagAverageSize"+ dagAverageSize[i] + "\\";
			
			cycle(basePathXML, basePathTXT);
			BuildParameters.setDagAverageSize(40);
		}
		
		System.out.println("=====================================================>");
		
		int[] dagLevelFlag = { 1,2,3 };
		for (int i = 0; i < dagLevelFlag.length; i++) {
			BuildParameters.setDagLevelFlag(dagLevelFlag[i]);

			String basePathXML = "G:\\DagCasesXML\\dagLevelFlag"+ dagLevelFlag[i] + "\\";
			String basePathTXT = "G:\\DagCasesTXT\\dagLevelFlag"+ dagLevelFlag[i] + "\\";
			cycle(basePathXML, basePathTXT);
			BuildParameters.setDagLevelFlag(2);
		}
		
		
		
		System.out.println("=====================================================>");

		int[] processorNumber = { 4,8,16 };
		for (int i = 0; i < processorNumber.length; i++) {
			BuildParameters.setProcessorNumber(processorNumber[i]);
			
			String basePathXML = "G:\\DagCasesXML\\processorNumber"+ processorNumber[i] + "\\";
			String basePathTXT = "G:\\DagCasesTXT\\processorNumber"+ processorNumber[i] + "\\";
			cycle(basePathXML, basePathTXT);
			BuildParameters.setProcessorNumber(8);
		}
		
		
		
		System.out.println("=====================================================>");

		// 任务的平均长度（20,30,40,50 默认值30）
		//String taskAverageLength = String.valueOf(BuildParameters.taskAverageLength);
		
		int[] taskAverageLength = { 20,40,60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			BuildParameters.setTaskAverageLength(taskAverageLength[i]);

			String basePathXML = "G:\\DagCasesXML\\taskAverageLength"+ taskAverageLength[i] + "\\";
			String basePathTXT = "G:\\DagCasesTXT\\taskAverageLength"+ taskAverageLength[i] + "\\";
			
			cycle(basePathXML, basePathTXT);
			
			BuildParameters.setTaskAverageLength(40);
		}
		
		
		
		
		// deadline的倍数值 （1.1，1.3，1.6，2.0）
		//String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

		System.out.println("=====================================================>");
		double[] deadLineTimes = {1.05,1.1,1.2};
		
		for (int i = 0; i < deadLineTimes.length; i++) {
			BuildParameters.setDeadLineTimes(deadLineTimes[i]);

			String basePathXML = "G:\\DagCasesXML\\deadLineTimes"+ deadLineTimes[i] + "\\";
			String basePathTXT = "G:\\DagCasesTXT\\deadLineTimes"+ deadLineTimes[i] + "\\";
			cycle(basePathXML, basePathTXT);
			
			BuildParameters.setDeadLineTimes(1.1);
		}
	}

	/**
	 * 
	 * @Title: cycle
	 * @Description: 穿入路径然后循环100次
	 * @param @param basePathXML
	 * @param @param basePathTXT
	 * @return void
	 * @throws
	 */

	public static void cycle(String basePathXML, String basePathTXT) {


		for (int i = 0; i < 100; i++) {
			String pathXML = basePathXML + i + "\\";
			String pathTXT = basePathTXT + i + "\\";
			

			fileXML = new File(pathXML);
			fileTXT = new File(pathTXT);
			
			fileXML.mkdirs();
			fileTXT.mkdirs();
			

			DagBuilder dagbuilder = new DagBuilder(i, pathXML, pathTXT);
			dagbuilder.BuildDAG(i, pathXML, pathTXT);
		}
	}

}
