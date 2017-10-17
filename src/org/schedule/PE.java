package org.schedule;

import java.util.List;

public class PE {
	public int ability;	//处理器计算能力
	
	public int ID;	//处理器的ID
	
	public int task;	//该处理器不为空闲时正在处理的任务编号
	
	boolean free;	//处理器是否空闲
	
	public int busytime;	//
	
	private double avail;	//处理器开始空闲的时间点

	private static List<double[]>availPeriod;	//

	private double[] ast;	//处理器所执行的第i个任务的最早开始时间
	
	private double[] aft;	//处理器所执行的第i个任务的最早结束时间
	
	public PE(){
		ast=new double[100];
		aft=new double[100];
	}
	
	public void setID(int id){
		this.ID = id;
	}
	
	public int getID(){
		return ID;
	}
	
	public void setbusytime(int busytime_){
		this.busytime = busytime_;
	}
	
	public int getbusytime(){
		return busytime;
	}
	
	public void setability(int temp){
		this.ability = temp;
	}
	
	public int getability(){
		return ability;
	}
	
	public void setfree(boolean temp){
		this.free = temp;
	}
	
	public boolean getfree(){
		return free;
	}
	
	public void settask(int task){
		this.task = task;
	}
	
	public int gettask(){
		return task;
	}
	
	public void setAvail(double avail){
		this.avail = avail;
	}
	
	public double getAvail(){
		return avail;
	}
	
	public void setast(int num , double ast){
		this.ast[num] = ast;
	}
	
	public double getast(int num){
		return ast[num];
	}

	public void setaft(int num , double aft){
		this.aft[num] = aft;
	}
	
	public double getaft(int num){
		return aft[num];
	}
}
