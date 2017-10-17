package org.schedule;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 
* @ClassName: DAGdepend 
* @Description: 任务的依赖关系
* @author YWJ
* @date 2017-9-10 下午8:19:48
 */
public class DAGdepend { //一个DAG中的各个TASK间依赖关系，

	private List<DAG> DAGList;	//DAG列表
	
	private Map<Integer,Integer> DAGDependMap; //当前DAG下的各个子任务节点的依赖关系
	
	private Map<String,Double> DAGDependValueMap;	//当前DAG下的各个子任务节点的依赖关系的值

	public ArrayList<DAGMap> DAGMapList;
	
	
	

	public void setdagmaplist(ArrayList<DAGMap> list){
		this.DAGMapList = list;
	}
	
	public ArrayList getdagmaplist(){
		return DAGMapList;
	}
	
	/**
	 * 
	* @Title: isDepend 
	* @Description: 两者是否存在依赖   
	* @return boolean    
	* @throws
	 */
	public boolean isDepend(String src,String des){
		if(DAGDependValueMap.containsKey(src+" "+des)){
			return true;
		}
		else return false;
	}
	
	/**
	 * 
	* @Title: getDependValue 
	* @Description: 得到依赖值   ，就是两个DAG（任务）之间传输数据的值
	* @return double    
	* @throws
	 */
	public double getDependValue(int src,int des){
		return DAGDependValueMap.get(String.valueOf(src)+" "+String.valueOf(des));
	}
	

	public void setDAGList(List cl){
		this.DAGList = cl;
	}

	public List getDAGList(){
		return DAGList;		
	}
	

	public void setDAGDependMap(Map cd){
		this.DAGDependMap = cd;
	}

	public Map getDAGDependMap(){
		return DAGDependMap;
	}
	

	public void setDAGDependValueMap(Map cdv){
		this.DAGDependValueMap = cdv;
	}

	public Map getDAGDependValueMap(){
		return DAGDependValueMap;
	}

}
