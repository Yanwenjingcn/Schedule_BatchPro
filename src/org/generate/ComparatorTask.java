package org.generate;

import java.util.Comparator;

import org.schedule.DAG;

public class ComparatorTask implements Comparator {
	public int compare(Object arg0,Object arg1){
		DAG task1 = (DAG)arg0;
		DAG task2 = (DAG)arg1;
		if((task1.getUpRankValue() - task2.getUpRankValue()) > 0)
			return -1;
		else if((task1.getUpRankValue() - task2.getUpRankValue()) < 0)
			return 1;
		else return 0;
		
	}

}