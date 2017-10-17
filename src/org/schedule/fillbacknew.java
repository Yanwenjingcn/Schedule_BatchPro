package org.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.jdom.xpath.XPath;
import org.jdom.Attribute;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.generate.BuildParameters;
import org.generate.DagBuilder;

public class fillbacknew {

	private static int[][] dagResultMap = null;
	private static ArrayList<PE> PEList;
	private static ArrayList<DAGMap> DAGMapList;

	private static ArrayList<DAG> DAG_queue;
	private static ArrayList<DAG> readyqueue;

	private static HashMap<Integer, Integer> DAGDependMap;
	private static HashMap<String, Double> DAGDependValueMap;

	private static ArrayList<DAG> DAG_queue_personal;
	private static HashMap<Integer, Integer> DAGDependMap_personal;
	private static HashMap<String, Double> DAGDependValueMap_personal;
	private static Map<Integer, int[]> ComputeCostMap;
	private static Map<Integer, Integer> AveComputeCostMap;

	private static Map<Integer, DAG> DAGIdToDAGMap;
	public static Map<Integer, double[]> DAGExeTimeMap;
	private static Map<Integer, Double> upRankValueMap;
	private static Map<Integer, double[]> vmComputeCostMap;
	private static Map<Integer, Double> vmAveComputeCostMap;
	private static Map<Integer, Integer[]> cloudletInVm;
	private static Map<Integer, Integer> cloudletInVmId;
	
	//д���ļ��Ľ��
	public static String[][] rateResult = new String[1][4];

	private static int islastnum = 0;
	private static double deadLineTimes = 1.3;// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
	private static int pe_number = 8;

	public static String[][] rate = new String[5][2];

	public static int current_time;
	public static int proceesorEndTime = BuildParameters.timeWindow;// ʱ�䴰
	public static int timeWindow;
	public static int T = 1;

	public static int fillbacktasknum = 10;
	public static int[][] message;
	public static int dagnummax = 10000;
	public static int timewindowmax = 9000000;
	public static int mesnum = 5;
	private static HashMap<Integer, ArrayList> SlotListInPes;
	private static HashMap<Integer, HashMap> TASKListInPes;
	
	
	//�������ϵĺ��Ʊ��
	private static int[] pushFlag;
	//��Ҫ���Ƶ�������
	private static int pushCount=0;
	//���Ƴɹ��ܴ���
	private static int pushSuccessCount=0;
	
	//��������Ŀ
	private static int taskTotal=0;
	
	

	public fillbacknew() {
		readyqueue = new ArrayList<DAG>();
		DAG_queue = new ArrayList<DAG>();
		DAG_queue_personal = new ArrayList<DAG>();
		PEList = new ArrayList<PE>();
		DAGMapList = new ArrayList<DAGMap>();
		DAGDependMap = new HashMap<Integer, Integer>();
		DAGDependValueMap = new HashMap<String, Double>();
		deadLineTimes = BuildParameters.deadLineTimes;
		pe_number = BuildParameters.processorNumber;
		current_time = 0;
		timeWindow = proceesorEndTime / pe_number;

		pushFlag=new int[pe_number];
		
		message = new int[dagnummax][mesnum];
		dagResultMap=new int[1000][dagnummax];
		
		
		SlotListInPes = new HashMap<Integer, ArrayList>();	//�����������ϵĿ��ж���Ϣ
		
		TASKListInPes = new HashMap<Integer, HashMap>();
		for (int i = 0; i < pe_number; i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKListInPes.put(i, TASKInPe);
			// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
		}
	}

	/**
	 * 
	* @Title: runMakespan
	* @Description: ��ʼfillback�㷨
	* @param @throws Throwable
	* @return void
	* @throws
	 */
	public void runMakespan(String pathXML,String resultPath) throws Throwable {
		
		// init dagmap
		fillbacknew fb = new fillbacknew();
		DAGdepend dagdepend = new DAGdepend();
		computerability vcc = new computerability();
		
		//��ʼ��������
		initPE();
		
		//��ʼ������xml
		initdagmap(dagdepend, vcc,pathXML);

		Date begin=new Date();
		Long beginTime=begin.getTime();
		// ���ȵ�һ��DAG����ʼ�������������ϵ�����ֲ������жΡ�����������ɳ����
		scheduleFirstDAG();
		
		//���õ�ǰʱ���ǵ�һ��DAG �ĵ���ʱ��
		current_time = DAGMapList.get(0).getsubmittime();

		for (int i = 1; i < DAGMapList.size(); i++) {
			
			HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();
			HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();

			computeSlot(DAGMapList.get(i).getsubmittime(), DAGMapList.get(i).getDAGdeadline());
			
			SlotListInPestemp = copySlot();
			TASKListInPestemp = copyTASK();

			scheduleOtherDAG(i, SlotListInPestemp, TASKListInPestemp);

		}

		
		Date end=new Date();
		Long endTime=end.getTime();
		
		Long diff=endTime-beginTime;
		
		// output the result
		outputresult(diff,resultPath);
		storeresult();

	}

	/**
	 * �����������Դ�����ʺ����������
	 */
	public static void outputresult(Long diff,String resultPath) {
		int suc = 0;
		int fault=0;
		int effective = 0;
		int tempp = timeWindow;

		for (int j = 0; j < DAGMapList.size(); j++) {
			
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++) {
				DAG dag_temp = (DAG) DAGMapList.get(j).gettasklist().get(i);
				DAGTaskList.add(dag_temp);
			}

			//��ȡ���ȳɹ���������Ŀ
			if (DAGMapList.get(j).getfillbackdone()) {
				System.out.println("dag"+DAGMapList.get(j).getDAGId()+"���ȳɹ����ɹ�ִ��");
				suc++;
				for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++) {
					effective = effective + DAGTaskList.get(i).getts();
				}
			}
			
			if(!DAGMapList.get(j).getfillbackdone()){
				System.out.println("dag"+DAGMapList.get(j).getDAGId()+"����ʧ�ܣ�ʧ��ִ�С�����");
				fault++;
			}
		}

		System.out.println("fillbackFIFO��"+suc+"������������");
		System.out.println("fillbackFIFO��"+fault+"���������ʧ�ܡ�����");
		
		DecimalFormat df = new DecimalFormat("0.0000");
		System.out.println("fillbackFIFO:");
		System.out.println("PE's use ratio is "+ df.format((float) effective / (pe_number * tempp)));
		System.out.println("effective PE's use ratio is "+ df.format((float) effective / (tempp * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) suc / DAGMapList.size()));
		System.out.println();
		
		
		rateResult[0][0] = df.format((float) effective / (pe_number * tempp));
		rateResult[0][1] = df.format((float) effective / (tempp * pe_number));
		rateResult[0][2] = df.format((float) suc / DAGMapList.size());
		rateResult[0][3] = df.format(diff);
		
		System.out.println("������Ƴɹ�����="+pushSuccessCount);
		System.out.println("��������="+taskTotal);
		int count=0;

		for(int k=0;k<dagResultMap.length;k++){
			for(int l=0;l<dagResultMap[k].length;l++){
				if(dagResultMap[k][l]==1){
					System.out.println("����dagid="+k+"������ı��="+l+"����ʧ��");
					count++;
				}
			}
		}
		
		
		System.out.println("count="+count);
		
		
		PrintResult.printLREBToTxt(rateResult,resultPath);
	}

	/**
	 * ���汾�㷨�ĸ�������Ŀ�ʼ����ʱ��
	 */
	public static void storeresult() {
		int dagcount = 0;
		for (DAGMap dagmap : DAGMapList) {
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				DAG dag = (DAG) dagmap.gettasklist().get(i);
				DAGTaskList.add(dag);
				
				message[dagcount][0] = dag.getdagid();
				message[dagcount][1] = dag.getid();
				message[dagcount][2] = dag.getfillbackpeid();
				message[dagcount][3] = dag.getfillbackstarttime();
				message[dagcount][4] = dag.getfillbackfinishtime();
				dagcount++;
			}
		}
	}


	
	/**
	 * 
	* @Title: computeSlot
	* @Description: ����relax�������¼������ʱ���SlotListInPes������SlotListInPes.put(i, slotListinpe)==��slotListinpe��������ɸѡ���ģ�ʱ������ƥ��submit----deadlineʱ��ε�slot�ļ���
	* @param @param submit��DAG�ύʱ��
	* @param @param deadline��DAG��ֹʱ��
	* @return void
	* @throws
	 */
	public static void computeSlot(int submit, int deadline) {

		SlotListInPes.clear();

		for (int i = 0; i < pe_number; i++) {
			//��ǰ�������Ͽ���Ƭ����������
			int Slotcount = 0;
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			ArrayList<slot> slotListinpe_ori = new ArrayList<slot>();

			if (TASKInPe.size() == 0) {//��ǰ��������û��ִ�й�����
				slot tem = new slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			} else if (TASKInPe.size() == 1) {//�ô�������ֻ��һ����������
				if (TASKInPe.get(0)[0] > submit) {//---submit---TASKInPe.get(0)[0]---
					slot tem = new slot();
					ArrayList<String> below_ = new ArrayList<String>();
					below_.add(TASKInPe.get(0)[2] + " " + TASKInPe.get(0)[3]+ " " + 0);
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					//����ʱ���  ---submit<--����ʱ��-->TASKInPe.get(0)[0]--
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(TASKInPe.get(0)[0]);
					tem.setbelow(below_);
					slotListinpe.add(tem);
					Slotcount++;
				}
				if (TASKInPe.get(0)[1] < deadline) {//----TASKInPe.get(0)[1]----deadline----
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					//����ʱ���  ---TASKInPe.get(0)[1]<--����ʱ��-->deadline--
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			} else {//�ô��������ж����������
				//��ȡ��������ԭ�����ڸ�������ִ�п���Ƭ��
				for (int j = 1; j < TASKInPe.size(); j++) {
					if (TASKInPe.get(j - 1)[1] < TASKInPe.get(j)[0]) {
						slot tem = new slot();
						ArrayList<String> below_ = new ArrayList<String>();
						for (int k = j; k < TASKInPe.size(); k++) {
							below_.add(TASKInPe.get(k)[2] + " "+ TASKInPe.get(k)[3] + " " + j);
						}
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						//����ʱ���  ---TASKInPe.get(j - 1)[1]<--����ʱ��-->TASKInPe.get(j)[0]--
						tem.setslotstarttime(TASKInPe.get(j - 1)[1]);
						tem.setslotfinishtime(TASKInPe.get(j)[0]);
						tem.setbelow(below_);
						slotListinpe_ori.add(tem);
						Slotcount++;
					}
				}

				//�����ɵ�ǰDAG����ʼslot�ı��
				int startslot = 0;
				for (int j = 0; j < slotListinpe_ori.size(); j++) {
					slot tem = new slot();
					tem = slotListinpe_ori.get(j);
					
					if (j == 0 && tem.getslotstarttime() > submit) {
						startslot = 0;
						break;
					}

					if (tem.getslotstarttime() <= submit //--slotstarttime--submit--slotfinishtime--
							&& tem.getslotfinishtime() > submit) {
						tem.setslotstarttime(submit);
						startslot = j;
						break;
					} else if (tem.getslotstarttime() > submit  //slotfinishtime(��һ��slot)--submit---slotstarttime
							&& slotListinpe_ori.get(j - 1).getslotfinishtime() <= submit) {
						startslot = j;
						break;
					}

					//�������ʱ����ǰ���slot��û�취ƥ����룬����ڴ��������
					if (j == (slotListinpe_ori.size() - 1))
						startslot = slotListinpe_ori.size();
				}

				//����slotListinpe���ݣ���������ɸѡ���ģ�ʱ������ƥ��submit----deadlineʱ��ε�slot�ļ���
				int count = 0;
				for (int j = startslot; j < slotListinpe_ori.size(); j++) {
					slot tem = new slot();
					tem = slotListinpe_ori.get(j);

					if (tem.getslotfinishtime() < deadline) {
						tem.setslotId(count);
						slotListinpe.add(tem);
						count++;
					} else if (tem.getslotfinishtime() >= deadline
							&& tem.getslotstarttime() < deadline) {//---slotstarttime---deadline---slotfinishtime---
						tem.setslotId(count);
						tem.setslotfinishtime(deadline);
						slotListinpe.add(tem);
						break;
					}
				}

				//����slotListinpe�е����һ��slot
				if (TASKInPe.get(TASKInPe.size() - 1)[1] < deadline
						&& TASKInPe.get(TASKInPe.size() - 1)[1] > submit) {//---submit---���������һ�������ִ�н���ʱ��---deadline---
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(TASKInPe.get(TASKInPe.size() - 1)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				} else if (TASKInPe.get(TASKInPe.size() - 1)[1] <= submit) {//---���������һ�������ִ�н���ʱ��---submit--- 
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			}

			SlotListInPes.put(i, slotListinpe);
		}

	}


	/**
	 * 
	* @Title: changeinpe
	* @Description: ����relax�����޸�slotlistinpe
	* @param @param slotlistinpe
	* @param @param inpe
	* @return void
	* @throws
	 */
	public static void changeinpe(ArrayList<slot> slotlistinpe, int inpe) {
		ArrayList<String> below = new ArrayList<String>();

		for (int i = 0; i < slotlistinpe.size(); i++) {
			ArrayList<String> belowte = new ArrayList<String>();

			slot slottem = slotlistinpe.get(i);

			for (int j = 0; j < slottem.getbelow().size(); j++) {
				below.add(slottem.getbelow().get(j));
			}

			String belowbuf[] = below.get(0).split(" ");
			int buffer = Integer.valueOf(belowbuf[2]).intValue();
			if (buffer >= inpe) {
				buffer += 1;
				for (int j = 0; j < below.size(); j++) {
					String belowbuff = belowbuf[0] + " " + belowbuf[1] + " "+ buffer;
					belowte.add(belowbuff);
				}
				slottem.getbelow().clear();
				slottem.setbelow(belowte);
			}
		}

	}


	/**
	 * 
	* @Title: changetasklistinpe
	* @Description: ��һ�ε�����relax�����ݽ���޸�TASKListInPes��ֵ
	* @param @param dagmap,DAG����DAG�и����������Լ�DAG�������������ϵ
	* @return void
	* @throws
	 */
	private static void changetasklistinpe(DAGMap dagmap) {
		for (int i = 0; i < pe_number; i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			for (int j = 0; j < TASKInPe.size(); j++) {
				if (TASKInPe.get(j)[2] == dagmap.getDAGId()) {
					DAG temp = new DAG();
					temp = getDAGById(TASKInPe.get(j)[2], TASKInPe.get(j)[3]);
					TASKInPe.get(j)[0] = temp.getfillbackstarttime();
					TASKInPe.get(j)[1] = temp.getfillbackfinishtime();
				}
			}
			TASKListInPes.put(i, TASKInPe);
		}
	}

	/**
	 * @Description: ����ÿ���еĸ�����������Ժ��Ƶľ��룬����relax�ֶΣ��ɳھ���
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param DAGTaskList��DAG�и���������
	 * @param canrelaxDAGTaskList�����Ժ��Ƶ��������б�
	 * @param DAGTaskDependValue��������ϵ
	 * @param levelnumber������
	 * @param totalrelax��������ֵ
	 */
	public static void calculateweight(DAGMap dagmap,
			ArrayList<DAG> DAGTaskList, ArrayList<DAG> canrelaxDAGTaskList,
			Map<String, Double> DAGTaskDependValue, int levelnumber,
			int totalrelax) {
		
		int startlevelnumber = canrelaxDAGTaskList.get(0).getnewlevel();
		int[] weight = new int[levelnumber];
		int[] relax = new int[DAGTaskList.size()];
		int[] maxlength = new int[levelnumber + 1];
		int weightsum = 0;

		for (int i = startlevelnumber; i <= levelnumber; i++) {
			int max = 0, maxid = 0;
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));

				if (canrelaxDAGTaskList.contains(dagtem)) {
					if (i == levelnumber) {
						max = dagtem.getts();
						maxid = i;
					} else {

						int value = dagtem.getts();
						for (int k = 0; k < dagmap.taskinlevel.get(i + 1).size(); k++) {
							DAG dagsuc = new DAG();
							dagsuc = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i + 1).get(k));
							if (dagmap.isDepend(String.valueOf(dagtem.getid()),String.valueOf(dagsuc.getid()))) {
								if (dagtem.getfillbackpeid() != dagsuc.getfillbackpeid()) {
									int tempp = dagtem.getts()+ (int) (double) DAGTaskDependValue.get(dagtem.getid() + " "+ dagsuc.getid());
									if (value < tempp) {
										value = tempp;
										maxid = dagtem.getid();
									}
								}
							}
						}
						
						if (max < value) {
							max = value;
							maxid = dagtem.getid();
						}
					}
				}
			}
			weight[i - 1] = max;
			maxlength[i - 1] = maxid;
		}

		for (int i = startlevelnumber - 1; i < levelnumber; i++) {
			weightsum = weight[i] + weightsum;
		}

		if(weightsum==0){
			weightsum=1;
		}
		for (int i = startlevelnumber; i <= levelnumber; i++) {
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));

				if (canrelaxDAGTaskList.contains(dagtem)) {
					int tem = weight[i - 1] * totalrelax / weightsum;
					dagtem.setrelax(tem);
				}
			}
			
		}
	}

	/**
	 * @Description: ����ͬ���������������һ���б���
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param DAGTaskList��DAG�и���������
	 * @param deadline��DAG�Ľ�ֹʱ��
	 * @return levelnumber,���в���
	 */
	public static int putsameleveltogether(DAGMap dagmap,
			ArrayList<DAG> DAGTaskList, int deadline) {
		int levelnumber = DAGTaskList.get(DAGTaskList.size() - 1).getnewlevel();
		int finishtime = DAGTaskList.get(DAGTaskList.size() - 1)
				.getfillbackfinishtime();
		int totalrelax = deadline - finishtime;
		for (int j = 1; j <= levelnumber; j++) {
			ArrayList<Integer> samelevel = new ArrayList<Integer>();
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				if (DAGTaskList.get(i).getnewlevel() == j)
					samelevel.add(i);
			}
			dagmap.taskinlevel.put(j, samelevel);
		}
		return levelnumber;
	}

	
	/**
	 * 
	* @Title: calculatenewlevel
	* @Description: ���ݵ��Ƚ�������¼���DAG�и���������Ĳ���������newlevel��orderbystarttime����
	* @param @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	* @param @param DAGTaskList��DAG�и���������
	* @param @param DAGTaskListtemp
	* @param @param setorderbystarttime
	* @return void
	* @throws
	 */
	public static void calculatenewlevel(DAGMap dagmap,ArrayList<DAG> DAGTaskList, ArrayList<DAG> DAGTaskListtemp,ArrayList<DAG> setorderbystarttime) {
		DAG min = new DAG();
		DAG temp = new DAG();
		
		//��DAGTaskListtemp����fillbackstarttime��С��������
		for (int k = 0; k < DAGTaskListtemp.size(); k++) {
			int tag = k;
			min = DAGTaskListtemp.get(k);
			temp = DAGTaskListtemp.get(k);
			for (int p = k + 1; p < DAGTaskListtemp.size(); p++) {
				if (DAGTaskListtemp.get(p).getfillbackstarttime() < min.getfillbackstarttime()) {
					min = DAGTaskListtemp.get(p);
					tag = p;
				}
			}
			if (tag != k) {
				DAGTaskListtemp.set(k, min);
				DAGTaskListtemp.set(tag, temp);
			}
		}
		
		dagmap.setorderbystarttime(DAGTaskListtemp);
		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			setorderbystarttime.add((DAG) dagmap.getorderbystarttime().get(i));
		}
		
		//����starttimeΪ����˳��
		for (int i = 0; i < setorderbystarttime.size(); i++) {
			DAG dag = new DAG();
			dag = setorderbystarttime.get(i);

			if (i == 0) {
				dag.setnewlevel(1);
			} else {
				int max = 0;
				for (int j = i - 1; j >= 0; j--) {//��Ѱ��ǰ��������ͬ�������ϵ����ڵ�����
					if (setorderbystarttime.get(j).getfillbackpeid() == dag.getfillbackpeid()) {
						max = setorderbystarttime.get(j).getnewlevel() + 1;
						break;
					}
				}
				Iterator<Integer> it = DAGTaskList.get(i).getpre().iterator();
				while (it.hasNext()) {
					int pretempid = it.next();
					int leveltemp = DAGTaskList.get(pretempid).getnewlevel() + 1;
					if (leveltemp > max)
						max = leveltemp;
				}
				dag.setnewlevel(max);
			}
		}
		
		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			for (DAG dag : setorderbystarttime)
				if (dag.getid() == DAGTaskList.get(i).getid())
					DAGTaskList.get(i).setnewlevel(dag.getnewlevel());
		}
	}

	
	
	/**
	 * 
	* @Title: calculateoriginallevel
	* @Description: ����DAG��ԭʼ����������Ĳ��������ã�level
	* @param @param  DAGTaskList��DAG�и���������
	* @return void
	* @throws
	 */
	public static void calculateoriginallevel(ArrayList<DAG> DAGTaskList) {
		for (int i = 0; i < DAGTaskList.size(); i++) {
			//�����ǰ��������ʼ�������ò㼶Ϊ1
			if (i == 0)
				DAGTaskList.get(i).setlevel(1);
			else {//�����ǰ��������ʼ�������ò㼶Ϊ�丸����㼶���ֵ+1
				int max = 0;
				Iterator<Integer> it = DAGTaskList.get(i).getpre().iterator();
				while (it.hasNext()) {
					int pretempid = it.next();
					int leveltemp = DAGTaskList.get(pretempid).getlevel() + 1;
					if (leveltemp > max)
						max = leveltemp;
				}
				DAGTaskList.get(i).setlevel(max);
			}
		}
	}

	/**
	 * 
	* @Title: wholerelax
	* @Description: ���ݵ��Ƚ������levelrelaxing������
	* @param @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	* @return void
	* @throws
	 */
	public static void wholerelax(DAGMap dagmap) {
		int Criticalnum = CriticalPath(dagmap);
		int submit = dagmap.getsubmittime();
		int deadline = dagmap.getDAGdeadline();

		ArrayList<DAG> canrelaxDAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskListtemp = new ArrayList<DAG>();
		ArrayList<DAG> setorderbystarttime = new ArrayList<DAG>();
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
			DAGTaskListtemp.add((DAG) dagmap.gettasklist().get(i));
		}

		//���㱾DAG��ԭ��level
		calculateoriginallevel(DAGTaskList);
		//����setorderbystarttime�����µ�level
		calculatenewlevel(dagmap, DAGTaskList, DAGTaskListtemp,setorderbystarttime);
		
		//��ͬ�㼶���������ͬһ���б���
		int levelnumber = putsameleveltogether(dagmap, DAGTaskList, deadline);

		int finishtime = DAGTaskList.get(DAGTaskList.size() - 1).getfillbackfinishtime();
		
		int totalrelax = deadline - finishtime;

		boolean finishsearch = true;//ֻҪ��һ������û�ܻ���ɹ�������ʧ��
		for (int i = levelnumber; i >= 1; i--) {
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));

				//���ұ������Ƿ�û�л�����߻���ʧ��
				if (dagtem.getisfillback() == false)
					canrelaxDAGTaskList.add(dagtem);
				else {
					finishsearch = false;
					break;
				}
			}
			if (finishsearch == false)
				break;
		}

		//��ʼ�ɳڲ���
		if (canrelaxDAGTaskList.size() > 0) {

			DAG mindag = new DAG();
			DAG tempdag = new DAG();
			
			//��������id��С��������canrelaxDAGTaskList
			for (int k = 0; k < canrelaxDAGTaskList.size(); k++) {
				int tag = k;
				mindag = canrelaxDAGTaskList.get(k);
				tempdag = canrelaxDAGTaskList.get(k);
				for (int p = k + 1; p < canrelaxDAGTaskList.size(); p++) {
					if (canrelaxDAGTaskList.get(p).getid() < mindag.getid()) {
						mindag = canrelaxDAGTaskList.get(p);
						tag = p;
					}
				}
				if (tag != k) {
					canrelaxDAGTaskList.set(k, mindag);
					canrelaxDAGTaskList.set(tag, tempdag);
				}
			}

			
			int startlevelnumber = canrelaxDAGTaskList.get(0).getnewlevel();
			
			//����ÿ�����������ֵ�����ö�Ӧ�����ֶ�
			calculateweight(dagmap, DAGTaskList, canrelaxDAGTaskList,DAGTaskDependValue, levelnumber, totalrelax);

			int startinpe[] = new int[pe_number];
			int finishinpe[] = new int[pe_number];
			int length = -1;
			int maxpeid = -1;
			
			for (int k = 0; k < pe_number; k++)
				startinpe[k] = timewindowmax;
			
			for (int k = 0; k < canrelaxDAGTaskList.size(); k++) {
				DAG dagtem = new DAG();
				dagtem = canrelaxDAGTaskList.get(k);
				
				if (startinpe[dagtem.getfillbackpeid()] > dagtem.getfillbackstarttime())
					startinpe[dagtem.getfillbackpeid()] = dagtem.getfillbackstarttime();
				if (finishinpe[dagtem.getfillbackpeid()] < dagtem.getfillbackfinishtime())
					finishinpe[dagtem.getfillbackpeid()] = dagtem.getfillbackfinishtime();
			}
			
			//�ҵ���ִ��ʱ����Ĵ�����
			for (int k = 0; k < pe_number; k++) {
				if (length < (finishinpe[k] - startinpe[k])) {
					length = finishinpe[k] - startinpe[k];
					maxpeid = k;
				}
			}
			//���øô������ϵ�����Ϊ�ؼ�����
			for (int k = 0; k < canrelaxDAGTaskList.size(); k++) {
				DAG dagtem = new DAG();
				dagtem = canrelaxDAGTaskList.get(k);
				if (dagtem.getfillbackpeid() == maxpeid)
					dagtem.setiscriticalnode(true);
			}

			//���ø��µ�һ�������ʱ��
			for (int j = 0; j < dagmap.taskinlevel.get(startlevelnumber).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(startlevelnumber).get(j));

				if (canrelaxDAGTaskList.contains(dagtem)) {
					//����fd
					dagtem.setslidefinishdeadline(dagtem.getfillbackfinishtime() + dagtem.getrelax());
					//����sd
					dagtem.setslidedeadline(dagtem.getrelax()+ dagtem.getfillbackstarttime());
					//���ÿɻ����ĳ���
					dagtem.setslidelength(dagtem.getrelax());
				}
			}

			//�ӵڶ��㿪ʼ����
			for (int i = startlevelnumber + 1; i <= levelnumber; i++) {
				DAG dagtem1 = new DAG();
				dagtem1 = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i - 1).get(0));
				int starttime = dagtem1.getslidefinishdeadline();
				int finishdeadline = -1;

				for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i).get(j));
					//����s
					dagtem.setfillbackstarttime(starttime);
					//����f
					dagtem.setfillbackfinishtime(dagtem.getfillbackstarttime()+ dagtem.getts());
				}

				//�ҵ������ڹؼ�·���ϵ����񲢻�ȡ����fd
				for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i).get(j));
					if (dagtem.getiscriticalnode()) {
						finishdeadline = dagtem.getfillbackfinishtime()+ dagtem.getrelax();
						break;
					}
				}

				//�������û���ڹؼ�·���ϵ�������ѡȡ��������������fdΪ�����fd
				if (finishdeadline == -1) {
					for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
						DAG dagtem = new DAG();
						dagtem = getDAGById(dagmap.getDAGId(),
								(int) dagmap.taskinlevel.get(i).get(j));
						if (finishdeadline < dagtem.getfillbackfinishtime())
							finishdeadline = dagtem.getfillbackfinishtime();
					}

				}

				//���±������������fdΪ�ڹؼ�·���ϵ������fd
				for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),
							(int) dagmap.taskinlevel.get(i).get(j));

					dagtem.setslidefinishdeadline(finishdeadline);
					dagtem.setslidedeadline(finishdeadline - dagtem.getts());
					dagtem.setslidelength(dagtem.getslidedeadline()
							- dagtem.getfillbackstarttime());
				}

			}

		} else {//û����Ҫ�ɳڵ�����
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				DAGTaskList.get(i).setslidedeadline(DAGTaskList.get(i).getfillbackstarttime());
				DAGTaskList.get(i).setslidelength(0);
			}
		}
	}

	/**
	 * @Description: �жϺ��ƿ��п��ĸ����ܷ�ʹ������ɹ�������п�
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param readylist��readylist��������
	 * 
	 * @return isslide���ܷ����
	 */
	public static boolean scheduling(DAGMap dagmap, ArrayList<DAG> readylist) {
		boolean findsuc = true;//��DAG�ܷ����ɹ���ֻҪһ������ʧ�ܾ���ȫ��ʧ��

		do {
			int finimin = timewindowmax;
			int mindag = -1;
			int message[][] = new int[readylist.size()][6];
			// 0 is if success 1 means success 0 means fail, 
			// 1 is earliest starttime
			// 2 is peid
			// 3 is slotid
			// 4 is if need slide
			// 5 is slide length
			
			int[] finish = new int[readylist.size()];//�������ִ�н���ʱ��

			//ΪDAG�г���һ���������������Ѱ�ɲ����slot��������Ϣ
			for (int i = 0; i < readylist.size(); i++) {
				DAG dag = new DAG();
				dag = readylist.get(i);
				message[i] = findslot(dagmap, dag);
				finish[i] = message[i][1] + dag.getts();
			}

			//ֻҪ������һ������û�ܻ���ɹ�����ô����DAGʧ��
			int dagId=dagmap.getDAGId();
			
			for (int i = 0; i < readylist.size(); i++) {
				DAG tempDagResult=readylist.get(i);
				
				if (message[i][0] == 0) {
					dagResultMap[dagId][tempDagResult.getid()]=1;
					findsuc = false;
					//return findsuc;
				}
			}
			
			if(findsuc==false){
				return findsuc;
			}

			
			//�ҵ�����������ִ�н���ʱ������ģ�mindagΪ������
			for (int i = 0; i < readylist.size(); i++) {
				if (finimin > finish[i]) {
					finimin = finish[i];
					mindag = i;
				}
			}

			//�ҵ����ִ�н���ʱ�����������
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			DAG dagtemp = new DAG();
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				DAG dag = new DAG();
				DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
				dag = (DAG) dagmap.gettasklist().get(i);
				if (dag.getid() == readylist.get(mindag).getid())
					dagtemp = (DAG) dagmap.gettasklist().get(i);
			}

			//�����������fillbackstarttime����Ϣ
			int startmin = finimin - readylist.get(mindag).getts();
			int pemin = message[mindag][2];
			int slotid = message[mindag][3];
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);
			dagtemp.setprefillbackdone(true);
			dagtemp.setprefillbackdone(true);


			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);
			
			// 0 is if success 1 means success 0 means fail, 
			// 1 is earliest starttime
			// 2 is peid
			// 3 is slotid
			// 4 is if need slide
			// 5 is slide length
			
			
			if (message[mindag][4] == 1) {//���������Ҫ����
				int slide = message[mindag][5];//�����ĳ���
				ArrayList<String> below = new ArrayList<String>();
				ArrayList<slot> slotafter = new ArrayList<slot>();

				//�ҵ�Ҫ���������slot
				slot slottemp = new slot();
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				for (int j = 0; j < slotlistinpe.size(); j++) {
					if (slotlistinpe.get(j).getslotId() == slotid) {
						slottemp = slotlistinpe.get(j);
						break;
					}
				}

				//�õ�������п�slot�����below
				for (int i = 0; i < slottemp.getbelow().size(); i++) {
					below.add(slottemp.getbelow().get(i));
				}

				//��ȡҪ�����Ŀ��п������������п�slotafter
				for (int j = 0; j < slotlistinpe.size(); j++) {
					if (slotlistinpe.get(j).getslotstarttime() > slottemp.getslotstarttime()) {
						slotafter.add(slotlistinpe.get(j));
					}
				}

				if (below.size() <= fillbacktasknum) {//�ÿ��п�����������10��
					int count = 0;//slotafter�еı��
					
					
					//��ȡҪ�������п�������
					for (int i = 0; i < below.size(); i++) {
						boolean flag = false;
						String buf[] = below.get(i).split(" ");
						
						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();
						
						DAG dag_temp = new DAG();
						dag_temp = getDAGById(DAGId, TASKId);

						int temp = slide;
						//
						if (count < slotafter.size()) {
							//�ҵ��������������Ǹ�slot
							if ((dag_temp.getfillbackstarttime() + dag_temp.getts())== slotafter.get(count).getslotstarttime()) {
								
								if ((slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime()) < slide) {
									slide = slide- (slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime());
								} else {
									flag = true;
								}
								count++;
							}
						}

						//���������
						getDAGById(DAGId, TASKId).setfillbackstarttime(getDAGById(DAGId, TASKId).getfillbackstarttime() + temp);
						
						//��������ǹ�һ����ӵ�0�ڵ�
						if (getDAGById(DAGId, TASKId).getfillbackfinishtime() != 0)
							getDAGById(DAGId, TASKId).setfillbackfinishtime(getDAGById(DAGId, TASKId).getfillbackfinishtime() + temp);

						TASKInPe.get(inpe + i)[0] = getDAGById(DAGId, TASKId).getfillbackstarttime();
						TASKInPe.get(inpe + i)[1] = getDAGById(DAGId, TASKId).getfillbackstarttime()+ getDAGById(DAGId, TASKId).getts();
						TASKInPe.get(inpe + i)[2] = DAGId;
						TASKInPe.get(inpe + i)[3] = TASKId;

						if (flag)
							break;
					}
					
					
					
					

				} else {//Ҫ�ƶ����������10��
					int count = 0;
					for (int i = 0; i < fillbacktasknum; i++) {
						boolean flag = false;
						String buf[] = below.get(i).split(" ");
						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();
						DAG dag_temp = new DAG();
						dag_temp = getDAGById(DAGId, TASKId);

						int temp = slide;

						if (count < slotafter.size()) {
							if ((dag_temp.getfillbackstarttime() + dag_temp.getts()) == slotafter.get(count).getslotstarttime()) {
								if ((slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime()) < slide) {
									slide = slide- (slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime());
								} else {
									slide = 0;
									flag = true;
								}
								count++;
							}
						}

						getDAGById(DAGId, TASKId).setfillbackstarttime(getDAGById(DAGId, TASKId).getfillbackstarttime() + temp);
					
						if (getDAGById(DAGId, TASKId).getfillbackfinishtime() != 0)
							getDAGById(DAGId, TASKId).setfillbackfinishtime(getDAGById(DAGId, TASKId).getfillbackfinishtime() + temp);

						TASKInPe.get(inpe + i)[0] = getDAGById(DAGId, TASKId).getfillbackstarttime();
						TASKInPe.get(inpe + i)[1] = getDAGById(DAGId, TASKId).getfillbackstarttime()+ getDAGById(DAGId, TASKId).getts();
						TASKInPe.get(inpe + i)[2] = DAGId;
						TASKInPe.get(inpe + i)[3] = TASKId;

						if (flag)
							break;
					}
					
				}
			}//���������Ҫ����

			
			//��Ҫ�����Ŀ���λ���в������񣬲�����ԭ�����ڴ������ϵ�����
			if (TASKInPe.size() > 0) {//�ô�������ԭ��������
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				
				ArrayList<String> below = new ArrayList<String>();

				slot slottem = new slot();
				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {//������slot����ԭ��������
					String buf[] = below.get(0).split(" ");
					//����������ڵ�����ı��
					int inpe = Integer.valueOf(buf[2]).intValue();

					//���ƺ���������
					for (int i = TASKInPe.size(); i > inpe; i--) {
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i - 1)[0];
						st_fitemp[1] = TASKInPe.get(i - 1)[1];
						st_fitemp[2] = TASKInPe.get(i - 1)[2];
						st_fitemp[3] = TASKInPe.get(i - 1)[3];
						TASKInPe.put(i, st_fitemp);
					}

					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);
					
					dagtemp.setisfillback(true);

					changeinpe(slotlistinpe, inpe);

				} else {//������slot����ԭ��û������
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}

			} else {//�ô�������ԭ��û������
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finimin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}

			//���¼�����п��б�
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());

			readylist.remove(mindag);

		} while (readylist.size() > 0);

		return findsuc;
	}

	/**
	 * @Description: �ж�DAG������ڵ��ܷ��ҵ�����ʱ��η��룬������򷵻���Ӧ����Ϣ
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param dagtemp ��DAG������TASK�е�һ��
	 * @return message��0 is if success(1 means success 0 means fail), 1 is
	 *         earliest start time, 2 is peid, 3 is slotid
	 */
	public static int[] findslot(DAGMap dagmap, DAG dagtemp) {
		int message[] = new int[6];

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = timewindowmax;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];	//�ڴ�����i�Ͽ�ʼִ�е�ʱ��
		int[] slotid = new int[pe_number];	//���п��ڴ�����I�ϵı��
		int[] isneedslide = new int[pe_number]; // 0 means don't need 1 means need slide
		int[] slidelength = new int[pe_number];//�ڴ�����i����Ҫ�����ĳ���
		
		/**
		 * ��0
		 */
		for(int k=0;k<pe_number;k++){
			pushFlag[k]=0;
		}
		

		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		
		ArrayList<DAG> pre_queue = new ArrayList<DAG>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dagtemp.getpre();
		if (pre.size() >= 0) {
			for (int j = 0; j < pre.size(); j++) {
				DAG buf = new DAG();
				buf = getDAGById(dagtemp.getdagid(), pre.get(j));
				pre_queue.add(buf);
			}
		}

		
		
		
		for (int i = 0; i < pe_number; i++) {
			
			int predone = 0;//�ڵ�ǰ�������ϵ�ǰ�������翪ʼִ��ʱ��
			
			if (pre_queue.size() == 1) {//���������ֻ��һ��������
				if (pre_queue.get(0).getfillbackpeid() == i) {//�븸������ͬһ����������
					predone = pre_queue.get(0).getfillbackfinishtime();
				} else {//�븸������ͬһ����������
					int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+ " "+ String.valueOf(dagtemp.getid()));
					predone = pre_queue.get(0).getfillbackfinishtime() + value;
				}
			} else if (pre_queue.size() >= 1) {//�ж��������
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == i) {//�븸������ͬһ����������
						if (predone < pre_queue.get(j).getfillbackfinishtime()) {
							predone = pre_queue.get(j).getfillbackfinishtime();
						}
					} else {//�븸������ͬһ����������
						int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+ " "+ String.valueOf(dagtemp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+ valu;
						if (predone < value)
							predone = value;
					}
				}
			}
			
			

			startinpe[i] = -1;
			ArrayList<slot> slotlistinpe = new ArrayList<slot>();
			
			//i:���������
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((slot) SlotListInPes.get(i).get(j));
			
			//��Ѱ�������ڵ�ǰ�������ϲ�������翪ʼ�Ŀ����������Ϣ
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if (predone <= slst) {
					if ((slst + dagtemp.getts()) <= slfi
							&& (slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					} else if ((slst + dagtemp.getts()) > slfi
							&& (slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
						

						
						//��Ҫ���Ƶľ���
						slide = slst + dagtemp.getts() - slfi;

						//������Ƶ�ǰ���п��������ܷ񽫵�ǰ��������ȥ
						//i:���������
						if (checkslide(i, slotlistinpe.get(j).getslotId(),slide)) {
							
							/**
							 * ������Ƴɹ���Ǹ������Ǿ������ƲŻ�ý����
							 */
							pushFlag[i]=1;
							
							startinpe[i] = slst;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 1;
							slidelength[i] = slide;//���Ƴ���
							break;
						}
					}
				} else if (predone > slst && predone < slfi) {
					if ((predone + dagtemp.getts()) <= slfi
							&& (predone + dagtemp.getts()) <= dagtemp.getdeadline()) {
						startinpe[i] = predone;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					} else if ((predone + dagtemp.getts()) > slfi
							&& (predone + dagtemp.getts()) <= dagtemp.getdeadline()) {
						
						slide = predone + dagtemp.getts() - slfi;

						if (checkslide(i, slotlistinpe.get(j).getslotId(),slide)) {
							/**
							 * ��Ǹ������Ǿ������ƲŻ�ý����
							 */
							pushFlag[i]=1;
							
							startinpe[i] = predone;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 1;
							slidelength[i] = slide;
							break;
						}
					}
				}
			}
			
			
			
		}

		
		
		
		//������֪��ǰ�����ڸ��������������翪ʼִ�е���Ϣ����Ѱ��ʼʱ������Ĵ�����pemin
		for (int i = 0; i < pe_number; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}

		// 0 is if success 1 means success 0 means fail, 1 is earliest start
		// time, 2 is peid, 3 is slotid
		//��ѡ������
		if (findsuc) {
			/**
			 * ��εĲ��������ƶ�����
			 */
			if(pushFlag[pemin]==1){
				pushSuccessCount++;
			}
			
			message[0] = 1;
			message[1] = startmin;
			message[2] = pemin;
			message[3] = slotid[pemin];
			message[4] = isneedslide[pemin];
			if (isneedslide[pemin] == 1)
				message[5] = slidelength[pemin];
			else
				message[5] = -1;
		} else {
			message[0] = 0;
		}

		return message;
	}

	
	
	/**
	 * @Description: �ж��ڵ�ǰ���п�ռ䲻��ʱ�������ƿ��п��ĸ��غ󣬿�������Ŀռ��ܷ�ʹ������ɹ�������п�
	 * 
	 * @param peid�����п����ڵ�PE��ID
	 * @param slotid�����п��ڸ�PE�ϵ�ID
	 * @param slide����Ҫ���Ƶľ��루��ʣ���ٿ�����û�з��䣩
	 * @return isslide���ܷ����
	 */
	public static boolean checkslide(int peid, int slotid, int slide) {

		//�����񻬶��Ƿ�ɹ�
		boolean isslide = true;
		int slidetry = slide;//Ҫ���Ժ��Ƶľ���
		ArrayList<String> below = new ArrayList<String>();
		ArrayList<slot> slotafter = new ArrayList<slot>();

		slot slottemp = new slot();
		ArrayList<slot> slotlistinpe = new ArrayList<slot>();

		for (int j = 0; j < SlotListInPes.get(peid).size(); j++)
			slotlistinpe.add((slot) SlotListInPes.get(peid).get(j));
		
		//�ҵ�Ҫ�������Ǹ�slot
		for (int j = 0; j < slotlistinpe.size(); j++) {
			if (slotlistinpe.get(j).getslotId() == slotid) {
				slottemp = slotlistinpe.get(j);
				break;
			}
		}

		//�õ���slot���������
		for (int i = 0; i < slottemp.getbelow().size(); i++) {
			below.add(slottemp.getbelow().get(i));
		}

		//�õ����slot������slot����slotafter
		for (int j = 0; j < slotlistinpe.size(); j++) {
			if (slotlistinpe.get(j).getslotstarttime() > slottemp.getslotstarttime()) {
				slotafter.add(slotlistinpe.get(j));
			}
		}

		if (below.size() <= fillbacktasknum) {//ֻ����Ҫ������slot�ĺ�10��������ƶ�����
			
			int count = 0;
			for (int i = 0; i < below.size(); i++) {
				
				boolean flag = false;
				//��ȡbelow�����Ϣ
				String buf[] = below.get(i).split(" ");
				int DAGId = Integer.valueOf(buf[0]).intValue();
				int TASKId = Integer.valueOf(buf[1]).intValue();
				int inpe = Integer.valueOf(buf[2]).intValue();//�ڱ��Ϊinpe��slot֮��
				DAG dagtemp = new DAG();
				dagtemp = getDAGById(DAGId, TASKId);

				//slidetryΪҪ���Ժ��Ƶľ���
				if ((dagtemp.getfillbackstarttime() + slidetry) > dagtemp.getslidedeadline()) {//������������ƺ�ᳬ��deadline
					isslide = false;
					return isslide;
				} else {//���Ʋ��ᳬ��deadline
					
					if (count < slotafter.size()) {
						
						if (dagtemp.getfillbackfinishtime() == slotafter.get(count).getslotstarttime()) {
							//������������ƽ�������һ��slot�ĳ��Ȼ��ǲ�������ԭ��slidҪ��
							if ((slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime()) < slidetry) {
								slidetry = slidetry- (slotafter.get(count).getslotfinishtime() - slotafter.get(count).getslotstarttime());
							} else {//������������ƽ�������һ��slot�ĳ���������ԭ��slidҪ����
								flag = true;
								break;
							}
							count++;
						}
					}
					
				}
				
				
				if (flag)
					break;

			}
		} else {
			
			int count = 0;
			for (int i = 0; i < fillbacktasknum; i++) {
				
				boolean flag = false;
				String buf[] = below.get(i).split(" ");
				int DAGId = Integer.valueOf(buf[0]).intValue();
				int TASKId = Integer.valueOf(buf[1]).intValue();
				int inpe = Integer.valueOf(buf[2]).intValue();
				DAG dagtemp = new DAG();
				dagtemp = getDAGById(DAGId, TASKId);

				if ((dagtemp.getfillbackstarttime() + slidetry) > dagtemp.getslidedeadline()) {
					isslide = false;
					return isslide;
				} else {
					if (count < slotafter.size()) {
						if (dagtemp.getfillbackfinishtime() == slotafter.get(
								count).getslotstarttime()) {
							if ((slotafter.get(count).getslotfinishtime() - slotafter
									.get(count).getslotstarttime()) < slidetry) {
								slidetry = slidetry
										- (slotafter.get(count)
												.getslotfinishtime() - slotafter
												.get(count).getslotstarttime());
							} else {
								slidetry = 0;
								flag = true;
								break;
							}
							count++;
						}
					}
				}
				if (flag)
					break;

			}
			
			if (slidetry > 0)
				isslide = false;
		}

		return isslide;

	}

	/**
	 * @Description: �ж�DAG����ʼ�ڵ��ܷ��ҵ�����ʱ��η���
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param dagtemp����ʼ�ڵ�
	 * @return findsuc���ܷ����
	 */
	public static boolean findfirsttaskslot(DAGMap dagmap, DAG dagtemp) {
		// perfinish is the earliest finish time minus task'ts time, the
		// earliest start time

		boolean findsuc = false;	//
		int startmin = timewindowmax;
		int finishmin = 0;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];//�ڴ�����i�Ͽ�ʼִ�е�����ʱ��
		int[] slotid = new int[pe_number];//����ڴ�����i��ִ�У�����������slot��id

		//�������д�����
		for (int i = 0; i < pe_number; i++) {
			startinpe[i] = -1;
			ArrayList<slot> slotlistinpe = new ArrayList<slot>();
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((slot) SlotListInPes.get(i).get(j));
			
			//�������㴦��������������ʱ��Ҫ��Ŀ��ж�
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if (dagtemp.getarrive() <= slst) {
					if ((slst + dagtemp.getts()) <= slfi
							&& (slst + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					} else if ((slst + dagtemp.getts()) > slfi
							&& (slst + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						slide = slst + dagtemp.getts() - slfi;

						if (checkslide(i, slotlistinpe.get(j).getslotId(),
								slide)) {
							startinpe[i] = slst;
							slotid[i] = slotlistinpe.get(j).getslotId();
							break;
						}
					}
				} else {
					if ((dagtemp.getarrive() + dagtemp.getts()) <= slfi
							&& (dagtemp.getarrive() + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						startinpe[i] = dagtemp.getarrive();
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					} else if ((dagtemp.getarrive() + dagtemp.getts()) > slfi
							&& (dagtemp.getarrive() + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						slide = dagtemp.getarrive() + dagtemp.getts() - slfi;

						if (checkslide(i, slotlistinpe.get(j).getslotId(),
								slide)) {
							startinpe[i] = dagtemp.getarrive();
							slotid[i] = slotlistinpe.get(j).getslotId();
							break;
						}
					}
				}
			}
		}

		//�ҵ���ʼʱ������Ĵ�����
		for (int i = 0; i < pe_number; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}

		//���������һ�����������ܹ������������
		if (findsuc) {
			//startmin:�����紦���ʱ��
			//finishmin:������ִ����ϵ�ʱ��
			finishmin = startmin + dagtemp.getts();
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);

			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);
			
			if (TASKInPe.size() > 0) {//ԭ���Ĵ��������ж���������
				
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				
				ArrayList<String> below = new ArrayList<String>();

				slot slottem = new slot();
				//�ҵ�slotlistinpe��Ҫ������Ǹ�slot����
				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid[pemin]) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				//�õ�below
				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {//Ҫ�����λ�ú����ж������
					//����������뵽��Ӧ��λ��
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();

					//�����п��������ڴ������ϵ�ִ�д��򶼺���һ��λ��
					for (int i = TASKInPe.size(); i > inpe; i--) {
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i - 1)[0];
						st_fitemp[1] = TASKInPe.get(i - 1)[1];
						st_fitemp[2] = TASKInPe.get(i - 1)[2];
						st_fitemp[3] = TASKInPe.get(i - 1)[3];
						TASKInPe.put(i, st_fitemp);
					}
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);
					dagtemp.setisfillback(true);
				} else {//Ҫ�����λ�ú���û������Ҳ�Ͳ���Ҫ�ƶ�
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}

			} else {//����ô�������ԭ��û������
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finishmin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}

			//�����µĿ��п��б�
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
		}

		return findsuc;//�����Ƿ��ҵ�λ�ò�������

	}

	/**
	 * @Description: �ж�backfilling�����ܷ�ɹ�
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @return fillbacksuc��backfilling�����ĳɹ����
	 */
	public static boolean fillback(DAGMap dagmap) {

		int runtime = dagmap.getsubmittime();
		boolean fillbacksuc = true; // ֻҪ��һ������ʧ�ܾ���ȫ��ʧ��
		boolean fini = true; //�Ƿ����е����񶼻���ɹ�

		ArrayList<DAG> readylist = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		
		int DAGID=dagmap.getDAGId();

		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}

		do {
			
			//������������Ϊ��ǰʱ��ִ����ϵ��������ò���
			for (DAG dag : DAGTaskList) {
				if ((dag.getfillbackstarttime() + dag.getts()) == runtime
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false) {
					dag.setfillbackfinishtime(runtime);
					dag.setfillbackdone(true);
				}
			}

			for (DAG dag : DAGTaskList) {
				
				//���ñ�DAG�ĵ�һ������
				if (dag.getid() == 0 && dag.getfillbackready() == false) {
					if (findfirsttaskslot(dagmap, DAGTaskList.get(0))) {//��ǰDAG����ʼ�ڵ����ҵ�����ʱ��η���
						DAGTaskList.get(0).setprefillbackready(true);//
						DAGTaskList.get(0).setprefillbackdone(true);

						//System.out.println("DAG"+DAGID+"");
						if (dag.getts() == 0) {//������������Ϊ�˹�һ������ӽ�ȥ����ʼ�ڵ�
							dag.setfillbackfinishtime(dag.getfillbackstarttime());
							dag.setfillbackdone(true);
						}
					} else {//��ʼ�������ʧ��
						fillbacksuc = false;
						System.out.println("DAG"+DAGID+"�ĵ�һ����������ʧ��");
						return fillbacksuc;
					}
				}

				//��ѯ��ǰ��������и������Ƿ�����ɣ��������ͽ���ǰ��������������
				if (dag.getfillbackdone() == false&& dag.getfillbackready() == false) {
					ArrayList<DAG> pre_queue = new ArrayList<DAG>();
					ArrayList<Integer> pre = new ArrayList<Integer>();
					pre = dag.getpre();

					if (pre.size() > 0) {
						boolean ready = true;
						//��ȡ��ǰ��������и�����
						for (int j = 0; j < pre.size(); j++) {
							DAG buf = new DAG();
							buf = getDAGById(dag.getdagid(), pre.get(j));
							pre_queue.add(buf);
							if (!buf.getfillbackdone()) {
								ready = false;
								break;
							}
						}
						if (ready) {
							readylist.add(dag);//��ǰ��������������
							dag.setprefillbackready(true);
							dag.setfillbackready(true);
						}
					}
				}		
			}

			
			if (readylist.size() > 0) {
				if (!scheduling(dagmap, readylist)) {
					fillbacksuc = false;
					return fillbacksuc;
				}
			}

			fini = true;
			//��ѯ��ǰʱ�̱�DAG�����е������Ƿ����Ѿ�ִ�гɹ������������񶼳ɹ�������ʱ��ѭ��
			for (DAG dag : DAGTaskList) {
				if (dag.getfillbackdone() == false) {
					fini = false;
					break;
				}
			}

			runtime = runtime + T;

		} while (runtime <= dagmap.getDAGdeadline() && !fini && fillbacksuc);

		
		if (fini) {
			for (DAG dag : DAGTaskList) {
				dag.setfillbackfinishtime(dag.getfillbackstarttime()+ dag.getts());
			}
		} else {
			fillbacksuc = false;
		}

		return fillbacksuc;
	}


	/**
	 * 
	* @Title: restoreSlotandTASK
	* @Description: ��ԭSlotListInPes��TASKListInPes
	* @param SlotListInPestemp�����ڻ�ԭ��SlotListInPes
	* @param TASKListInPestemp�����ڻ�ԭ��TASKListInPes
	* @return void
	* @throws
	 */
	public static void restoreSlotandTASK(HashMap<Integer, ArrayList> SlotListInPestemp,HashMap<Integer, HashMap> TASKListInPestemp) {
		
		SlotListInPes.clear();
		TASKListInPes.clear();

		for (int k = 0; k < SlotListInPestemp.size(); k++) {
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			for (int j = 0; j < SlotListInPestemp.get(k).size(); j++) {
				slot slottemp = new slot();
				slottemp = (slot) SlotListInPestemp.get(k).get(j);
				slotListinpe.add(slottemp);
			}
			SlotListInPes.put(k, slotListinpe);
		}
		for (int k = 0; k < TASKListInPestemp.size(); k++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPestemp.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPestemp.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPes.put(k, TASKInPe);
		}
	}
	
	/**
	 * 
	* @Title: scheduleOtherDAG
	* @Description: ʹ��Backfilling���ȵ�i��DAG�������ȳɹ�������LevelRelaxing�����������޸�TASKListInPes�и���TASK�Ŀ�ʼ����ʱ�䣬�����Ȳ��ɹ���ȡ����DAG��ִ��
	* @param @param i��DAG��ID
	* @param @param SlotListInPestemp�����ڻ�ԭ��SlotListInPes
	* @param @param TASKListInPestemp�����ڻ�ԭ��TASKListInPes
	* @return void
	* @throws
	 */
	public static void scheduleOtherDAG(int i,HashMap<Integer, ArrayList> SlotListInPestemp,HashMap<Integer, HashMap> TASKListInPestemp) {
		
		//�е�ʱ���Ǻü���DAGͬʱ�ύ
		int arrive = DAGMapList.get(i).getsubmittime();
		if (arrive > current_time)
			current_time = arrive;

		//�жϱ�DAG��backfilling�����ܷ�ɹ�
		boolean fillbacksuc = fillback(DAGMapList.get(i));

		//������ɹ�
		if (!fillbacksuc) {
			
			restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

			DAGMapList.get(i).setfillbackdone(false);
			System.out.println("�˴�����"+DAGMapList.get(i).getDAGId()+"����ʧ��");
			
			DAGMapList.get(i).setfillbackpass(true);
			//����������������Ϊʧ�ܣ�pass��
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for (int j = 0; j < DAGMapList.get(i).gettasklist().size(); j++) {
				DAGTaskList.add((DAG) DAGMapList.get(i).gettasklist().get(j));
				DAGTaskList.get(j).setfillbackpass(true);
			}
		} else {//�����DAG��backfilling�����ɹ�
			DAGMapList.get(i).setfillbackdone(true);
			//���ݱ��εĵ��Ƚ�����ɳڵ�ǰ������
			wholerelax(DAGMapList.get(i));
		}
	}

	/**
	 * 
	* @Title: copySlot
	* @Description: �������ڵ�SlotListInPes������ŵ������д���������ƥ�䵱ǰDAG��subimit--deadlineʱ��ε�slot�������ڻ�ԭ
	* @param @return
	* @return HashMap��SlotListInPestemp
	* @throws
	 */
	public static HashMap copySlot() {
		HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();

		for (int k = 0; k < SlotListInPes.size(); k++) {
			
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			
			for (int j = 0; j < SlotListInPes.get(k).size(); j++) {
				slot slottemp = new slot();
				slottemp = (slot) SlotListInPes.get(k).get(j);
				slotListinpe.add(slottemp);
			}
			
			SlotListInPestemp.put(k, slotListinpe);
		}

		return SlotListInPestemp;
	}


	
	/**
	 * 
	* @Title: copyTASK
	* @Description: �������ڵ�TASKListInPes�����ڻ�ԭ
	* @param @return
	* @return HashMap
	* @throws
	 */
	public static HashMap copyTASK() {
		HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();

		for (int k = 0; k < TASKListInPes.size(); k++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPes.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPes.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPestemp.put(k, TASKInPe);
		}

		return TASKListInPestemp;
	}

	/**
	 * @Description:������Ƚ���еĹؼ�·��
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @return Criticalnumber���ؼ�·���ϵ����������
	 */
	private static int CriticalPath(DAGMap dagmap) {
		
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		
		DAGTaskDependValue = dagmap.getdependvalue();	//DAGDependValueMap
		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}
		
		int Criticalnumber = 0;
		int i = DAGTaskList.size() - 1;

		while (i >= 0) {
			//����DAG�����壩�����һ������һ���ڹؼ�·���ϣ�����inCriticalPathΪtrue
			if (i == (DAGTaskList.size() - 1)) {
				DAGTaskList.get(i).setinCriticalPath(true);
				Criticalnumber++;
			}

			int max = -1;
			int maxid = -1;
			Iterator<Integer> it = DAGTaskList.get(i).getpre().iterator();
			while (it.hasNext()) {
				int pretempid = it.next();
				//DAGTaskList.get(pretempid).getheftaft()��ֵ��0
				int temp = (int) ((int) DAGTaskList.get(pretempid).getheftaft() + (double) DAGTaskDependValue.get(String.valueOf(pretempid + " " + i)));
				if (temp > max) {
					max = temp;
					maxid = pretempid;
				}
			}
			
			DAGTaskList.get(maxid).setinCriticalPath(true);
			Criticalnumber++;
			i = maxid;
			if (maxid == 0)
				i = -1;
		}
		return Criticalnumber;
	}

	/**
	 *@Description: Ϊ���ȳɹ���DAG����levelRelaxing����
	 * 
	 * @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param deadline��DAG�Ľ�ֹʱ��
	 * @param Criticalnumber���ؼ�·���ϵ����������
	 */
	private static void levelrelax(DAGMap dagmap, int deadline,int Criticalnumber) {
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskListtemp = new ArrayList<DAG>();
		ArrayList<DAG> setorderbystarttime = new ArrayList<DAG>();
		
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
			DAGTaskListtemp.add((DAG) dagmap.gettasklist().get(i));
		}

		//����ÿ���������ڵĲ㼶
		calculateoriginallevel(DAGTaskList);
		
		//���ݵ��Ƚ�������µĲ㼶
		calculatenewlevel(dagmap, DAGTaskList, DAGTaskListtemp,setorderbystarttime);

		// calculate weight
		//�������Ȩֵ��������
		int levelnumber = DAGTaskList.get(DAGTaskList.size() - 1).getnewlevel();//��DAG�����һ������Ĳ㼶
		int[] weight = new int[levelnumber];
		int[] relax = new int[DAGTaskList.size()];//ÿ�����������ֵ
		int[] maxlength = new int[levelnumber + 1];
		int weightsum = 0;	//Ȩֵ�ܺ�
		//��DAG���һ������Ļ������ʱ��
		int finishtime = DAGTaskList.get(DAGTaskList.size() - 1).getfillbackfinishtime();
		//������ֵ
		int totalrelax = deadline - finishtime;	
		
		//��ͬ�㼶���������ͬһ��list��
		for (int j = 1; j <= levelnumber; j++) {
			ArrayList<Integer> samelevel = new ArrayList<Integer>();
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				if (DAGTaskList.get(i).getnewlevel() == j)
					samelevel.add(i);
			}
			dagmap.taskinlevel.put(j, samelevel);
		}

		for (int i = 1; i <= levelnumber; i++) {
			int max = 0, maxid = 0;
			//����ó������Ȩֵ
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));
				
				if (i == levelnumber) {//��������һ�㣬��ôȨֵ����Ϊ����ִ��ʱ��
					max = dagtem.getts();
					maxid = i;
				} else {
					int value = dagtem.getts();
					//��ȡ��һ�㵱ǰ�����������
					for (int k = 0; k < dagmap.taskinlevel.get(i + 1).size(); k++) {
						DAG dagsuc = new DAG();
						dagsuc = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i + 1).get(k));
						if (dagmap.isDepend(String.valueOf(dagtem.getid()),String.valueOf(dagsuc.getid()))) {
							
							//�����ǰ�������������ͬһ����������
							if (dagtem.getfillbackpeid() != dagsuc.getfillbackpeid()) {							
								int tempp = dagtem.getts()+ (int) (double) DAGTaskDependValue.get(dagtem.getid() + " "+ dagsuc.getid());
								if (value < tempp) {
									value = tempp;
									maxid = dagtem.getid();
								}
							}
						}
					}
					if (max < value) {
						max = value;
						maxid = dagtem.getid();
					}
				}
			}
			
			
			weight[i - 1] = max;
			maxlength[i - 1] = maxid;//���ݵ�����ID
		}

		//����Ȩֵ�ܺ�
		for (int i = 0; i < levelnumber; i++) {
			weightsum = weight[i] + weightsum;
		}

		
		// findcriticalnode ���������� ����ִ��ʱ�����
		int maxpelength = 0;
		int maxpeid = 0;
		//��Ѱ���д����������һ������ִ�н���������ʱ����ڵ�ID
		for (int i = 0; i < pe_number; i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			if (TASKInPe.size() > 0) {
				if (maxpelength < TASKInPe.get(TASKInPe.size() - 1)[1]) {
					maxpelength = TASKInPe.get(TASKInPe.size() - 1)[1];
					maxpeid = i;
				}
			}
		}
		
		//��Ӧ�������ϵ�������������Ϊiscriticalnode
		for (int i = 0; i < TASKListInPes.get(maxpeid).size(); i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(maxpeid);
			DAG dagtem = new DAG();
			dagtem = getDAGById((int) TASKInPe.get(i)[2],(int) TASKInPe.get(i)[3]);
			dagtem.setiscriticalnode(true);
		}

		//����ÿһ�������ֵ�������ڱ�������������
		for (int i = 1; i <= levelnumber; i++) {
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));
				int tem = weight[i - 1] * totalrelax / weightsum;
				dagtem.setrelax(tem);
			}
		}

		int relaxlength = DAGTaskList.get(0).getrelax();
		
		//���ñ�DAG�ĵ�һ�������fd
		DAGTaskList.get(0).setslidefinishdeadline(
				DAGTaskList.get(0).getrelax()+ DAGTaskList.get(0).getfillbackfinishtime());
		//���ñ�DAG�ĵ�һ�������sd
		DAGTaskList.get(0).setslidedeadline(
				DAGTaskList.get(0).getrelax()+ DAGTaskList.get(0).getfillbackstarttime());
		
		//���û�������
		DAGTaskList.get(0).setslidelength(DAGTaskList.get(0).getrelax());

		
		
		for (int i = 2; i <= levelnumber; i++) {
			DAG dagtem1 = new DAG();
			dagtem1 = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i - 1).get(0));
			
			//ͬһ���fd��һ���ġ��õ���һ���fd
			int starttime = dagtem1.getslidefinishdeadline();
			
			int finishdeadline = -1;

			//���ñ�����������s��f
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));

				dagtem.setfillbackstarttime(starttime);
				dagtem.setfillbackfinishtime(dagtem.getfillbackstarttime()+ dagtem.getts());
			}

			//Ĭ��ȡ�ؼ�·���ϵ��������ٽ���ʱ��Ϊ���������������ٽ���ʱ��fd
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));
				if (dagtem.getiscriticalnode()) {//�ҵ��ڹؼ�·���ϵ�����
					finishdeadline = dagtem.getfillbackfinishtime()+ dagtem.getrelax();
					break;
				}
			}

			//�������û���ڹؼ�·���ϵ�������ôȡ��������������f�����ʱ��Ϊfinishdeadline��fd
			if (finishdeadline == -1) {
				for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int) dagmap.taskinlevel.get(i).get(j));
					if (finishdeadline < dagtem.getfillbackfinishtime())
						finishdeadline = dagtem.getfillbackfinishtime();
				}

			}

			//���ñ��������fd��sd
			for (int j = 0; j < dagmap.taskinlevel.get(i).size(); j++) {
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(), (int) dagmap.taskinlevel.get(i).get(j));

				dagtem.setslidefinishdeadline(finishdeadline);
				dagtem.setslidedeadline(finishdeadline - dagtem.getts());
				//slidelength=sd-s
				dagtem.setslidelength(dagtem.getslidedeadline()- dagtem.getfillbackstarttime());
			}

		}

	}

	
	/**
	 * 
	* @Title: FIFO
	* @Description: ���㱾�㷨��makespan
	* @param @param dagmap��DAG����DAG�и����������Լ�DAG�������������ϵ
	* @return void
	* @throws
	 */
	public static void FIFO(DAGMap dagmap) {

		int time = current_time;

		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();//��DAG�����������б�
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();	//��DAG����������������Ĵ���ֵ
		DAGTaskDependValue = dagmap.getdependvalue();
		
		//��ȡ��DAG������������뵽���㷽���Զ���ı���DAGTaskList��
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}

		
		while (time <= timeWindow) {	//timeWindowΪ���������ܽ���ʱ��
			boolean fini = true;
			
			//������DAG��������Ƿ���й������������pass�����Ƿ�����
			for (DAG dag : DAGTaskList) {
				if (dag.getfillbackdone() == false&& dag.getfillbackpass() == false) {
					fini = false;
					break;
				}
			}
			
			//�����ǰDAG���������ǽ��й�����ģ������ǻ���ʧ�ܵġ���ô���˳������ٽ��к�������
			if (fini) {
				break;
			}

			//�����������񣬲鿴��ǰʱ�䱾�ý����������Ƿ��ܹ�ִ����ϣ�����fillbackfinishtime��fillbackdone
			for (DAG dag : DAGTaskList) {
				if ((dag.getfillbackstarttime() + dag.getts()) == time
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false) {
					dag.setfillbackfinishtime(time);
					dag.setfillbackdone(true);
					PEList.get(dag.getfillbackpeid()).setfree(true);
				}
			}

			//��ѯ��ǰʱ����û�о����������оͼ����������
			for (DAG dag : DAGTaskList) {
				if (dag.getarrive() <= time && dag.getfillbackdone() == false
						&& dag.getfillbackready() == false
						&& dag.getfillbackpass() == false) {
					boolean ifready = checkready(dag, DAGTaskList,DAGTaskDependValue, time);
					if (ifready) {
						dag.setfillbackready(true);
						readyqueue.add(dag);//�������м����������
					}
				}
			}

			//���Ⱦ�������
			schedule(DAGTaskList, DAGTaskDependValue, time);

			for (DAG dag : DAGTaskList) {
				if (dag.getfillbackstarttime() == time&& dag.getfillbackready()&& dag.getfillbackdone() == false) {
					
					if (dag.getdeadline() >= time) {
						if (dag.getts() == 0) {//��ǰ�����ִ��ʱ��Ϊ0����ô��Ӧ���ǹ�һ��ʱ��ӽ�ȥ�Ľڵ㣬����ʱ���T
							dag.setfillbackfinishtime(time);
							dag.setfillbackdone(true);
							time = time - T;
						} else {
							PEList.get(dag.getfillbackpeid()).setfree(false);//��ʵ��������û���õ�����ֶΰ�
							PEList.get(dag.getfillbackpeid()).settask(dag.getid());//�ô�������Ϊ����ʱ���ڴ����������
						}
					} else {
						dag.setfillbackpass(true);
					}
					
				}

			}
			
			time = time + T;
		}

	}

	/**
	 * @Description:����readyList
	 * 
	 * @param DAGTaskList��DAG��һ�����������б�
	 * @param DAGTaskDependValue�������������ڵ�DAG���������������ϵ
	 * @param time����ǰʱ��
	 */
	private static void schedule(ArrayList<DAG> DAGTaskList,Map<String, Double> DAGTaskDependValue, int time) {

		ArrayList<DAG> buff = new ArrayList<DAG>();
		DAG min = new DAG();
		DAG temp = new DAG();
		
		//�����������е����������յ�����Ⱥ�ʱ���������
		//��Ϊ����������ͬ����һ��DAG����ô���ǵĵ���ʱ����һ���ġ�
		for (int k = 0; k < readyqueue.size(); k++) {
			int tag = k;
			min = readyqueue.get(k);
			temp = readyqueue.get(k);
			for (int p = k + 1; p < readyqueue.size(); p++) {
				if (readyqueue.get(p).getarrive() < min.getarrive()) {
					min = readyqueue.get(p);
					tag = p;
				}
			}
			if (tag != k) {
				readyqueue.set(k, min);
				readyqueue.set(tag, temp);
			}
		}

		//Ϊ�ھ��������е�ÿ��������ѡ����������������DAGTaskList��DAG������
		for (int i = 0; i < readyqueue.size(); i++) {
			DAG buf1 = new DAG();
			buf1 = readyqueue.get(i);

			for (DAG dag : DAGTaskList) {
				if (buf1.getid() == dag.getid()) {
					//Ϊ������ѡ��������ѡ��������翪ʼ�����PE��
					//���ã���ǰ�����fillbackpeid��ts��fillbackstarttime��finish_suppose��fillbackpass
					choosePE(dag, DAGTaskDependValue, time);
					break;
				}
			}
		}

		readyqueue.clear();

	}

	/**
	 * @Description:�ж�ĳһ�������Ƿ�ﵽ����״̬������������������е�fillbackready�ֶ�
	 * 
	 * @param dag��Ҫ�жϵ�������dag
	 * @param DAGTaskList��DAG�����б�
	 * @param DAGTaskDependValue�������������ڵ�DAG���������������ϵ
	 * @param time ����ǰʱ��
	 * @return isready����������dag�Ƿ���Լ���readyList
	 */
	private static boolean checkready(DAG dag, ArrayList<DAG> DAGTaskList,
			Map<String, Double> DAGTaskDependValue, int time) {

		boolean isready = true;

		if (dag.getfillbackpass() == false && dag.getfillbackdone() == false) {
			if (time > dag.getdeadline()) {
				dag.setfillbackpass(true);
			}
			if (dag.getfillbackstarttime() == 0
					&& dag.getfillbackpass() == false) {
				ArrayList<DAG> pre_queue = new ArrayList<DAG>();
				ArrayList<Integer> pre = new ArrayList<Integer>();
				pre = dag.getpre();
				if (pre.size() >= 0) {
					for (int j = 0; j < pre.size(); j++) {
						DAG buf3 = new DAG();
						buf3 = getDAGById(dag.getdagid(), pre.get(j));
						pre_queue.add(buf3);

						if (buf3.getfillbackpass()) {
							dag.setfillbackpass(true);
							isready = false;
							break;
						}

						if (!buf3.getfillbackdone()) {
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
	 * @Description:Ϊ��ǰ����ѡ��������ѡ��������翪ʼ�����PE�����ã�fillbackpeid��ts��fillbackstarttime��finish_suppose��fillbackpass
	 * 
	 * @param dag_temp��Ҫѡ��������DAG��������
	 * @param DAGTaskDependValue�������������ڵ�DAG���������������ϵ
	 * @param time����ǰʱ��
	 */
	private static void choosePE(DAG dag_temp,Map<String, Double> DAGTaskDependValue, int time) {

		//��ȡ��ǰ��������и��������
		ArrayList<DAG> pre_queue = new ArrayList<DAG>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dag_temp.getpre();
		if (pre.size() >= 0) {
			for (int j = 0; j < pre.size(); j++) {
				DAG buf = new DAG();
				buf = getDAGById(dag_temp.getdagid(), pre.get(j));
				pre_queue.add(buf);
			}
		}

		int temp[] = new int[PEList.size()];//������ڸ��������ϵ����翪ʼʱ��
		
		for (int i = 0; i < PEList.size(); i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);

			if (pre_queue.size() == 0) {//��ǰ����û�и����������ǿ�ʼ�ڵ�
				if (TASKInPe.size() == 0) {//��ǰ�����Ĵ�������û��ִ�й�����
					temp[i] = time;
				} else {
					if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
						temp[i] = time;
					else
						temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
				}
			} else if (pre_queue.size() == 1) {//��ǰ����ֻ��һ��������
				if (pre_queue.get(0).getfillbackpeid() == PEList.get(i).getID()) {//�������������ڵ�ǰ�����Ĵ������ϣ������Ǵ�����������ݴ��俪��
					if (TASKInPe.size() == 0) {
						temp[i] = time;
					} else {
						if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
					}
				} else {//�������������ڵ�ǰ�����Ĵ������ϣ����Ǵ�����������ݴ��俪��
					//value,�����������ݴ���Ŀ���
					int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+ " "+ String.valueOf(dag_temp.getid()));
					if (TASKInPe.size() == 0) {//��ǰ�����Ĵ�������û��ִ�й�����
						if ((pre_queue.get(0).getfillbackfinishtime() + value) < time)
							temp[i] = time;
						else
							temp[i] = pre_queue.get(0).getfillbackfinishtime()+ value;
					} else {
						if ((pre_queue.get(0).getfillbackfinishtime() + value) > TASKInPe.get(TASKInPe.size() - 1)[1]
								&& (pre_queue.get(0).getfillbackfinishtime() + value) > time)
							temp[i] = pre_queue.get(0).getfillbackfinishtime()+ value;
						else if (time > (pre_queue.get(0).getfillbackfinishtime() + value)
								&& time > TASKInPe.get(TASKInPe.size() - 1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
					}
				}
			} else {//��ǰ�����ж��������
				int max = time;
				
				//����ÿ��������
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == PEList.get(i).getID()) {
						if (TASKInPe.size() != 0) {
							if (max < TASKInPe.get(TASKInPe.size() - 1)[1])
								max = TASKInPe.get(TASKInPe.size() - 1)[1];
						}
					} else {
						int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+ " "+ String.valueOf(dag_temp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+ valu;

						if (TASKInPe.size() == 0) {
							if (max < value)
								max = value;
						} else {
							if (value <= TASKInPe.get(TASKInPe.size() - 1)[1]) {
								if (max < TASKInPe.get(TASKInPe.size() - 1)[1])
									max = TASKInPe.get(TASKInPe.size() - 1)[1];
							} else {
								if (max < value)
									max = value;
							}
						}
					}

				}

				temp[i] = max;
			}
		}

		//ѡ����������翪ʼʱ����С�Ĵ�����,min�����ʱ�䣬minpeid��Ӧ�Ĵ��������
		//��������翪ʼʱ����ͬ�ģ���ô��ѡ������С�Ĵ�����
		int min = timewindowmax;
		int minpeid = -1;
		for (int i = 0; i < PEList.size(); i++) {
			if (min > temp[i]) {
				min = temp[i];
				minpeid = i;
			}
		}

		if (min <= dag_temp.getdeadline()) { //���翪ʼʱ�� < ������Ľ�ֹʱ��
			
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(minpeid);

			dag_temp.setfillbackpeid(minpeid);
			dag_temp.setts(dag_temp.getlength());
			dag_temp.setfillbackstarttime(min);
			dag_temp.setfinish_suppose(dag_temp.getfillbackstarttime()+ dag_temp.getts());

			Integer[] st_fi = new Integer[4];
			st_fi[0] = dag_temp.getfillbackstarttime();	//��������翪ʼʱ��
			st_fi[1] = dag_temp.getfillbackstarttime() + dag_temp.getts();	//������������ʱ��
			st_fi[2] = dag_temp.getdagid();
			st_fi[3] = dag_temp.getid();
			TASKInPe.put(TASKInPe.size(), st_fi);

		} else {
			dag_temp.setfillbackpass(true);
		}

	}

	
	/**
	 * 
	* @Title: scheduleFirstDAG
	* @Description: ʹ��FIFO���ȵ�һ��DAG�������ȳɹ�������LevelRelaxing�����������޸�TASKListInPes�и���TASK�Ŀ�ʼ����ʱ��
	* @param 
	* @return void
	* @throws
	 */
	public static void scheduleFirstDAG() {
		
		FIFO(DAGMapList.get(0));//DAGMapList����DAG�����壩����Ϣ
		
		//temΪ��һ��DAG�������һ������
		DAG tem = (DAG) DAGMapList.get(0).gettasklist().get(DAGMapList.get(0).gettasknumber() - 1);
		
		//�����һ��DAG�������һ�������Ѿ�������ɹ�
		if (tem.getfillbackdone()) {
			DAGMapList.get(0).setfillbackdone(true);
			DAGMapList.get(0).setfillbackpass(false);
		}

		int Criticalnumber = CriticalPath(DAGMapList.get(0)); // find the critical path and get the task number on it
		
		//�ɳ��׸�DAG
		levelrelax(DAGMapList.get(0), DAGMapList.get(0).getDAGdeadline(),Criticalnumber); // relax the scheduling result
		
		//�ı�����������ϵ�����ʼ����������Ϣ
		changetasklistinpe(DAGMapList.get(0));
	}

	/**
	 * @Description:����DAX�ļ�ΪDAG����໥������ϵ�����´�����DAG_queue_personal��DAG_queue��AveComputeCostMap��ComputeCostMap��DAGDependValueMap_personal��DAGDependValueMap��DAGDependMap_personal��DAGDependMap
	 * 
	 * @param i��DAGID
	 * @param preexist�������еĹ�������������ȫ����ӵ�һ�����У��ڱ�DAGǰ����preexist������
	 * @param tasknumber��DAG���������
	 * @param arrivetimes��DAG����ʱ��
	 * @return back�������еĹ�������������ȫ����ӵ�һ�����У��ڱ�DAGȫ����Ӻ���back������
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i, int preexist,
			int tasknumber, int arrivetimes,String pathXML) throws NumberFormatException,IOException, JDOMException {

		int back = 0;
		DAGDependMap_personal = new HashMap<Integer, Integer>();
		DAGDependValueMap_personal = new HashMap<String, Double>();
		ComputeCostMap = new HashMap<Integer, int[]>();
		AveComputeCostMap = new HashMap<Integer, Integer>();

		// ��ȡXML������
		SAXBuilder builder = new SAXBuilder();
		// ��ȡdocument����
		Document doc = builder.build(pathXML+"/dag" + (i + 1) + ".xml");
		// ��ȡ���ڵ�
		Element adag = doc.getRootElement();

		for (int j = 0; j < tasknumber; j++) {
			DAG dag = new DAG();
			DAG dag_persional = new DAG();

			dag.setid(Integer.valueOf(preexist + j).intValue());
			dag.setarrive(arrivetimes);//Ϊÿ����������������DAG�ĵ���ʱ��
			dag.setdagid(i);
			dag_persional.setid(Integer.valueOf(j).intValue());
			dag_persional.setarrive(arrivetimes);
			dag_persional.setdagid(i);

			XPath path = XPath.newInstance("//job[@id='" + j + "']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute) list.get(0);
			//x������ĳ���
			int x = Integer.valueOf(attribute.getValue()).intValue();
			dag.setlength(x);
			dag.setts(x);
			dag_persional.setlength(x);
			dag_persional.setts(x);

			if (j == tasknumber - 1) {
				dag.setislast(true);
				islastnum++;
			}

			DAG_queue.add(dag);	//����DAG�������б�
			DAG_queue_personal.add(dag_persional);	//��ǰDAG��һ���������������б�

			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for (int k = 0; k < PEList.size(); k++) {	//x������ĳ���
				bufferedDouble[k] = Integer.valueOf( x/ PEList.get(k).getability());
				sum = sum + Integer.valueOf( x / PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble);	//��ǰ������ÿ���������ϵĴ�����
			AveComputeCostMap.put(j, (sum / PEList.size()));	//��ǰ���������д������ϵ�ƽ��������
		}

		XPath path1 = XPath.newInstance("//uses[@link='output']/@file");
		List list1 = path1.selectNodes(doc);
		for (int k = 0; k < list1.size(); k++) {
			Attribute attribute1 = (Attribute) list1.get(k);
			String[] pre_suc = attribute1.getValue().split("_");
			
			int[] presuc = new int[2];
			presuc[0] = Integer.valueOf(pre_suc[0]).intValue() + preexist;
			presuc[1] = Integer.valueOf(pre_suc[1]).intValue() + preexist;

			XPath path2 = XPath.newInstance("//uses[@file='"+ attribute1.getValue() + "']/@size");
			List list2 = path2.selectNodes(doc);
			Attribute attribute2 = (Attribute) list2.get(0);
			int datasize = Integer.valueOf(attribute2.getValue()).intValue();

			DAGDependMap.put(presuc[0], presuc[1]);	//����DAG�����������ӳ�䣬���ս���ŵ�������������һ��������
			DAGDependValueMap.put((presuc[0] + " " + presuc[1]),(double) datasize);
			DAG_queue.get(presuc[0]).addToSuc(presuc[1]);
			DAG_queue.get(presuc[1]).addToPre(presuc[0]);

			DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),Integer.valueOf(pre_suc[1]).intValue());
			DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]),(double) datasize);

			int tem0 = Integer.parseInt(pre_suc[0]);
			int tem1 = Integer.parseInt(pre_suc[1]);
			DAG_queue_personal.get(tem0).addToSuc(tem1);
			DAG_queue_personal.get(tem1).addToPre(tem0);

		}

		back = preexist + tasknumber;
		return back;
	}

	
	/**
	 * @Description:ΪDAG����deadline���Ժ���ǰ���㣬��ÿ�������������Ӧ����ٽ�ֹʱ�䣬��������֮������ݴ��俪��
	 * 
	 * @param dead_line ��DAG��deadline
	 * @param dagdepend_persion��DAG���໥������ϵ
	 */
	private static void createDeadline_XML(int dead_line,DAGdepend dagdepend_persion) throws Throwable {
		int maxability = 1;//�������Ĵ������������ж�Ĭ��Ϊ1
		int max = 10000;

		for (int k = DAG_queue_personal.size() - 1; k >= 0; k--) {
			
			ArrayList<DAG> suc_queue = new ArrayList<DAG>();
			ArrayList<Integer> suc = new ArrayList<Integer>();
			suc = DAG_queue_personal.get(k).getsuc();
			
			//ѡ�������������������Ŀ�ʼʱ��Ϊ�Լ��Ľ�ֹʱ�䣬���Ե����ݵĴ��俪��
			if (suc.size() > 0) {
				for (int j = 0; j < suc.size(); j++) {
					int tem = 0;
					DAG buf3 = new DAG(); //��ȡ��������������
					buf3 = getDAGById_task(suc.get(j));
					suc_queue.add(buf3);
					tem = (int) (buf3.getdeadline() - (buf3.getlength() / maxability));
					if (max > tem)
						max = tem;
				}
				DAG_queue_personal.get(k).setdeadline(max);
			} else {
				DAG_queue_personal.get(k).setdeadline(dead_line);
			}
		}
	}

	
	
	/**
	 * @Description:����DAGMAPʵ������ʼ��
	 * 
	 * @param dagdepend ��������������ϵ
	 * @param vcc����������
	 */
	public static void initdagmap(DAGdepend dagdepend, computerability vcc,String pathXML)
			throws Throwable {
		 int pre_exist = 0;
		
		//File file = new File(System.getProperty("user.dir") + "\\DAG_XML\\");
		
		File file = new File(pathXML);
		String[] fileNames = file.list();
		int num = fileNames.length - 1;

		BufferedReader bd = new BufferedReader(new FileReader(pathXML+"Deadline.txt"));
		String buffered;
		for (int i = 0; i < num; i++) {
			//ÿ��DAG��һ��dagmap
			DAGMap dagmap = new DAGMap();
			DAGdepend dagdepend_persional = new DAGdepend();
			
			/*
			 * ���DAG_queue_personal
			 */
			DAG_queue_personal.clear();

			// ��ȡDAG��arrivetime��deadline��task����
			buffered = bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];

			buff[0] = Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();// dagID
			buff[1] = Integer.valueOf(bufferedA[1]).intValue();// tasknum
			buff[2] = Integer.valueOf(bufferedA[2]).intValue();// arrivetime
			buff[3] = Integer.valueOf(bufferedA[3]).intValue();// deadline
			int deadline = buff[3];
			int tasknum = buff[1];
			taskTotal=taskTotal+ tasknum;
			int arrivetime = buff[2];

			//��ÿ��DAG��������������������Լ�������Ϣ
			pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum,arrivetime,pathXML);

			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);
			
			dagdepend_persional.setDAGList(DAG_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional.setDAGDependValueMap(DAGDependValueMap_personal);

			//�Ժ���ǰ����DAG��ÿ������Ľ�ֹʱ��
			createDeadline_XML(deadline, dagdepend_persional);

			//ΪDAG_queue�е��������ý�ֹʱ��
			int number_1 = DAG_queue.size();
			int number_2 = DAG_queue_personal.size();
			for (int k = 0; k < number_2; k++) {
				DAG_queue.get(number_1 - number_2 + k).setdeadline(DAG_queue_personal.get(k).getdeadline());
			}

			dagmap.settasknumber(tasknum);
			dagmap.setDAGId(i);
			dagmap.setDAGdeadline(deadline);
			dagmap.setsubmittime(arrivetime);
			dagmap.settasklist(DAG_queue_personal);
			dagmap.setdepandmap(DAGDependMap_personal);
			dagmap.setdependvalue(DAGDependValueMap_personal);
			DAGMapList.add(dagmap);

		}
		
		//��������DAGxml�ļ����ɵ�
		dagdepend.setdagmaplist(DAGMapList);
		dagdepend.setDAGList(DAG_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);

	}

	/**
	 * 
	* @Title: initPE
	* @Description: ����PEʵ������ʼ�������ô������ļ�������Ϊ1
	* @param @throws Throwable
	* @return void
	* @throws
	 */
	private static void initPE() throws Throwable {

		for (int i = 0; i < pe_number; i++) {
			PE pe = new PE();
			pe.setID(i);
			pe.setability(1);
			pe.setfree(true);
			pe.setAvail(0);
			PEList.add(pe);
		}
	}

	/**
	 * @Description:����DAGID��TASKID���ظ�TASKʵ��
	 * 
	 * @param DAGId ��DAGID
	 * @param dagId ��TASKID
	 * @return DAG��TASKʵ��
	 */
	private static DAG getDAGById(int DAGId, int dagId) {
		for (int i = 0; i < DAGMapList.get(DAGId).gettasknumber(); i++) {
			DAG temp = (DAG) DAGMapList.get(DAGId).gettasklist().get(i);
			if (temp.getid() == dagId)
				return temp;
		}

		return null;
	}

	/**
	 * @Description:����TASKID���ظ�TASKʵ��
	 * 
	 * @param dagId ��TASKID
	 * @return DAG��TASKʵ��
	 */
	private static DAG getDAGById_task(int dagId) {
		for (DAG dag : DAG_queue_personal) {
			if (dag.getid() == dagId)
				return dag;
		}
		return null;
	}

}
