package org.generate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DagBuilder {

	private int endNodeNumber = 0;//DAG图的尾节点数

	public static int endTime = 100 * 10000;

	public static RandomCreater randomCreater;

	public List<TaskNode> unCompleteTaskList;// 未完成任务列表

	public List<Random_Dag> dagList;// dag列表

	public static List<Random_Dag> finishDagList;// 构造完成的Dag列表

	public List<String> endNodeList;// 结束节点列表
	
	
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
	 * @Description: 初始化各个处理器，默认的处理器工作时长为 总时间窗口时间/处理器个数
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
	 * 打印节点信息
	 * 
	 * @param processorList
	 *            处理器列表
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
	 * @Description: 初始化 public List<String> endNodeList;//结束节点列表 初始化 public
	 *               List<TaskNode> unCompleteTaskList;//未完成任务列表
	 * @return void
	 * @throws
	 */
	public void initList(List<Processor> processorList) {
		// 初始化未完成的列表
		int maxsize = 0;
		// 获取处理器中初始的最大的任务数目maxsize
		for (Processor processor : processorList) {
			int size = processor.nodeList.size();
			if (size > maxsize)
				maxsize = size;
		}

		// 将所有处理器上的任务都加载在 未处理列表中
		// 初始化 public List<TaskNode> unCompleteTaskList;//未完成任务列表
		for (int i = 0; i < maxsize; i++)
			for (Processor processor : processorList) {
				if (i < processor.nodeList.size())
					unCompleteTaskList.add(processor.nodeList.get(i));
			}
		
		/**
		 * 
		 */
		System.out.println("unCompleteTaskList="+unCompleteTaskList.size());


		// 获取所有处理器上的最后一个结点
		// 初始化 public List<String> endNodeList;//结束节点列表
		for (Processor processor : processorList) {
			String endNodeId = processor.nodeList.get(processor.nodeList.size() - 1).nodeId;
			endNodeList.add(endNodeId);
		}
		
		//System.out.println();
	}

	/**
	 * 
	* @Title: isEndNode 
	* @Description: 判断某个节点是否是初始处理器生成时产生的最终节点   
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
	 * @Description: 尝试判断某个节点能否成为某DAG图的最终节点，可以返回TRUE
	 * @return boolean
	 * @throws
	 */
	public boolean tryFinishDag(TaskNode taskNode, Random_Dag dag) {
		int m = 0;
		for (TaskNode leafTask : dag.leafNodeList)
			if (taskNode.startTime >= leafTask.endTime) {
				if (taskNode.startTime == leafTask.endTime)// 如果时间差为0,判断是否在同一个处理器上
				{
					if (taskNode.getProcessorId() != leafTask.getProcessorId())
						continue;
				}
				m++;
			}
		
		//是否该节点能做为这个DAG图的最终节点？
		if (m == dag.leafNodeList.size())// 可以构成完整的Dag图
		{
			dag.generateNode(taskNode);
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, taskNode);
			dagList.remove(dag);
			finishDagList.add(dag);// 放到完成列表中去
			return true;
		}
		return false;
	}
	
	
	

	/**
	 * 
	* @Title: searchParentNode 
	* @Description: 为   unCompleteTaskList 中的每一个节点找寻匹配的父节点
	* @return void    
	* @throws
	 */
	public void searchParentNode(List<TaskNode> unCompleteTaskList,List<Random_Dag> dagList) {
		for (int i = 0; i < unCompleteTaskList.size(); i++) {
			TaskNode taskNode = unCompleteTaskList.get(i);
			
			// Collections.shuffle（）就是随机打乱原来的顺序，和洗牌一样
			Collections.shuffle(dagList);// 让node去随机匹配一个dag

			boolean match = false;

			
			//==========================================判断某个未匹配的节点能否成为某个DAG图的最终节点=================================
			for (int n = 0; n < dagList.size(); n++) // 完成Dag匹配
			{
				Random_Dag dag = dagList.get(n);
				if (dag.levelCount == dag.dagLevel + 1)
					match = tryFinishDag(taskNode, dag);
				if (match)
					break;
			}

			if (match)
				continue;//跳过本次匹配，进行下一个任务节点的匹配

			//==========================================如果当前节点是初始处理器时产生的终止节点中的一员，判断它能否成为某个DAG图的终止节点=================================
			if (isEndNode(taskNode.nodeId))// 结束节点匹配
			{
				for (int k = 0; k < dagList.size(); k++) {
					Random_Dag dag = dagList.get(k);
					match = tryFinishDag(taskNode, dag);
					if (match)
						break;
				}
			}
			
			if (match)
				continue;//跳过本次匹配，进行下一个任务节点的匹配

			//================================================================================
			
			for (int k = 0; k < dagList.size(); k++) {
				Random_Dag dag = dagList.get(k);
				//如果该DAG图层数已经到达上限，则跳过对该DAG图的搜索
				if (dag.levelCount == dag.dagLevel + 1)
					continue;
				
				boolean matchFlag = false;
				int edgeNum = 0;// 和当前节点所连接的边的条数
				
				//在当前DAG图中找寻当前节点可匹配的父节点并建立连接
				for (int j = 0; j < dag.lastLevelList.size(); j++) {
					TaskNode leafNode = dag.lastLevelList.get(j);
					if (taskNode.startTime >= leafNode.endTime)// 是否和当前Dag匹配
					{
						if (taskNode.startTime == leafNode.endTime)// 如果时间差为0,判断是否在同一个处理器上
						{
							//如果在同一处理器上，则放弃该上层节点为当前节点父节点的可能性
							if (taskNode.getProcessorId() != leafNode.getProcessorId()&& leafNode.getProcessorId() != 0)
								continue;
						}
						
						edgeNum++;
						matchFlag = true;
						match = true;
						
						// 匹配后就不是叶子节点了
						dag.leafNodeList.remove(leafNode);
						
						//如果该DAG图中本身是不包含当前任务节点的
						if (!dag.containTaskNode(taskNode))
						{
							//创建新的一层
							dag.addToNewLevel(taskNode);
							dag.generateNode(taskNode);
							// 当前节点变成叶子节点
							dag.leafNodeList.add(taskNode);
						}
						
						//当当前节点已经和本DAG图中其它上层节点建立父子关系后。
						//即使当前上层节点和当前节点满足要求，该上层节点能够成为当前节点的父节点，但是也只有1/2的概率它们之间能够建立连接
						if (edgeNum > 1)// 如果已经和上一层匹配过了
						{
							if (Math.random() > 0.5)
								continue;
						}
						dag.generateEdge(leafNode, taskNode);
					}
					
				}
				
				// 当 当前DAG图所有上层节点都遍历完毕后，如果当前节点和当前Dag匹配，则跳出，不进行剩下的DAG图的遍历
				if (matchFlag)
					break;
			}
			
			
			
			//=======================================如果当前节点与目前所有的DAG图都不匹配，则为新Dag的root=========================================
			if (!match)
			{
				//上一DAG图的提交时间
				int foreDagTime;
				//随机判断，要么是dagList中最后一个DAG图的提交时间，要么是finishDagList中最后一个DAG的提交时间
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
	 * @Description: 生成DAG图
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
	 * @Description: 为每一个DGA图添加一个结束节点 foot节点 用于规整化整个DAG图的输出汇总为一个结点
	 * @return void
	 * @throws
	 */
	public void fillDags() {
		for (int k = 0; k < dagList.size(); k++) {
			Random_Dag dag = dagList.get(k);
			TaskNode footNode = new TaskNode("foot_" + (k + 1), 0, endTime,
					endTime);
			dag.generateNode(footNode);
			endNodeNumber++;// 尾节点数加一
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, footNode);
			finishDagList.add(dag);
		}
	}

	/**
	 * 
	 * @Title: finishDags
	 * @Description: 为每一个新生成的DAG图计算截止时间
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
	public void checkDags()// 检查生成的dag是否正确
	{
		// 检查节点数量
		int nodeSum = 0;
		for (Random_Dag dag : finishDagList) {
			nodeSum += dag.taskList.size();
			//判断unCompleteTaskList中是否包含当前节点
			for (TaskNode node : dag.taskList)
				if (!unCompleteTaskList.contains(node)) {
					System.err.print("不包含节点：" + node.nodeId + " ");
				}
			//判断边是否存在，edgeList中是否包含当前边
			for (DagEdge edge : dag.edgeList)
				if (edge.tail.startTime < edge.head.endTime)
					System.err.print("边错误：" + edge.head + "——>" + edge.tail+ "　");
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
	 * @Description: 输出成txt文件改为xml文件
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
			System.setOut(ps); // 重定向输出流
			
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
	 * @Description: 初始化DAG图基本信息
	 * @return void
	 * @throws
	 */
	public void initDags() {
		unCompleteTaskList = new ArrayList<TaskNode>();
		dagList = new ArrayList<Random_Dag>();
		finishDagList = new ArrayList<Random_Dag>();
		endNodeList = new ArrayList<String>();
		randomCreater = new RandomCreater();

		// 初始化处理器，并生成其上 初始的随机任务
		List<Processor> processorList = createProcessor(
				BuildParameters.processorNumber, BuildParameters.timeWindow
						/ BuildParameters.processorNumber);

		// 打印初始处理器List中每个处理器上的的总任务数
		printNodes(processorList);

		// 初始化 public List<String> endNodeList;//结束节点列表
		// 初始化 public List<TaskNode> unCompleteTaskList;//未完成任务列表
		initList(processorList);

		generateDags(1);// 刚开始生成一个带root的DAG

		// 为每一个DGA图添加一个结束节点 foot节点.用于规整化整个DAG图的输出汇总为一个结点
		fillDags();

		// 计算deadline和打印信息
		finishDags();

		checkDags();// 检查生成Dag的正确性

		// 输出DAG信息至TXT以及XML
		writeDags();
	}

	/**
	 * 外部接口，调用生成DAG图
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
