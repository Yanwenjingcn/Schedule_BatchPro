package org.schedule;

import java.util.Map;

/**
 * 
* @ClassName: computerability
* @Description: 各个DAG的子任务在各个处理器上的执行总花销一级平均花销
* @author YanWenjing
* @date 2017-9-12 下午4:36:25
 */
public class computerability {

	private static Map<Integer,int[]> ComputeCostMap;

	private static Map<Integer,Integer> AveComputeCostMap;
	

	public void setComputeCostMap(Map cch){
		this.ComputeCostMap = cch;
	}

	public static int getComputeCost(int Id,int peId){
		return ComputeCostMap.get(Id)[peId];
	}
	
	public void setAveComputeCostMap(Map acch){
		this.AveComputeCostMap = acch;
	}

	//返回各个处理器的平均处理能力
	public static int getAveComputeCost(int Id){
		return 1;
	}
}
