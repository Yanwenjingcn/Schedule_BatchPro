package org.schedule;

import java.util.ArrayList;
import org.generate.BuildParameters;

public class FIFO {
	
	int dagnummax = 10000;
	int mesnum = 5;
	public static int tasknum;
	public static int[][] message;
	//0-dagid 1-taskid 2-peid 3-starttime 4-finishtime
	
	public static ArrayList<DAG> dag_queue;
	public static ArrayList<DAG> dag_queue_ori;
	public static ArrayList<DAG> readyqueue;
	public static int course_number; //子任务数

	public static int current_time;
	public static int T = 1;
	
	public static ArrayList<PE> pe_list;
	
	public static DAGdepend dagdepend;
	
	public static int[][] petimelist;
	public static int[] petimes;
	
	public static int pe_number;
	public static int proceesorEndTime = BuildParameters.timeWindow;//时间窗
	public static int timeWindow;
	
	public FIFO(int PEnumber){
		dagdepend = new DAGdepend();
		dag_queue = new ArrayList<DAG>();
		dag_queue_ori = new ArrayList<DAG>();
		readyqueue = new ArrayList<DAG>();
		course_number = 0;
		current_time = 0;
		petimelist = new int[PEnumber][2000];
		petimes = new int[PEnumber];
		timeWindow = proceesorEndTime/PEnumber;
		message = new int[dagnummax][mesnum];
	}
	
    /**
     * 判断某一子任务是否达到就绪状态
     *
     * @param dag，要判断的子任务dag
     * @param dagdepand，该子任务所在的DAG的子任务间依赖关系
     * @param current，当前时刻
     * @return isready，该子任务dag是否可以加入readyList
     */
	private static boolean checkready(DAG dag,ArrayList<DAG> queue1,DAGdepend dagdepend,int current){
		
		boolean isready = true;
		
		if(dag.getpass() == false && dag.getdone() == false)
		{
			if(current >= dag.getdeadline())
			{
				dag.setpass(true);
			}
			if(dag.getstart()==0 && dag.getpass() == false)
			{
				ArrayList<DAG> pre_queue = new ArrayList<DAG>();
				ArrayList<Integer> pre = new ArrayList<Integer>(); 
				pre = dag.getpre();
				if(pre.size()>=0)
				{
					for(int j = 0;j<pre.size();j++)
					{
						DAG buf3 = new DAG();
						buf3 = getDAGById(pre.get(j));
						pre_queue.add(buf3);
					
						if(buf3.getpass())
						{
							dag.setpass(true);
							isready = false;
							break;
						}
					
						if(!buf3.done)
						{
							isready = false;
							break;
						}
						
					}
				}

			}
		}
		
		return isready;
	}
	
    /**
     * 计算本算法的makespan
     *
     * @param PEnumber，处理器资源个数
     * @return temp，用于计算处理器资源利用率和任务完成率
     */
	public static int[] makespan(int PEnumber) throws Throwable{
		
		tasknum = dag_queue_ori.size();
		
		pe_number = PEnumber;
		for(DAG dag_:dag_queue_ori)
		{
			DAG dag_new = new DAG();
			dag_new.setarrive(dag_.getarrive());
			dag_new.setdeadline(dag_.getdeadline());
			dag_new.setid(dag_.getid());
			dag_new.setlength(dag_.getlength());
			dag_new.setpre(dag_.getpre());
			dag_new.setsuc(dag_.getsuc());
			dag_new.setislast(dag_.getislast());
			dag_new.setdagid(dag_.getdagid());
			dag_queue.add(dag_new);
		}
		sort(dag_queue,course_number);

		for(int i = 0;i<pe_list.size();i++)
		{
			if(petimes[i]==0)
				petimelist[i][0] = 0;
		}
		
		while(current_time <= timeWindow)
		{
			for(DAG dag:dag_queue)
			{
				if((dag.getstart()+dag.getts()) == current_time && dag.getready() && dag.getdone() == false && dag.getpass() == false)
				{
					dag.setfinish(current_time);
					dag.setdone(true);
					pe_list.get(dag.getpeid()).setfree(true);
					//System.out.println("the task " + dag.getid() +" in DAG "+dag.getdagid()+" : start at "+ dag.getstart() +" finish at "+ dag.getfinish() + " at PE"+ dag.getpeid());
				}
			}
			
			for(DAG dag:dag_queue)
			{
				if(dag.getarrive() <= current_time && dag.getdone() == false && dag.getready() == false && dag.getpass() == false)
				{
					boolean ifready = checkready(dag,dag_queue,dagdepend,current_time);
					if(ifready)
					{
						dag.setready(true);
						readyqueue.add(dag);
					}
				}

			}
			
			schedule(dag_queue,dagdepend,current_time);
			
			for(DAG dag:dag_queue)
			{
				
				if(dag.getstart() == current_time && dag.getready() && dag.getdone() == false && dag.getpass() == false)
				{
					if(dag.getdeadline() > current_time)
					{
						if(dag.getts() == 0)
						{
							dag.setfinish(current_time);
							dag.setdone(true);
							current_time = current_time -T;
						}
						else
						{
							pe_list.get(dag.getpeid()).setfree(false);
							pe_list.get(dag.getpeid()).settask(dag.getid());
						}
					}	
					else
					{
						dag.setpass(true);
					}
				}
				
			}
			current_time = current_time + T;
		}
		
		int temp[] = new int[pe_number+3];
		temp = storeresult();

		return temp;
		
	}
	
	/**
     * 保存本算法的各个任务的开始结束时间
     *
     * @return temp，用于计算处理器资源利用率和任务完成率
     */
	public static int[] storeresult()
	{
		int temp[] = new int[pe_number+3];
		int tempp = 0;
		temp[0] = current_time-T;
		temp[pe_number+2] = 0;

		for(DAG dag_temp:dag_queue)
		{
			for(int q = 1;q<1+pe_number;q++)
			{
				if(dag_temp.getpeid() == (q-1) && dag_temp.getdone())
				{
					temp[q] = temp[q]+dag_temp.getts();
					break;
				}
			}
		}
		
		for(DAG dag:dag_queue)
		{
			if(dag.islast == true)
			{
				if(dag.done == true)
				{
					temp[pe_number+1]++;
					
					for(DAG dag_temp:dag_queue)
					{
						if(dag_temp.getdagid() == dag.getdagid())
							temp[pe_number+2] = temp[pe_number+2] + dag_temp.getts();
					}
				}
			}
		}
		
		int dagcount = 0;
		for(DAG dag:dag_queue)
		{
			message[dagcount][0] = dag.getdagid();
			message[dagcount][1] = dag.getid();
			message[dagcount][2] = dag.getpeid();
			message[dagcount][3] = dag.getstart();
			message[dagcount][4] = dag.getfinish();
			dagcount++;
		}

		return temp;
	}
	
	/**
     * 调度readyList
     *
     * @param queue1，readyList
     * @param dagdepand，该子任务所在的DAG的子任务间依赖关系
     * @param current，当前时刻
     */			
	private static void schedule(ArrayList<DAG> queue1,DAGdepend dagdepend,int current){

		ArrayList<DAG> buff = new ArrayList<DAG>();
		DAG min = new DAG();
		DAG temp = new DAG();
		for(int k = 0 ; k < readyqueue.size() ; k++)
		{
			int tag = k;
			min = readyqueue.get(k);
			temp = readyqueue.get(k);
			for(int p = k+1 ; p < readyqueue.size() ; p++ )
			{
				if(readyqueue.get(p).getarrive() < min.getarrive() )
				{
					min = readyqueue.get(p);
					tag = p;
				}
			}
			if(tag != k)
			{
				readyqueue.set(k, min);
				readyqueue.set(tag, temp);
			}
		}
		
		for(int i = 0;i<readyqueue.size();i++)
		{
			DAG buf1 = new DAG();
			buf1 = readyqueue.get(i);
			
			for(DAG dag:dag_queue)
			{
				if(buf1.getid() == dag.getid())
				{
					choosePE(dag);
					break;
				}
			}

		}

		readyqueue.clear();
			
	}

	/**
     * 为子任务选择处理器，选择可以最早开始处理的PE
     *
     * @param dag_temp，要选择处理器的子任务
     */
	private static void choosePE(DAG dag_temp){
		
		ArrayList<DAG> pre_queue = new ArrayList<DAG>();
		ArrayList<Integer> pre = new ArrayList<Integer>(); 
		pre = dag_temp.getpre();
		if(pre.size()>=0)
		{
			for(int j = 0;j<pre.size();j++)
			{
				DAG buf = new DAG();
				buf = getDAGById(pre.get(j));
				pre_queue.add(buf);
			}
		}
		
		int temp[] = new int[pe_list.size()];
		for(int i=0;i<pe_list.size();i++)
		{
			if(pre_queue.size() == 0)
			{
				if(current_time>petimelist[i][petimes[i]])
					temp[i] = current_time;
				else
					temp[i] = petimelist[i][petimes[i]];
			}
			else if(pre_queue.size() == 1)
			{
				if(pre_queue.get(0).getpeid() == pe_list.get(i).getID())
				{
					if(current_time>petimelist[i][petimes[i]])
						temp[i] = current_time;
					else
						temp[i] = petimelist[i][petimes[i]];
				}
				else
				{
					int value = (int)dagdepend.getDependValue(pre_queue.get(0).getid(),dag_temp.getid());
					if((pre_queue.get(0).getfinish()+value) > petimelist[i][petimes[i]] && (pre_queue.get(0).getfinish()+value) > current_time)
						temp[i] = pre_queue.get(0).getfinish() + value;
					else if(current_time>(pre_queue.get(0).getfinish()+value) && current_time>petimelist[i][petimes[i]])
						temp[i] = current_time;
					else
						temp[i] = petimelist[i][petimes[i]];
				}
			}
			else
			{
				int max = current_time;
				for(int j=0;j<pre_queue.size();j++)
				{
					if(pre_queue.get(j).getpeid() == pe_list.get(i).getID())
					{
						if(max < petimelist[i][petimes[i]])
							max = petimelist[i][petimes[i]];
					}
					else
					{
						int value = pre_queue.get(j).getfinish()+(int)dagdepend.getDependValue(pre_queue.get(j).getid(),dag_temp.getid());
						if(value <= petimelist[i][petimes[i]])
						{
							if(max <petimelist[i][petimes[i]])
								max = petimelist[i][petimes[i]];
						}
						else
						{
							if(max < value)
								max = value;
						}
					}
				}
				temp[i] = max;
			}
		}		
		
		int min = 300000;
		int minpeid = 0;
		for(int i=0;i<pe_list.size();i++){
			if(min > temp[i])
				{
					min = temp[i];
					minpeid = i;
				}
		}
	
		if(min < dag_temp.getdeadline())
		{
			dag_temp.setpeid(minpeid);
			dag_temp.setts(dag_temp.getlength()/pe_list.get(minpeid).getability());
			dag_temp.setstart(min);
			dag_temp.setfinish_suppose(dag_temp.getstart()+dag_temp.getts());
			petimes[minpeid]++;
			petimelist[minpeid][petimes[minpeid]] = dag_temp.getfinish_suppose();
			//System.out.println("TASK"+dag_temp.getid()+" statrtime:" + dag_temp.getstart()+" "+dag_temp.getfinish_suppose());
		}
		else
		{
			dag_temp.setpass(true);
			//System.out.println(current_time+" "+dag_temp.Deadline);
		}

	}
	
	private static void sort(ArrayList<DAG> ready_queue,int course_num) throws Throwable{
		ArrayList<DAG> buff = new ArrayList<DAG>();
		DAG min = new DAG();
		DAG temp = new DAG();
		for(int i = 0 ; i<course_num ; i++)
		{
			int tag = i;
			min = ready_queue.get(i);
			temp = ready_queue.get(i);
			for(int j = i+1 ; j<course_num ; j++ )
			{
				if(ready_queue.get(j).getarrive() < min.getarrive() )
				{
					min = ready_queue.get(j);
					tag = j;
				}
			}
			if(tag != i)
			{
				ready_queue.set(i, min); 
				ready_queue.set(tag, temp);
			}
		}
	}
	
	private static DAG getDAGById(int dagId){
		for(DAG dag:dag_queue){
			if(dag.getid() == dagId)
				return dag;
		}
		return null;
	}
}
