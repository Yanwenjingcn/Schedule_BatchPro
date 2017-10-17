package org.schedule;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
* @ClassName: DAGMap
* @Description: һ����DAG��������Ϣ
* @author YanWenjing
* @date 2017-9-14 ����4:30:39
 */
public class DAGMap { //һ��������

	public boolean fillbackpass = false;
	
	public boolean fillbackdone = false;	//DAG���������һ��DAG���Ƿ񱻻���
	
	public int DAGId;	//DAG���������һ��DAG����id���
	
	public int tasknumber;	//DAG���������һ��DAG����������Ŀ
	
	public int DAGdeadline;	//DAG���������һ��DAG���Ľ�ֹʱ��
	
	public int submittime;	//DAG���������һ��DAG������ʱ��
	
	public ArrayList<DAG> TaskList;	//DAG_queue_personal
		
	public HashMap<Integer,Integer> DAGDependMap;	//DAGDependMap_personal
	
	public HashMap<String,Double> DAGDependValueMap;	//DAGDependValueMap_personal
	
	public ArrayList<DAG> orderbystarttime;	//����fillbackstarttime��С�������к�������б�
	
	public HashMap<Integer,ArrayList> taskinlevel;	//��ͬ�㼶���������ͬһ��list��
	
	
	
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
	* @Description: �ж���������֮���Ƿ�������
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
