package org.schedule;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 
* @ClassName: DAGdepend 
* @Description: �����������ϵ
* @author YWJ
* @date 2017-9-10 ����8:19:48
 */
public class DAGdepend { //һ��DAG�еĸ���TASK��������ϵ��

	private List<DAG> DAGList;	//DAG�б�
	
	private Map<Integer,Integer> DAGDependMap; //��ǰDAG�µĸ���������ڵ��������ϵ
	
	private Map<String,Double> DAGDependValueMap;	//��ǰDAG�µĸ���������ڵ��������ϵ��ֵ

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
	* @Description: �����Ƿ��������   
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
	* @Description: �õ�����ֵ   ����������DAG������֮�䴫�����ݵ�ֵ
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
