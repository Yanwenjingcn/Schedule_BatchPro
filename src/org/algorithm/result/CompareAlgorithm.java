package org.algorithm.result;

import org.generate.BuildParameters;
import org.schedule.FillbacknewWithoutInsert;
import org.schedule.Makespan;
import org.schedule.fillbacknew;

public class CompareAlgorithm {

	/**
	 * @throws Throwable
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws Throwable {

		 dagAverageSize();
		 dagLevelFlag();
		 processorNumber();
		 taskAverageLength();
		 deadLineTimes();


	}



	public static void deadLineTimes() throws Throwable {
		// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
		// String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

		double[] deadLineTimes = { 1.05,1.1, 1.2};

		for (int i = 0; i < deadLineTimes.length; i++) {
			BuildParameters.setDeadLineTimes(deadLineTimes[i]);

			String basePathXML = "G:\\DagCasesXML\\deadLineTimes" + deadLineTimes[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\deadLineTimes" + deadLineTimes[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			BuildParameters.setDeadLineTimes(1.1);

		}
	}

	public static void taskAverageLength() throws Throwable {
		// �����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
		// String taskAverageLength =
		// String.valueOf(BuildParameters.taskAverageLength);

		int[] taskAverageLength = { 20,  40, 60 };
		//int[] taskAverageLength = {60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			BuildParameters.setTaskAverageLength(taskAverageLength[i]);

			String basePathXML = "G:\\DagCasesXML\\taskAverageLength" + taskAverageLength[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\taskAverageLength" + taskAverageLength[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			BuildParameters.setTaskAverageLength(40);
		}
	}

	public static void processorNumber() throws Throwable {
		// ����Ԫ�ĸ�����2,4,8,16,32��String processorNumber =
		// String.valueOf(BuildParameters.processorNumber);

		int[] processorNumber = { 4, 8, 16 };
		for (int i = 0; i < processorNumber.length; i++) {
			BuildParameters.setProcessorNumber(processorNumber[i]);

			String basePathXML = "G:\\DagCasesXML\\processorNumber" + processorNumber[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\processorNumber" + processorNumber[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			BuildParameters.setProcessorNumber(8);
		}
	}

	public static void dagLevelFlag() throws Throwable {
		int[] dagLevelFlag = { 1, 2, 3 };

		for (int i = 0; i < dagLevelFlag.length; i++) {
			BuildParameters.setDagLevelFlag(dagLevelFlag[i]);

			String basePathXML = "G:\\DagCasesXML\\dagLevelFlag" + dagLevelFlag[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\dagLevelFlag" + dagLevelFlag[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			BuildParameters.setDagLevelFlag(2);
		}
	}

	public static void dagAverageSize() throws Throwable {
		int[] dagAverageSize = { 20,  40, 60};
		for (int i = 0; i < dagAverageSize.length; i++) {

			BuildParameters.setDagAverageSize(dagAverageSize[i]);
			// XML�ļ����õ�λ��
			String basePathXML = "G:\\DagCasesXML\\dagAverageSize" + dagAverageSize[i] + "\\";

			// ��������λ��
			String resultPath = "G:\\DagCasesResult\\dagAverageSize" + dagAverageSize[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			BuildParameters.setDagAverageSize(40);
		}
	}

	public static void runAlgorithm(String basePathXML, String resultPath) throws Throwable {

		// ÿ���������100�Σ�100�εĽ�������һ���ļ��ͺá�

		for (int i = 0; i < 100; i++) {
			Makespan ms = new Makespan();
			fillbacknew fb = new fillbacknew();

			String pathXML = basePathXML;

			pathXML = basePathXML + i + "\\";

			ms.runMakespan_xml(pathXML, resultPath);
			fb.runMakespan(pathXML, resultPath);

		}

	}


}
