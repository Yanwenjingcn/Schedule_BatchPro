package org.schedule;
import java.util.Comparator;

public class ComparatorDAGori implements Comparator {
	public int compare(Object arg0,Object arg1){
		DAG cloudlet1 = (DAG)arg0;
		DAG cloudlet2 = (DAG)arg1;
		if((cloudlet1.getid() - cloudlet2.getid()) > 0)
			return 1;
		else if((cloudlet1.getid() - cloudlet2.getid()) < 0)
			return -1;
		else return 0;
		
	}

}
