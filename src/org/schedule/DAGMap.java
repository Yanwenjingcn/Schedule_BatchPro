package org.schedule;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
* @ClassName: DAGMap
* @Description: 一整个DAG的总体信息
* @author YanWenjing
* @date 2017-9-14 下午4:30:39
 */
public class DAGMap { //一个工作流

	public boolean fillbackpass = false;
	
	public boolean fillbackdone = false;	//DAG（这是真的一个DAG）是否被回填
	
	public int DAGId;	//DAG（这是真的一个DAG）的id编号
	
	public int tasknumber;	//DAG（这是真的一个DAG）的任务数目
	
	public int DAGdeadline;	//DAG（这是真的一个DAG）的截止时间
	
	public int submittime;	//DAG（这是真的一个DAG）到达时间
	
	public ArrayList<DAG> TaskList;	//DAG_queue_personal
		
	public HashMap<Integer,Integer> DAGDependMap;	//DAGDependMap_personal
	
	public HashMap<String,Double> DAGDependValueMap;	//DAGDependValueMap_personal
	
	public ArrayList<DAG> orderbystarttime;	//按照fillbackstarttime从小到大排列后的任务列表
	
	public HashMap<Integer,ArrayList> taskinlevel;	//相同层级的任务放在同一个list中
	
	
	
	public DAGMap(){
		TaskList = new ArrayList<DAG>();
		orderbystarttime = new ArrayList<DAG>();
		DAGDependMap = new HashMap<Integer,Integer>();
		DAGDependValueMap = new HashMap<String,Double>();
		taskinlevel = new HashMap<Integer,ArrayList>();
	}
	
	/**
	 * 
	* @Title: isDepend
	* @Description: 判断两个任务之间是否有依赖
	* @param @param src
	* @param @param des
	* @param @return
	* @return boolean
	* @throws
	 */
	public boolean isDepend(String src,String des){
		if(DAGDependValueMap.containsKey(src+" "+des)){
			return true;
		}
		else return false;
	}
	
	
	
	
	public void setfillbackpass(boolean pass){
		this.fillbackpass = pass;
	}
	
	public boolean getfillbackpass(){
		return fillbackpass;
	}
	
	public void setfillbackdone(boolean done){
		this.fillbackdone = done;
	}
	
	public boolean getfillbackdone(){
		return fillbackdone;
	}
	
	public void settasklist(ArrayList<DAG> list){
		for(int i =0;i<list.size();i++)
			this.TaskList.add(list.get(i));
	}
	
	public ArrayList gettasklist(){
		return TaskList;
	}
	
	public void setorderbystarttime(ArrayList<DAG> list){
		for(int i =0;i<list.size();i++)
			this.orderbystarttime.add(list.get(i));
	}
	
	public ArrayList getorderbystarttime(){
		return orderbystarttime;
	}
	
	public void setdepandmap(HashMap<Integer,Integer> map){
		this.DAGDependMap = map;
	}
	
	public void setdependvalue(HashMap<String,Double> value){
		this.DAGDependValueMap = value;
	}
	
	public HashMap getdependvalue(){
		return DAGDependValueMap;
	}
	
	public void setDAGId(int id){
		this.DAGId = id;
	}
	
	public int getDAGId(){
		return DAGId;
	}
	
	public void settasknumber(int num){
		this.tasknumber = num;
	}
	
	public int gettasknumber(){
		return tasknumber;
	}
	
	public void setDAGdeadline(int deadline){
		this.DAGdeadline = deadline;
	}
	
	public int getDAGdeadline(){
		return DAGdeadline;
	}
	
	public void setsubmittime(int submit){
		this.submittime = submit;
	}
	
	public int getsubmittime(){
		return submittime;
	}
	
	
}
