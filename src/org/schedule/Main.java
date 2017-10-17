package org.schedule;

import org.generate.DagBuilder;

public class Main {

	public static void main(String[] args) throws Throwable {
		//调用类生成测试DAG图
		//DagBuilder dagbuilder = new DagBuilder();
		//dagbuilder.BuildDAG();
		
		Makespan ms = new Makespan();
    	fillbacknew fb = new fillbacknew();
    	//ms.runMakespan_xml();
		//fb.runMakespan();
	}

}
