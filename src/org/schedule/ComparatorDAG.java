package org.schedule;
import java.util.Comparator;

public class ComparatorDAG implements Comparator {
	public int compare(Object arg0,Object arg1){
		DAG cloudlet1 = (DAG)arg0;
		DAG cloudlet2 = (DAG)arg1;
		if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) > 0)
			return -1;
		else if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) < 0)
			return 1;
		else return 0;
		
	}

}