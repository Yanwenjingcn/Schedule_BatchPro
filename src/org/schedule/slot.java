package org.schedule;

import java.util.ArrayList;

public class slot { //PE上空闲时间段
	
	public int PEId;	//处理器ID
	
	public int slotId;	//slot的ID
	
	public int slotstarttime;	//slot空闲开始的时间
	
	public int slotfinishtime;	//slot空闲结束的时间
	
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
