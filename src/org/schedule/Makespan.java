package org.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import org.jdom.xpath.XPath;
import org.jdom.Attribute;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.generate.BuildParameters;

public class Makespan {

	private static int CURRENT_TIME = 0;	//��ǰʱ��
	private static int task_num = 0;
	private static int clocktick = 1;
	private static double heft_deadline = 0;
	private static int islastnum = 0;	//����DAGͼ�����һ���������Ŀ
	private static double deadLineTimes = 1.3;// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
	private static int pe_number = 8;	//����������
	public static String[][] rate = new String[5][4];

	private static ArrayList<PE> PEList;	//�������б�
	private static ArrayList<DAG> DAG_queue;	//�������������DAG�е�������ļ��ϣ�
	private static ArrayList<DAG> ready_queue;	//

	private static HashMap<Integer, Integer> DAG_deadline;
	private static HashMap<Integer, Integer> DAGDependMap;	//�������������DAG�е�������ļ��ϣ�
	private static HashMap<String, Double> DAGDependValueMap;	//�������������DAG�е�������ļ��ϣ�����DAG�����������������ıߵ�ֵ

	private static ArrayList<DAG> DAG_queue_personal;	//����DAG�е��������б�
	private static HashMap<Integer, Integer> DAGDependMap_personal;	//����DAG�е�������ӳ���ϵ
	private static HashMap<String, Double> DAGDependValueMap_personal;	//����DAG�е�������ı�ֵ
	
	private static Map<Integer, int[]> ComputeCostMap;	//���� DAG��ĳ������������ÿ���������ϵ�ִ��ʱ���ӳ��
	private static Map<Integer, Integer> AveComputeCostMap;	//����DAG��ĳ�����������ڴ������ϵ�ƽ��ִ��ʱ���ӳ��

	public static Map<Integer, double[]> DAGExeTimeMap;	//��HEFT�����У���ǰDAG(����)��ÿ��������ĵ����翪ʼʱ������ٿ�ʼʱ��
	private static Map<Integer, DAG> DAGIdToDAGMap;
	private static Map<Integer, Double> upRankValueMap;
	private static Map<Integer, double[]> vmComputeCostMap;
	private static Map<Integer, Double> vmAveComputeCostMap;
	private static Map<Integer, Integer[]> cloudletInVm;	//������ID==���������е�������ID���� ��ӳ��
	private static Map<Integer, Integer> cloudletInVmId;	//��ǰDAG��������������ID==��������Ĵ�����ID��ӳ��

	
	//����ѭ��
	private int caseCount=0;
	private String pathXML;
	
	/**
	 * 
	 * @Title: Makespan
	 * @Description: ���췽������ʼ������ ����
	 * @param:
	 * @throws
	 */
	public Makespan() {
		ready_queue = new ArrayList<DAG>();
		DAG_queue = new ArrayList<DAG>();
		DAG_queue_personal = new ArrayList<DAG>();
		PEList = new ArrayList<PE>();
		DAGDependMap = new HashMap<Integer, Integer>();
		DAGDependValueMap = new HashMap<String, Double>();
		deadLineTimes = BuildParameters.deadLineTimes;
		pe_number = BuildParameters.processorNumber;
	}

	/**
	 * 
	 * @Title: runMakespan_xml
	 * @Description: ���FIFO��EDF��STF��EFTF��NEWEDF���Ƚ��
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	public void runMakespan_xml(String pathXML,String resultPath) throws Throwable {

		Makespan ms = new Makespan();
		DAGdepend dagdepend = new DAGdepend();
		computerability vcc = new computerability();
		
		// ��ʼ��������
		initPE();
		int num = initdagmap(dagdepend, vcc,pathXML);

		fiforesult(dagdepend, num);
		edfresult(dagdepend, num);
		stfresult(dagdepend, num);
		eftfresult(dagdepend, num);
		
		PrintResult.printToTxt(rate,resultPath);
		
		//newEDFresult(dagdepend, num);
		
		

	}

	/**
	 * ���FIFO���Ƚ��
	 * 
	 * @param dagdepend ��������������ϵ
	 * @param num,ȫ��DAG�ĸ����������ܸ���
	 */
	public static void fiforesult(DAGdepend dagdepend, int num)throws Throwable {
		FIFO fifo = new FIFO(pe_number);
		fifo.dag_queue_ori = DAG_queue;
		fifo.course_number = DAG_queue.size();
		fifo.pe_number = pe_number;
		fifo.pe_list = PEList;
		fifo.dagdepend = dagdepend;
		int[] temp1 = new int[pe_number + 3];
		
		Date begin=new Date();
		long beginTime = begin.getTime();
		
		temp1 = fifo.makespan(pe_number);

		Date end=new Date();
		long endTime = end.getTime();
		
		DecimalFormat df = new DecimalFormat("0.0000");
		double sum = 0;

		System.out.println("FIFO:");
		
		for (int j = 0; j < PEList.size(); j++) {
			sum = (float) temp1[j + 1] / temp1[0] + sum;
		}
		System.out.println("PE's use ratio is "+ df.format((float) sum / pe_number));
		System.out.println("effective PE's use ratio is "+ df.format((float) temp1[pe_number + 2]/ (temp1[0] * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) temp1[pe_number + 1] / num));
		System.out.println();
		
		rate[0][0] = df.format((float) sum / pe_number);
		rate[0][1] = df.format((float) temp1[pe_number + 1] / num);
		rate[0][2] = df.format((float) temp1[pe_number + 2]/ (temp1[0] * pe_number));
		rate[0][3] = df.format(endTime-beginTime);
		
		
	}

	/**
	 * ���EDF���Ƚ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param num
	 *            ,ȫ��DAG�ĸ����������ܸ���
	 */
	public static void edfresult(DAGdepend dagdepend, int num) throws Throwable {
		EDF edf = new EDF(pe_number);
		edf.dag_queue_ori = DAG_queue;
		edf.course_number = DAG_queue.size();
		edf.pe_number = pe_number;
		edf.pe_list = PEList;
		edf.dagdepend = dagdepend;
		int[] temp2 = new int[pe_number + 3];
		
		Date begin=new Date();
		long beginTime = begin.getTime();
		
		temp2 = edf.makespan(pe_number);
		
		Date end=new Date();
		long endTime = end.getTime();

		DecimalFormat df = new DecimalFormat("0.0000");
		double sum = 0;

		System.out.println("EDF:");
		for (int j = 0; j < PEList.size(); j++) {
			sum = (float) temp2[j + 1] / temp2[0] + sum;
		}
		System.out.println("PE's use ratio is "+ df.format((float) sum / pe_number));
		System.out.println("effective PE's use ratio is "+ df.format((float) temp2[pe_number + 2]/ (temp2[0] * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) temp2[pe_number + 1] / num));
		System.out.println();
		rate[1][0] = df.format((float) sum / pe_number);
		rate[1][1] = df.format((float) temp2[pe_number + 1] / num);
		rate[1][2] = df.format((float) temp2[pe_number + 2]/ (temp2[0] * pe_number));
		rate[1][3] = df.format(endTime-beginTime);
	}

	/**
	 * ���STF���Ƚ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param num
	 *            ,ȫ��DAG�ĸ����������ܸ���
	 */
	public static void stfresult(DAGdepend dagdepend, int num) throws Throwable {
		STF stf = new STF(pe_number);
		stf.dag_queue_ori = DAG_queue;
		stf.course_number = DAG_queue.size();
		stf.pe_number = pe_number;
		stf.pe_list = PEList;
		stf.dagdepend = dagdepend;
		int[] temp3 = new int[pe_number + 3];
		
		Date begin=new Date();
		long beginTime = begin.getTime();
		
		temp3 = stf.makespan(pe_number);
		
		Date end=new Date();
		long endTime = end.getTime();

		DecimalFormat df = new DecimalFormat("0.0000");
		double sum = 0;

		System.out.println("STF:");
		for (int j = 0; j < PEList.size(); j++) {
			sum = (float) temp3[j + 1] / temp3[0] + sum;
		}
		System.out.println("PE's use ratio is "+ df.format((float) sum / pe_number));
		System.out.println("effective PE's use ratio is "+ df.format((float) temp3[pe_number + 2]/ (temp3[0] * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) temp3[pe_number + 1] / num));
		System.out.println();
		rate[2][0] = df.format((float) sum / pe_number);
		rate[2][1] = df.format((float) temp3[pe_number + 1] / num);
		rate[2][2] = df.format((float) temp3[pe_number + 2]/ (temp3[0] * pe_number));
		rate[2][3] = df.format(endTime-beginTime);
	}

	/**
	 * ���EFTF���Ƚ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param num
	 *            ,ȫ��DAG�ĸ����������ܸ���
	 */
	public static void eftfresult(DAGdepend dagdepend, int num)
			throws Throwable {
		EFFF efff = new EFFF(pe_number);
		efff.dag_queue_ori = DAG_queue;
		efff.course_number = DAG_queue.size();
		efff.pe_number = pe_number;
		efff.pe_list = PEList;
		efff.dagdepend = dagdepend;
		int[] temp4 = new int[pe_number + 3];
		
		Date begin=new Date();
		long beginTime = begin.getTime();
		
		temp4 = efff.makespan(pe_number);
		
		Date end=new Date();
		long endTime = end.getTime();

		DecimalFormat df = new DecimalFormat("0.0000");
		double sum = 0;

		System.out.println("EFTF:");
		for (int j = 0; j < PEList.size(); j++) {
			sum = (float) temp4[j + 1] / temp4[0] + sum;
		}
		System.out.println("PE's use ratio is "+ df.format((float) sum / pe_number));
		System.out.println("effective PE's use ratio is "+ df.format((float) temp4[pe_number + 2]/ (temp4[0] * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) temp4[pe_number + 1] / num));
		System.out.println();
		rate[3][0] = df.format((float) sum / pe_number);
		rate[3][1] = df.format((float) temp4[pe_number + 1] / num);
		rate[3][2] = df.format((float) temp4[pe_number + 2]/ (temp4[0] * pe_number));
		rate[3][3] = df.format(endTime-beginTime);
	}

	/**
	 * ���newEDF���Ƚ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param num
	 *            ,ȫ��DAG�ĸ����������ܸ���
	 */
	public static void newEDFresult(DAGdepend dagdepend, int num)
			throws Throwable {
		NewEDF newedf = new NewEDF(pe_number);
		newedf.dag_queue_ori = DAG_queue;
		newedf.course_number = DAG_queue.size();
		newedf.pe_number = pe_number;
		newedf.pe_list = PEList;
		newedf.dagdepend = dagdepend;
		int[] temp5 = new int[pe_number + 3];
		temp5 = newedf.makespan(pe_number);

		DecimalFormat df = new DecimalFormat("0.0000");
		double sum = 0;

		System.out.println("NewEDF:");
		for (int j = 0; j < PEList.size(); j++) {
			sum = (float) temp5[j + 1] / temp5[0] + sum;
		}
		System.out.println("PE's use ratio is "+ df.format((float) sum / pe_number));
		System.out.println("effective PE's use ratio is "+ df.format((float) temp5[pe_number + 2]/ (temp5[0] * pe_number)));
		System.out.println("Task Completion Rates is "+ df.format((float) temp5[pe_number + 1] / num));
		System.out.println();
		rate[4][0] = df.format((float) sum / pe_number);
		rate[4][1] = df.format((float) temp5[pe_number + 1] / num);
		rate[4][2] = df.format((float) temp5[pe_number + 2]/ (temp5[0] * pe_number));
	}

	/**
	 * ����DAGMAPʵ������ʼ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param vcc
	 *            ����������
	 * @return num,ȫ��DAG�ĸ����������ܸ���
	 */
	public static int initdagmap(DAGdepend dagdepend, computerability vcc,String pathXML)
			throws Throwable {
		int pre_exist = 0;
		//File file = new File(System.getProperty("user.dir") + "\\DAG_XML\\"); 
		
		
		File file = new File(pathXML);
		
		String[] fileNames = file.list(); // 
		int num = fileNames.length - 1;// ������һ���ļ���Deadline.txt

		BufferedReader bd = new BufferedReader(new FileReader(pathXML+"Deadline.txt"));
		String buffered;

		for (int i = 0; i < num; i++) {

			DAGdepend dagdepend_persional = new DAGdepend();
			DAG_queue_personal.clear();// ��ʼ��,ArrayList�Դ���clear()����

			// ��ȡ�ļ���Deadline.TXT������ȡDAG��arrivetime��deadline��task����
			buffered = bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];
			buff[0] = Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();// dagID
			buff[1] = Integer.valueOf(bufferedA[1]).intValue();// tasknum
			buff[2] = Integer.valueOf(bufferedA[2]).intValue();// arrivetime
			buff[3] = Integer.valueOf(bufferedA[3]).intValue();// deadline
			int tasknum = buff[1];
			int arrivetime = buff[2];
			int deadline = buff[3];

			//�������DAG����������������ϵ
			pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum,arrivetime,pathXML);

			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);
			
			dagdepend_persional.setDAGList(DAG_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional.setDAGDependValueMap(DAGDependValueMap_personal);

			//ΪDAG_queue�Լ�DAG_queue_personal�е���������deadline
			createDeadline_XML(deadline, dagdepend_persional);
			
			int number_1 = DAG_queue.size();
			int number_2 = DAG_queue_personal.size();
			for (int k = 0; k < number_2; k++) {
				DAG_queue.get(number_1 - number_2 + k).setdeadline(DAG_queue_personal.get(k).getdeadline());
			}

			double makespan = HEFT(DAG_queue_personal, dagdepend_persional);
			ComparatorDAGori comparator = new ComparatorDAGori();
			Collections.sort(DAG_queue_personal, comparator);

			int Criticalnumber = CriticalPath(DAG_queue_personal,dagdepend_persional);
			setNewDeadline(DAG_queue_personal, dagdepend_persional, arrivetime,
					deadline, makespan, Criticalnumber);
			for (int k = 0; k < number_2; k++) {
				DAG_queue.get(number_1 - number_2 + k).setnewdeadline(DAG_queue_personal.get(k).getnewdeadline());
			}
			for (int k = 0; k < number_2; k++) {
				//System.out.println("orideadline:"+ DAG_queue_personal.get(k).getdeadline()+ " newdeadline:"+ DAG_queue_personal.get(k).getnewdeadline());
			}

			clear();
		}

		dagdepend.setDAGList(DAG_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);

		return num;
	}

	/**
	 * ���PELIST
	 */
	private static void clear() throws Throwable {
		PEList.clear();
		initPE();
	}

	/**
	 * ������Ƚ���еĹؼ�·��
	 * �������һ���ڵ���ǰ���ҵģ��������Ը��ڵ��heft_eft+���ݴ��俪�����ֵΪ׼
	 * 
	 * @param dagqueue_heft��DAG�и���������
	 * @param dagdepend_heft��DAG�������������ϵ
	 * @return Criticalnumber���ؼ�·���ϵ����������
	 */
	private static int CriticalPath(ArrayList<DAG> dagqueue_heft,DAGdepend dagdepend_heft) {
		int Criticalnumber = 0;	//�ؼ�·����������Ŀ
		int i = dagqueue_heft.size() - 1;
		//�Ժ���ǰ����
		while (i >= 0) {
			if (i == (dagqueue_heft.size() - 1)) {//����������������DAGͼ�е����һ������
				dagqueue_heft.get(i).setinCriticalPath(true);
				Criticalnumber++;
			}

			int max = -1;
			int maxid = -1;
			Iterator<Integer> it = dagqueue_heft.get(i).getpre().iterator();
			while (it.hasNext()) {
				int pretempid = it.next();
				int temp = (int) ((int) dagqueue_heft.get(pretempid).getheftaft() 
						+ (double) dagdepend_heft.getDAGDependValueMap().get(pretempid + " " + i));
				if (temp > max) {
					max = temp;
					maxid = pretempid;
				}
			}
			dagqueue_heft.get(maxid).setinCriticalPath(true);
			Criticalnumber++;
			i = maxid;
			if (maxid == 0)
				i = -1;
		}
		
		return Criticalnumber;
	}

	/**
	 * ͨ��HEFT�㷨�ĵ��Ƚ������ÿ�����������deadline
	 * 
	 * @param dagqueue_heft��DAG��һ�����и���������
	 * @param dagdepend_heft��DAG��һ�����������������ϵ
	 * @param arrivetime��DAG����ǰ��һ��������ʱ��
	 * @param deadline��DAG��ֹʱ��
	 * @param makespan��HEFT���Ƚ����makespan
	 * @param Criticalnumber���ؼ�·���ϵ����������
	 */
	private static void setNewDeadline(ArrayList<DAG> dagqueue_heft,
			DAGdepend dagdepend_heft, int arrivetime, int deadline,
			double makespan, int Criticalnumber) {
		
		double redundancy = ((deadline - arrivetime) - makespan);
		double preredundancy = (redundancy / Criticalnumber);
		int cnum = Criticalnumber;

		for (int i = dagqueue_heft.size() - 1; i >= 0; i--) {
			if (dagqueue_heft.get(i).getinCriticalPath()) {//��ǰ�����ڹؼ�·����
				int newdeadline = (int) ((int) dagqueue_heft.get(i).getheftaft() 
						+ dagqueue_heft.get(i).getarrive() + preredundancy* cnum);
				dagqueue_heft.get(i).setnewdeadline(newdeadline);
				cnum--;
				
				//��ȡ���ڵ�
				Iterator<Integer> it = dagqueue_heft.get(i).getpre().iterator();
				while (it.hasNext()) {
					int pretempid = it.next();
					int dead = (int) (dagqueue_heft.get(i).getnewdeadline() 
							- (double) dagdepend_heft.getDAGDependValueMap().get(pretempid + " " + i));
					dagqueue_heft.get(pretempid).setnewdeadline(dead);
				}
			} else {//��ǰ�����ڹؼ�·����
				if (dagqueue_heft.get(i).getnewdeadline() != 0) {
					Iterator<Integer> it = dagqueue_heft.get(i).getpre().iterator();
					while (it.hasNext()) {
						int pretempid = it.next();
						int dead = (int) (dagqueue_heft.get(i).getnewdeadline() 
								- (double) dagdepend_heft.getDAGDependValueMap().get(pretempid + " " + i));
						dagqueue_heft.get(pretempid).setnewdeadline(dead);
					}
				}
			}
			
			
		}

		
		
		for (int i = dagqueue_heft.size() - 1; i >= 0; i--) {
			if (dagqueue_heft.get(i).getnewdeadline() == 0) {
				dagqueue_heft.get(i).setnewdeadline(dagqueue_heft.get(dagqueue_heft.size() - 1).getnewdeadline());
			}
		}

	}

	/**
	 * ����DAX�ļ�ΪDAG����໥������ϵ
	 * 
	 * @param i��DAGID����ǰDAG
	 * @param preexist�������еĹ�������һ������������һ��DAG����������ȫ����ӵ�һ�����У��ڱ�DAGǰ����preexist������
	 * @param tasknumber��DAG���������
	 * @param arrivetimes ��DAG����ʱ��
	 * @return back�������еĹ�������������ȫ����ӵ�һ�����У��ڱ�DAGȫ����Ӻ���back������
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i, int preexist,int tasknumber, int arrivetimes,String pathXML) throws NumberFormatException,
			IOException, JDOMException {

		int back = 0;
		DAGDependMap_personal = new HashMap<Integer, Integer>();	//ÿ��DAG���еģ������ǵ�ǰDAG���������µ����һ��output�Ķ�Ӧ��ϵ
		DAGDependValueMap_personal = new HashMap<String, Double>();		//ÿ��DAG���е�
		ComputeCostMap = new HashMap<Integer, int[]>();		//ÿ��DAG���е�
		AveComputeCostMap = new HashMap<Integer, Integer>();	//ÿ��DAG���е�

		// ��ȡXML������
		SAXBuilder builder = new SAXBuilder();
		
		// ��ȡdocument����
		Document doc = builder.build(pathXML+"/dag" + (i + 1) + ".xml");
		
		// ��ȡ���ڵ�
		Element adag = doc.getRootElement();

		for (int j = 0; j < tasknumber; j++) {
			DAG dag = new DAG();	//��ӵ� ���й�������������ȫ�����ڵ�һ�����У��ڱ�DAGǰ����preexist�����񣩵�DAG
									//��ʵ������Ӧ�ÿ���Ϊһ��DAG������DAG�еĸ���������
			DAG dag_persional = new DAG();	//

			dag.setid(Integer.valueOf(preexist + j).intValue());//�������Ϊ��preexist+�������ţ�
			dag.setarrive(arrivetimes);
			dag.setdagid((i + 1));//������������DAG������ʱԭʼ���
			dag_persional.setid(Integer.valueOf(j).intValue());
			dag_persional.setarrive(arrivetimes);
			dag_persional.setdagid((i + 1));//������������DAG������ʱԭʼ���

			XPath path = XPath.newInstance("//job[@id='" + j + "']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute) list.get(0);
			int x = Integer.valueOf(attribute.getValue()).intValue();	//�õ����񳤶�
			dag.setlength(x);	//��������ĳ���
			dag_persional.setlength(x);		//��������ĳ���

			//��Ǳ�DAG�е����һ������
			if (j == tasknumber - 1) {
				dag.setislast(true);
				islastnum++;	//�ܹ��ж��ٸ���������
			}

			DAG_queue.add(dag);
			DAG_queue_personal.add(dag_persional);

			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for (int k = 0; k < PEList.size(); k++) {
				bufferedDouble[k] = Integer.valueOf(x/ PEList.get(k).getability());
				sum = sum + Integer.valueOf(x / PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble);//DAG��ĳ������������ÿ���������ϵ�ִ��ʱ���ӳ��
			AveComputeCostMap.put(j, (sum / PEList.size()));//DAG��ĳ����������ÿ���������ϵ�ִ��ʱ���ƽ��ֵ
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
			Attribute attribute2 = (Attribute) list2.get(0);	//��������ݴ���Ļ�����Ҳ����DAG�����������ӵıߵ�ֵ
			int datasize = Integer.valueOf(attribute2.getValue()).intValue();

			DAGDependMap.put(presuc[0], presuc[1]);		//����DAG��������֮�������MAP
			DAGDependValueMap.put((presuc[0] + " " + presuc[1]),(double) datasize); 	//����DAG��������֮�������MAP�ıߵ�ֵ

			//���DAG��������֮��Ĺ���
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
		return back;//���ص�ǰ��preexist��ֵ
	}

	/**
	 * ΪDAG����deadline����ÿ�������������Ӧ����ٽ�ֹʱ��
	 * ѡ��ǰ����������������У����������ʱ��-������ִ��ʱ�䣩����С����ôֵ��Ϊ��ǰ�����deadline
	 * @param dead_line��DAG��deadline           
	 * @param dagdepend_persion��DAG���໥������ϵ    ����ʵû���õ�
	 */
	private static void createDeadline_XML(int dead_line,DAGdepend dagdepend_persion) throws Throwable {
		int maxability = 1;
		int max = 10000;

		//�����ֹʱ���ǴӺ���ǰ����
		for (int k = DAG_queue_personal.size() - 1; k >= 0; k--) {
			ArrayList<DAG> suc_queue = new ArrayList<DAG>();
			ArrayList<Integer> suc = new ArrayList<Integer>();
			suc = DAG_queue_personal.get(k).getsuc();
			//ѡ��ǰ����������������У����������ʱ��-������ִ��ʱ�䣩����С����ôֵ��Ϊ��ǰ�����deadline
			if (suc.size() > 0) {
				for (int j = 0; j < suc.size(); j++) {
					int tem = 0;
					DAG buf3 = new DAG();
					buf3 = getDAGById(suc.get(j));
					suc_queue.add(buf3);
					tem = (int) (buf3.getdeadline() - (buf3.getlength() / maxability));
					if (max > tem)
						max = tem;
				}
				DAG_queue_personal.get(k).setdeadline(max);
			} else {//��ǰ������������DAG��Ҷ����������ǽ�������
				DAG_queue_personal.get(k).setdeadline(dead_line);
			}
		}
	}

	/**
	 * 
	 * @Title: initPE
	 * @Description: ����PEʵ������ʼ��
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
	 * ����TASKID���ظ�TASKʵ��
	 * 
	 * @param dagId
	 *            ��TASKID
	 * @return DAG��TASKʵ��
	 */
	private static DAG getDAGById(int dagId) {
		for (DAG dag : DAG_queue_personal) {
			if (dag.getid() == dagId)
				return dag;
		}
		return null;
	}

	/**
	 * ��HEFT��ÿ��DAGԤ�Ƚ��е���
	 * 
	 * @param dagqueue_heft��DAG�и���������===��DAG_queue_personal
	 * @param dagdepend_heft��DAG�������������ϵ====��dagdepend_persional
	 * @return makespan��HEFT���Ƚ����makespan
	 */
	public static double HEFT(ArrayList<DAG> dagqueue_heft,DAGdepend dagdepend_heft) throws Throwable {
		
		DAGExeTimeMap = new HashMap<Integer, double[]>();
		DAGIdToDAGMap = new HashMap<Integer, DAG>(); 	//��DAG��ID����DAG����Ķ�Ӧ
		upRankValueMap = new HashMap<Integer, Double>();	//�������RANKֵ
		
		//ΪHEFT������׼������ȡ��ǰDAG���������ڸ����������ϵ�ƽ��ִ��ʱ��
		createVmComputeCost(dagqueue_heft);
		
		//����id--��DAG(��ʵ����������)��ӳ��
		for (int i = 0; i < dagqueue_heft.size(); i++) {
			DAGIdToDAGMap.put(i, dagqueue_heft.get(i));   
		}
		
		//�������rankֵ
		computeUpRankValue(dagqueue_heft, dagdepend_heft);
		
		//����rankֵ�Ӵ�С����
		ComparatorDAG comparator = new ComparatorDAG();
		Collections.sort(dagqueue_heft, comparator);
		
		double makespan = assignVm_(dagqueue_heft, dagdepend_heft);
		return makespan;
	}

	/**
	 * HEFT��ʼ��
	 * @param dagqueue_heft��DAG�и���������           
	 */
	private static void createVmComputeCost(ArrayList<DAG> dagqueue_heft1)throws IOException {
		vmComputeCostMap = new HashMap<Integer, double[]>();
		vmAveComputeCostMap = new HashMap<Integer, Double>();
		int num = 0;
		for (int i = 0; i < dagqueue_heft1.size(); i++) {
			double sum = 0;
			double ComputeCost[] = new double[pe_number];
			for (int j = 0; j < pe_number; j++) {
				ComputeCost[j] = dagqueue_heft1.get(i).getlength();
				sum += ComputeCost[j];
			}
			vmComputeCostMap.put(num, ComputeCost);	//��ʵ���ǵ�ǰDAGÿ��������ĳ���
			vmAveComputeCostMap.put(num, sum / pe_number);	//��ʵ���ǵ�ǰDAGÿ��������ĳ���
			num++;
		}
	}

	/**
	 * HEFT�������ȼ������������ϵĹ�ʽ
	 * �����Ǹ�������������Ƿ���ͬһ��������
	 * 
	 * @param dagqueue_heft��DAG�и��������񣨶��ԣ�
	 * @param dagdepend_heft��DAG�������������ϵ�����ԣ�
	 */
	public static void computeUpRankValue(ArrayList<DAG> dagqueue_heft2,DAGdepend dagdepend_heft2) {
		//rank�ļ������������Ͻ��е�
		for (int i = dagqueue_heft2.size() - 1; i >= 0; i--) {
			//��������i�ڸ����������ϵ�ƽ������ʱ��   computerability.getAveComputeCost(i)Ĭ�Ϸ���1
			dagqueue_heft2.get(i).setUpRankValue(dagqueue_heft2.get(i).getlength()/ computerability.getAveComputeCost(i));
			
			double tem = 0;
			Iterator<Integer> it = dagqueue_heft2.get(i).getsuc().iterator();
			while (it.hasNext()) {
				int sucCloudletIdTem = it.next();
				double valuetemp = dagqueue_heft2.get(sucCloudletIdTem).getUpRankValue()+ (double) dagdepend_heft2.getDAGDependValueMap().get(String.valueOf(i) + " "+ String.valueOf(sucCloudletIdTem))
						/ computerability.getAveComputeCost(i);//computerability.getAveComputeCost(i);���������ֵ��Ϊ����䴫���ٶȣ�
																//��ʱû�п������������Ƿ���ͬһ����������
				if (valuetemp > tem) {
					tem = valuetemp;
				}
			}
			
			tem += dagqueue_heft2.get(i).getUpRankValue();
			tem = (int) (tem * 1000) / 1000.0;	//ת��Ϊ������
			dagqueue_heft2.get(i).setUpRankValue(tem);
		}
	}

	/**
	 * ��HEFT��ÿ��DAGԤ�Ƚ��е���
	 * 
	 * @param dagqueue_heft����ǰDAG�и��������񣨶��ԣ�
	 * @param dagdepend_heft����ǰDAG�и������������ϵ�����ԣ�
	 * @return makespan��HEFT���Ƚ����makespan
	 */
	public static double assignVm_(ArrayList<DAG> dagqueue_heft3,DAGdepend dagdepend_heft3) {

		double makespan = 0;
		
		/* �����һ������ */
		cloudletInVm = new HashMap<Integer, Integer[]>();
		cloudletInVmId = new HashMap<Integer, Integer>();
		DecimalFormat df = new DecimalFormat("#.##");
		double temp = Integer.MAX_VALUE;
		int vmIdTem = -1;
		int[] num = new int[pe_number];
		Integer[][] cloudletinvm = new Integer[pe_number][100];// ǰһ�������ж��ٸ�����������һ������һ��DAG���ж��ٸ�������
		double exetime = 0;
		double[] time = new double[2];
		
		
		for (int firTem = 0; firTem < pe_number; firTem++) {
			if (vmComputeCostMap.get(0)[firTem] < temp) {
				temp = vmComputeCostMap.get(0)[firTem];
				vmIdTem = firTem;
			}
		}
		
		time[0] = PEList.get(vmIdTem).getAvail();
		time[1] = time[0] + vmComputeCostMap.get(0)[vmIdTem];
		DAGExeTimeMap.put(0, time);
		PEList.get(vmIdTem).setast(num[vmIdTem], time[0]);
		PEList.get(vmIdTem).setaft(num[vmIdTem], time[1]);
		num[vmIdTem]++;
		PEList.get(vmIdTem).setAvail(time[1]);
		cloudletinvm[vmIdTem][0] = 0;

		cloudletInVmId.put(0, vmIdTem);
		exetime = DAGExeTimeMap.get(0)[1] - DAGExeTimeMap.get(0)[0];
		//System.out.println("cloudlet 0" + "	ast:" + time[0] + "		aft:"+ time[1] + "		processor:" + (vmIdTem + 1) + "	pes:"+ dagqueue_heft3.get(0).getpeid() + "	exeTime:" + exetime);
		dagqueue_heft3.get(0).setheftast(time[0]);
		dagqueue_heft3.get(0).setheftaft(time[1]);

		
		/* �������������� */
		for (int iAssignTem = 1; iAssignTem < dagqueue_heft3.size(); iAssignTem++) {

			int cloudletIdCurrent = dagqueue_heft3.get(iAssignTem).getid();
			double[] timeTemp = new double[2];

			int vmIdTemp = -1;
			boolean success = false;
			timeTemp[1] = Integer.MAX_VALUE;

			/* ���ڲ��� */
			for (int Assigntemp = 0; Assigntemp < PEList.size(); Assigntemp++) {
				for (int i = 0; i < num[Assigntemp]; i++) {
					Iterator<Integer> it = DAGIdToDAGMap.get(cloudletIdCurrent)
							.getpre().iterator();
					double temp_1 = 0;
					double sum = 0;
					int cloudletIdTemp = 0;
					while (it.hasNext()) {
						/* ȡ��������ǰ������id */
						int pretempid = it.next();
						double pretemp;
						// System.out.println(cloudletIdCurrent+" "+pretempid+" "+cloudletInVmId.get(pretempid)+" "+Assigntemp);
						if (cloudletInVmId.get(pretempid) == Assigntemp) {
							pretemp = DAGExeTimeMap.get(pretempid)[1];
						} else {
							pretemp = DAGExeTimeMap.get(pretempid)[1]
									+ (double) dagdepend_heft3
											.getDAGDependValueMap()
											.get(String.valueOf(pretempid)
													+ " "
													+ String.valueOf(cloudletIdCurrent));
						}

						if (pretemp > temp_1) {
							temp_1 = pretemp;
							cloudletIdTemp = pretempid;
						}
					}
					sum += temp_1;
					sum += vmComputeCostMap.get(cloudletIdCurrent)[Assigntemp];
					if (PEList.get(Assigntemp).getast(i) != 0 && i == 0) {
						if (temp_1 >= 0
								&& PEList.get(Assigntemp).getast(i) >= sum) {
							vmIdTemp = Assigntemp;
							timeTemp[0] = temp_1;
							timeTemp[1] = sum;
							success = true;
							break;
						} else {
							continue;
						}
					} else if (PEList.get(Assigntemp).getast(i) == 0 && i == 0) {
						continue;
					} else {
						if (PEList.get(Assigntemp).getast(i) >= sum
								&& (PEList.get(Assigntemp).getast(i) - PEList
										.get(Assigntemp).getaft(i - 1)) >= vmComputeCostMap
										.get(cloudletIdCurrent)[Assigntemp]) {
							if (temp_1 < PEList.get(Assigntemp).getaft(i - 1)) {
								timeTemp[0] = PEList.get(Assigntemp).getaft(
										i - 1);
								timeTemp[1] = PEList.get(Assigntemp).getaft(
										i - 1)
										+ vmComputeCostMap
												.get(cloudletIdCurrent)[Assigntemp];
							} else {
								timeTemp[0] = temp_1;
								timeTemp[1] = sum;
							}
							vmIdTemp = Assigntemp;
							success = true;
							break;
						} else {
							continue;
						}
					}
				}
				if (success) {
					break;
				}
			}

			if (success) {
				DAGExeTimeMap.put(cloudletIdCurrent, timeTemp);
				PEList.get(vmIdTemp).setast(num[vmIdTemp], timeTemp[0]);
				PEList.get(vmIdTemp).setaft(num[vmIdTemp], timeTemp[1]);

				DAGIdToDAGMap.get(cloudletIdCurrent).setinserte(true);
				cloudletinvm[vmIdTemp][num[vmIdTemp]] = cloudletIdCurrent;
				int q = 0;
				int n = num[vmIdTemp];
				for (int p = num[vmIdTemp] - 1; p >= 0; p--) {
					if (DAGExeTimeMap.get(cloudletinvm[vmIdTemp][p])[0] > timeTemp[0]) {
						q = cloudletinvm[vmIdTemp][p];
						cloudletinvm[vmIdTemp][p] = cloudletIdCurrent;
						cloudletinvm[vmIdTemp][n] = q;
						n = p;
					}
				}
				dagqueue_heft3.get(iAssignTem).setinserte(true);
				num[vmIdTemp]++;
				PEList.get(vmIdTemp).setAvail(timeTemp[1]);
				cloudletInVmId.put(cloudletIdCurrent, vmIdTemp);
				exetime = DAGExeTimeMap.get(cloudletIdCurrent)[1]
						- DAGExeTimeMap.get(cloudletIdCurrent)[0];
				//System.out.println("cloudlet " + (cloudletIdCurrent) + "	ast:"+ timeTemp[0] + "		aft:" + timeTemp[1] + "	processor:"+ (vmIdTemp + 1) + "	pes:"+ dagqueue_heft3.get(iAssignTem).getpeid()+ "	exeTime:" + exetime);

				dagqueue_heft3.get(iAssignTem).setheftast(timeTemp[0]);
				dagqueue_heft3.get(iAssignTem).setheftaft(timeTemp[1]);
				// System.out.println(dagqueue_heft3.get(iAssignTem).getid()+" "+dagqueue_heft3.get(iAssignTem).getheftast()+" "+dagqueue_heft3.get(iAssignTem).getheftaft());

				continue;
			}

			for (int jAssignTem = 0; jAssignTem < PEList.size(); jAssignTem++) {
				/* ��ǰ������ÿ�����������ݶ���est��ΪtemEST */
				double temEST = PEList.get(jAssignTem).getAvail();
				/* ��ǰ�����ÿ��ǰ���������������ʱ�̵����ֵΪtem */
				double tem = 0;
				/* ѡ��ǰ������id */
				int cloudletIdTemp = 0;

				Iterator<Integer> it = DAGIdToDAGMap.get(cloudletIdCurrent)
						.getpre().iterator();

				while (it.hasNext()) {
					/* ȡ��������ǰ������id */
					int preTempId = it.next();
					/* ��ǰ�����ÿ��ǰ���������������ʱ�� */
					double preTem;

					if (cloudletInVmId.get(preTempId) == jAssignTem) {
						preTem = DAGExeTimeMap.get(preTempId)[1];
					} else {
						preTem = DAGExeTimeMap.get(preTempId)[1]
								+ (double) dagdepend_heft3
										.getDAGDependValueMap()
										.get(String.valueOf(preTempId)
												+ " "
												+ String.valueOf(cloudletIdCurrent));
					}

					if (preTem > tem) {
						tem = preTem;
						cloudletIdTemp = preTempId;
					}
				}
				temEST = (temEST > tem) ? temEST : tem;
				if ((temEST + vmComputeCostMap.get(cloudletIdCurrent)[jAssignTem]) < timeTemp[1]) {
					timeTemp[0] = temEST;
					timeTemp[1] = temEST
							+ vmComputeCostMap.get(cloudletIdCurrent)[jAssignTem];
					vmIdTemp = jAssignTem;
				}
			}
			DAGExeTimeMap.put(cloudletIdCurrent, timeTemp);
			PEList.get(vmIdTemp).setast(num[vmIdTemp], timeTemp[0]);
			PEList.get(vmIdTemp).setaft(num[vmIdTemp], timeTemp[1]);

			cloudletinvm[vmIdTemp][num[vmIdTemp]] = cloudletIdCurrent;
			num[vmIdTemp]++;
			PEList.get(vmIdTemp).setAvail(timeTemp[1]);
			cloudletInVmId.put(cloudletIdCurrent, vmIdTemp);
			exetime = DAGExeTimeMap.get(cloudletIdCurrent)[1]
					- DAGExeTimeMap.get(cloudletIdCurrent)[0];
			//System.out.println("cloudlet " + (cloudletIdCurrent) + "	ast:"+ timeTemp[0] + "		aft:" + timeTemp[1] + "	processor:"+ (vmIdTemp + 1) + "	pes:"+ dagqueue_heft3.get(iAssignTem).getpeid() + "	exeTime:"+ exetime);

			dagqueue_heft3.get(iAssignTem).setheftast(timeTemp[0]);
			dagqueue_heft3.get(iAssignTem).setheftaft(timeTemp[1]);
			// System.out.println(dagqueue_heft3.get(iAssignTem).getid()+" "+dagqueue_heft3.get(iAssignTem).getheftast()+" "+dagqueue_heft3.get(iAssignTem).getheftaft());

			makespan = timeTemp[1];
		}

		for (int i = 0; i < PEList.size(); i++) {
			cloudletInVm.put(i, cloudletinvm[i]);
		}

		return makespan;

	}

}
