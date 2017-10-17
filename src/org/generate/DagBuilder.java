package org.generate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DagBuilder {

	private int endNodeNumber = 0;//DAGͼ��β�ڵ���

	public static int endTime = 100 * 10000;

	public static RandomCreater randomCreater;

	public List<TaskNode> unCompleteTaskList;// δ��������б�

	public List<Random_Dag> dagList;// dag�б�

	public static List<Random_Dag> finishDagList;// ������ɵ�Dag�б�

	public List<String> endNodeList;// �����ڵ��б�
	
	
	//=================================
	
	private int caseCount = 0;
	//private String basePath ;
	private String pathXML;
	private String pathTXT;

	

	public DagBuilder(int caseCount, String pathXML, String pathTXT) {
		super();
		this.caseCount = caseCount;
		this.pathXML = pathXML;
		this.pathTXT = pathTXT;
	}



	/**
	 * 
	 * @Title: createProcessor
	 * @Description: ��ʼ��������������Ĭ�ϵĴ���������ʱ��Ϊ ��ʱ�䴰��ʱ��/����������
	 * @return List<Processor>
	 * @throws
	 */
	public List<Processor> createProcessor(int number, int endTime) {
		List<Processor> processorList = new ArrayList<Processor>();
		for (int i = 1; i <= number; i++) {
			Processor processor = new Processor(i, endTime);
			processorList.add(processor);
		}
		return processorList;
	}

	/*
	 * public BandWidth createBandWidth(List<Processor> processorList){
	 * BandWidth bandWidth = new BandWidth(processorList); return bandWidth; }
	 */

	/**
	 * ��ӡ�ڵ���Ϣ
	 * 
	 * @param processorList
	 *            �������б�
	 */
	public void printNodes(List<Processor> processorList) {
		for (Processor processor : processorList) {
			System.out.print(processor.processorId + ":"
					+ processor.nodeList.size() + " ");
			System.out.println();
		}
	}

	/**
	 * 
	 * @Title: initList
	 * @Description: ��ʼ�� public List<String> endNodeList;//�����ڵ��б� ��ʼ�� public
	 *               List<TaskNode> unCompleteTaskList;//δ��������б�
	 * @return void
	 * @throws
	 */
	public void initList(List<Processor> processorList) {
		// ��ʼ��δ��ɵ��б�
		int maxsize = 0;
		// ��ȡ�������г�ʼ������������Ŀmaxsize
		for (Processor processor : processorList) {
			int size = processor.nodeList.size();
			if (size > maxsize)
				maxsize = size;
		}

		// �����д������ϵ����񶼼����� δ�����б���
		// ��ʼ�� public List<TaskNode> unCompleteTaskList;//δ��������б�
		for (int i = 0; i < maxsize; i++)
			for (Processor processor : processorList) {
				if (i < processor.nodeList.size())
					unCompleteTaskList.add(processor.nodeList.get(i));
			}
		
		/**
		 * 
		 */
		System.out.println("unCompleteTaskList="+unCompleteTaskList.size());


		// ��ȡ���д������ϵ����һ�����
		// ��ʼ�� public List<String> endNodeList;//�����ڵ��б�
		for (Processor processor : processorList) {
			String endNodeId = processor.nodeList.get(processor.nodeList.size() - 1).nodeId;
			endNodeList.add(endNodeId);
		}
		
		//System.out.println();
	}

	/**
	 * 
	* @Title: isEndNode 
	* @Description: �ж�ĳ���ڵ��Ƿ��ǳ�ʼ����������ʱ���������սڵ�   
	* @return boolean    
	* @throws
	 */
	public boolean isEndNode(String taskId) {
		for (String endNodeId : endNodeList)
			if (taskId.equals(endNodeId))
				return true;
		return false;
	}

	/**
	 * 
	 * @Title: tryFinishDag
	 * @Description: �����ж�ĳ���ڵ��ܷ��ΪĳDAGͼ�����սڵ㣬���Է���TRUE
	 * @return boolean
	 * @throws
	 */
	public boolean tryFinishDag(TaskNode taskNode, Random_Dag dag) {
		int m = 0;
		for (TaskNode leafTask : dag.leafNodeList)
			if (taskNode.startTime >= leafTask.endTime) {
				if (taskNode.startTime == leafTask.endTime)// ���ʱ���Ϊ0,�ж��Ƿ���ͬһ����������
				{
					if (taskNode.getProcessorId() != leafTask.getProcessorId())
						continue;
				}
				m++;
			}
		
		//�Ƿ�ýڵ�����Ϊ���DAGͼ�����սڵ㣿
		if (m == dag.leafNodeList.size())// ���Թ���������Dagͼ
		{
			dag.generateNode(taskNode);
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, taskNode);
			dagList.remove(dag);
			finishDagList.add(dag);// �ŵ�����б���ȥ
			return true;
		}
		return false;
	}
	
	
	

	/**
	 * 
	* @Title: searchParentNode 
	* @Description: Ϊ   unCompleteTaskList �е�ÿһ���ڵ���Ѱƥ��ĸ��ڵ�
	* @return void    
	* @throws
	 */
	public void searchParentNode(List<TaskNode> unCompleteTaskList,List<Random_Dag> dagList) {
		for (int i = 0; i < unCompleteTaskList.size(); i++) {
			TaskNode taskNode = unCompleteTaskList.get(i);
			
			// Collections.shuffle���������������ԭ����˳�򣬺�ϴ��һ��
			Collections.shuffle(dagList);// ��nodeȥ���ƥ��һ��dag

			boolean match = false;

			
			//==========================================�ж�ĳ��δƥ��Ľڵ��ܷ��Ϊĳ��DAGͼ�����սڵ�=================================
			for (int n = 0; n < dagList.size(); n++) // ���Dagƥ��
			{
				Random_Dag dag = dagList.get(n);
				if (dag.levelCount == dag.dagLevel + 1)
					match = tryFinishDag(taskNode, dag);
				if (match)
					break;
			}

			if (match)
				continue;//��������ƥ�䣬������һ������ڵ��ƥ��

			//==========================================�����ǰ�ڵ��ǳ�ʼ������ʱ��������ֹ�ڵ��е�һԱ���ж����ܷ��Ϊĳ��DAGͼ����ֹ�ڵ�=================================
			if (isEndNode(taskNode.nodeId))// �����ڵ�ƥ��
			{
				for (int k = 0; k < dagList.size(); k++) {
					Random_Dag dag = dagList.get(k);
					match = tryFinishDag(taskNode, dag);
					if (match)
						break;
				}
			}
			
			if (match)
				continue;//��������ƥ�䣬������һ������ڵ��ƥ��

			//================================================================================
			
			for (int k = 0; k < dagList.size(); k++) {
				Random_Dag dag = dagList.get(k);
				//�����DAGͼ�����Ѿ��������ޣ��������Ը�DAGͼ������
				if (dag.levelCount == dag.dagLevel + 1)
					continue;
				
				boolean matchFlag = false;
				int edgeNum = 0;// �͵�ǰ�ڵ������ӵıߵ�����
				
				//�ڵ�ǰDAGͼ����Ѱ��ǰ�ڵ��ƥ��ĸ��ڵ㲢��������
				for (int j = 0; j < dag.lastLevelList.size(); j++) {
					TaskNode leafNode = dag.lastLevelList.get(j);
					if (taskNode.startTime >= leafNode.endTime)// �Ƿ�͵�ǰDagƥ��
					{
						if (taskNode.startTime == leafNode.endTime)// ���ʱ���Ϊ0,�ж��Ƿ���ͬһ����������
						{
							//�����ͬһ�������ϣ���������ϲ�ڵ�Ϊ��ǰ�ڵ㸸�ڵ�Ŀ�����
							if (taskNode.getProcessorId() != leafNode.getProcessorId()&& leafNode.getProcessorId() != 0)
								continue;
						}
						
						edgeNum++;
						matchFlag = true;
						match = true;
						
						// ƥ���Ͳ���Ҷ�ӽڵ���
						dag.leafNodeList.remove(leafNode);
						
						//�����DAGͼ�б����ǲ�������ǰ����ڵ��
						if (!dag.containTaskNode(taskNode))
						{
							//�����µ�һ��
							dag.addToNewLevel(taskNode);
							dag.generateNode(taskNode);
							// ��ǰ�ڵ���Ҷ�ӽڵ�
							dag.leafNodeList.add(taskNode);
						}
						
						//����ǰ�ڵ��Ѿ��ͱ�DAGͼ�������ϲ�ڵ㽨�����ӹ�ϵ��
						//��ʹ��ǰ�ϲ�ڵ�͵�ǰ�ڵ�����Ҫ�󣬸��ϲ�ڵ��ܹ���Ϊ��ǰ�ڵ�ĸ��ڵ㣬����Ҳֻ��1/2�ĸ�������֮���ܹ���������
						if (edgeNum > 1)// ����Ѿ�����һ��ƥ�����
						{
							if (Math.random() > 0.5)
								continue;
						}
						dag.generateEdge(leafNode, taskNode);
					}
					
				}
				
				// �� ��ǰDAGͼ�����ϲ�ڵ㶼������Ϻ������ǰ�ڵ�͵�ǰDagƥ�䣬��������������ʣ�µ�DAGͼ�ı���
				if (matchFlag)
					break;
			}
			
			
			
			//=======================================�����ǰ�ڵ���Ŀǰ���е�DAGͼ����ƥ�䣬��Ϊ��Dag��root=========================================
			if (!match)
			{
				//��һDAGͼ���ύʱ��
				int foreDagTime;
				//����жϣ�Ҫô��dagList�����һ��DAGͼ���ύʱ�䣬Ҫô��finishDagList�����һ��DAG���ύʱ��
				if (dagList.size() > 0)
					foreDagTime = dagList.get(dagList.size() - 1).submitTime;
				else
					foreDagTime = finishDagList.get(finishDagList.size() - 1).submitTime;
				Random_Dag dag = new Random_Dag(dagList.size()+ finishDagList.size() + 1, taskNode, foreDagTime);
				dagList.add(dag);
			}
		}
		
		

	}

	/**
	 * 
	 * @Title: generateDags
	 * @Description: ����DAGͼ
	 * @return void
	 * @throws
	 */
	public void generateDags(int number) {
		for (int i = 1; i <= number; i++) {
			Random_Dag dag = new Random_Dag(i);
			dagList.add(dag);
		}
		searchParentNode(unCompleteTaskList, dagList);
	}

	/**
	 * 
	 * @Title: fillDags
	 * @Description: Ϊÿһ��DGAͼ���һ�������ڵ� foot�ڵ� ���ڹ���������DAGͼ���������Ϊһ�����
	 * @return void
	 * @throws
	 */
	public void fillDags() {
		for (int k = 0; k < dagList.size(); k++) {
			Random_Dag dag = dagList.get(k);
			TaskNode footNode = new TaskNode("foot_" + (k + 1), 0, endTime,
					endTime);
			dag.generateNode(footNode);
			endNodeNumber++;// β�ڵ�����һ
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, footNode);
			finishDagList.add(dag);
		}
	}

	/**
	 * 
	 * @Title: finishDags
	 * @Description: Ϊÿһ�������ɵ�DAGͼ�����ֹʱ��
	 * @return void
	 * @throws
	 */
	public void finishDags() {
		for (Random_Dag dag : finishDagList) {
			dag.computeDeadLine();
		}
	}

	/**
	 * 
	 * @Title: checkDags
	 * @Description: TODO
	 * @return void
	 * @throws
	 */
	public void checkDags()// ������ɵ�dag�Ƿ���ȷ
	{
		// ���ڵ�����
		int nodeSum = 0;
		for (Random_Dag dag : finishDagList) {
			nodeSum += dag.taskList.size();
			//�ж�unCompleteTaskList���Ƿ������ǰ�ڵ�
			for (TaskNode node : dag.taskList)
				if (!unCompleteTaskList.contains(node)) {
					System.err.print("�������ڵ㣺" + node.nodeId + " ");
				}
			//�жϱ��Ƿ���ڣ�edgeList���Ƿ������ǰ��
			for (DagEdge edge : dag.edgeList)
				if (edge.tail.startTime < edge.head.endTime)
					System.err.print("�ߴ���" + edge.head + "����>" + edge.tail+ "��");
		}

		System.err.println();

		int number = 0;	
		for (TaskNode taskNode : unCompleteTaskList) {

			boolean containflag = false;
			for (Random_Dag dag : finishDagList)
				if (dag.taskList.contains(taskNode)) {
					containflag = true;
					break;
				}
			if (!containflag) {
				System.err.print(taskNode.nodeId + ":" + taskNode.startTime+ " ");
				number++;
			}

		}

		if (nodeSum == unCompleteTaskList.size() + 1 + endNodeNumber)
			System.out.println("Success");
		else
			System.out.println("check dags and fix bugs");

	}

	/**
	 * 
	 * @Title: writeDags
	 * @Description: �����txt�ļ���Ϊxml�ļ�
	 * @return void
	 * @throws
	 */
	public void writeDags() {

		FileDag fileDag = new FileDag(caseCount,pathTXT);
		XMLDag xmldag = new XMLDag(caseCount,pathXML);

		fileDag.clearDir();
		xmldag.clearDir();

		// System.out.println("finishDagList.size:"+finishDagList.size()+" "+DagBuilder.finishDagList.size());

		try {
			//String basePath = System.getProperty("user.dir") + "\\DAG_XML\\";
			String filePathxml = pathXML + "Deadline.txt";

			PrintStream out = System.out;
			PrintStream ps = new PrintStream(new FileOutputStream(filePathxml));
			System.setOut(ps); // �ض��������
			
			for (int i = 1; i <= finishDagList.size(); i++) {
				for (Random_Dag dag : finishDagList) {
					String[] number = dag.dagId.split("dag");
					if (i == Integer.valueOf(number[1]).intValue()) {
						System.out.println(dag.dagId + " "
								+ dag.taskList.size() + " " + dag.submitTime
								+ " " + dag.deadlineTime);
						break;
					}
				}
			}
			ps.close();
			System.setOut(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		for (Random_Dag dag : finishDagList) {
			//fileDag.writeData(dag);
			xmldag.writeDataToXML(dag);
		}
	}

	/**
	 * 
	 * @Title: initDags
	 * @Description: ��ʼ��DAGͼ������Ϣ
	 * @return void
	 * @throws
	 */
	public void initDags() {
		unCompleteTaskList = new ArrayList<TaskNode>();
		dagList = new ArrayList<Random_Dag>();
		finishDagList = new ArrayList<Random_Dag>();
		endNodeList = new ArrayList<String>();
		randomCreater = new RandomCreater();

		// ��ʼ�������������������� ��ʼ���������
		List<Processor> processorList = createProcessor(
				BuildParameters.processorNumber, BuildParameters.timeWindow
						/ BuildParameters.processorNumber);

		// ��ӡ��ʼ������List��ÿ���������ϵĵ���������
		printNodes(processorList);

		// ��ʼ�� public List<String> endNodeList;//�����ڵ��б�
		// ��ʼ�� public List<TaskNode> unCompleteTaskList;//δ��������б�
		initList(processorList);

		generateDags(1);// �տ�ʼ����һ����root��DAG

		// Ϊÿһ��DGAͼ���һ�������ڵ� foot�ڵ�.���ڹ���������DAGͼ���������Ϊһ�����
		fillDags();

		// ����deadline�ʹ�ӡ��Ϣ
		finishDags();

		checkDags();// �������Dag����ȷ��

		// ���DAG��Ϣ��TXT�Լ�XML
		writeDags();
	}

	/**
	 * �ⲿ�ӿڣ���������DAGͼ
	 * 
	 * @Title: BuildDAG
	 * @Description: TODO
	 * @return void
	 * @throws
	 */
	public void BuildDAG(int caseCount, String pathXML, String pathTXT) {
		DagBuilder dagBuilder = new DagBuilder(caseCount, pathXML, pathTXT);
		dagBuilder.initDags();

	}

}
