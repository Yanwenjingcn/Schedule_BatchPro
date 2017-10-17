package org.generate;

/**
 * 
* @ClassName: BuildParameters 
* @Description: 构造DAG图生成的默认参数
* @author YWJ
* @date 2017-9-9 下午3:23:04
 */
public class BuildParameters {
      public static int timeWindow=40000;//时间窗 默认40000
      public static int taskAverageLength=40;//DAG子任务平均长度ATL：任务的平均长度（20，40，60 默认值40）
      public static int dagAverageSize=40;//DAG平均子任务个数ADS：dag的平均大小（20，40，60 默认值40）
      public static int dagLevelFlag=2;//DAG串行度等级：(1,2,3)代表([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
      public static double deadLineTimes=1.1;//deadline的倍数值 （1.05，1.1，1,2）
      public static int processorNumber=8;//处理单元的个数（4,8,16）
      
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
