package org.schedule;

import java.util.ArrayList;

public class slot { //PE�Ͽ���ʱ���
	
	public int PEId;	//������ID
	
	public int slotId;	//slot��ID
	
	public int slotstarttime;	//slot���п�ʼ��ʱ��
	
	public int slotfinishtime;	//slot���н�����ʱ��
	
	public ArrayList<String> below;
	
	public int inpe;
	
	public slot(){
		below = new ArrayList<String>();
	}
	
	public ArrayList<String> getbelow(){
		return below;
	}
	
	public void setbelow(ArrayList<String> below_){
		for(int i=0;i<below_.size();i++)
		{
			below.add(below_.get(i));
		}
	}
	
	public void setPEId(int pe){
		this.PEId = pe;
	}
	
	public int getPEId(){
		return PEId;
	}
	
	public void setslotId(int id){
		this.slotId = id;
	}
	
	public int getslotId(){
		return slotId;
	}
	
	public void setslotstarttime(int start){
		this.slotstarttime = start;
	}
	
	public int getslotstarttime(){
		return slotstarttime;
	}
	
	public void setslotfinishtime(int finish){
		this.slotfinishtime = finish;
	}
	
	public int getslotfinishtime(){
		return slotfinishtime;
	}
}
