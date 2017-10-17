package org.generate;

/**
 * 
* @ClassName: BuildParameters 
* @Description: ����DAGͼ���ɵ�Ĭ�ϲ���
* @author YWJ
* @date 2017-9-9 ����3:23:04
 */
public class BuildParameters {
      public static int timeWindow=40000;//ʱ�䴰 Ĭ��40000
      public static int taskAverageLength=40;//DAG������ƽ������ATL�������ƽ�����ȣ�20��40��60 Ĭ��ֵ40��
      public static int dagAverageSize=40;//DAGƽ�����������ADS��dag��ƽ����С��20��40��60 Ĭ��ֵ40��
      public static int dagLevelFlag=2;//DAG���жȵȼ���(1,2,3)����([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
      public static double deadLineTimes=1.1;//deadline�ı���ֵ ��1.05��1.1��1,2��
      public static int processorNumber=8;//����Ԫ�ĸ�����4,8,16��
      
	public static int getTimeWindow() {
		return timeWindow;
	}
	public static void setTimeWindow(int timeWindow) {
		BuildParameters.timeWindow = timeWindow;
	}
	public static int getTaskAverageLength() {
		return taskAverageLength;
	}
	public static void setTaskAverageLength(int taskAverageLength) {
		BuildParameters.taskAverageLength = taskAverageLength;
	}
	public static int getDagAverageSize() {
		return dagAverageSize;
	}
	public static void setDagAverageSize(int dagAverageSize) {
		BuildParameters.dagAverageSize = dagAverageSize;
	}
	public static int getDagLevelFlag() {
		return dagLevelFlag;
	}
	public static void setDagLevelFlag(int dagLevelFlag) {
		BuildParameters.dagLevelFlag = dagLevelFlag;
	}
	public static double getDeadLineTimes() {
		return deadLineTimes;
	}
	public static void setDeadLineTimes(double deadLineTimes) {
		BuildParameters.deadLineTimes = deadLineTimes;
	}
	public static int getProcessorNumber() {
		return processorNumber;
	}
	public static void setProcessorNumber(int processorNumber) {
		BuildParameters.processorNumber = processorNumber;
	}
      
      //public int proceesorEndTime = timeWindow/processorNumber;
      
      
      
}
