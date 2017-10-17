package org.generate;

import java.util.List;

public class RandomCreater {
	
	public double taskLengthRate = 0.5;//���񳤶ȸ�����
//	public double bandWithRate = 0.25; //��������	
	public double levelRate = 0.5;//ÿһ������ĸ�����
	
	
	
	/**
	 * @param id ���������
	 * @param nodeList �������ڵ��б�
	 * @param capacity ��������peak�������������ٶ�
	 * @param endTime  ���������н���ʱ��
	 * 
	 * ��ʼ��������ʱ   �������ϵ�   ��ʼ����ڵ���Ϣ
	 */
	public void randomCreateNodes(int id,List<TaskNode> nodeList,int capacity,int endTime){
		int capacityLength = (int) (capacity*endTime); //��������100%����������£��ܴ���������ܳ���
		int nodeNum = 0; //task����ڵ���
		int totalLength = 0;//���񳤶�	
		
		while(totalLength < capacityLength){
			nodeNum++;//��1��ʼ
			
			/**
			 * ��������ĳ���ȡֵ��Χ��0.5*������ƽ�����ȣ�1.5*������ƽ�����ȡ�
			 */
			int taskLength = random((int)(BuildParameters.taskAverageLength*(1-taskLengthRate)), (int)(BuildParameters.taskAverageLength*(1+taskLengthRate)));//�������񳤶�	
			
			TaskNode taskNode;
			if(taskLength+totalLength > capacityLength)//�����������ĳ��� �����˴����������񳤶�
				taskLength = capacityLength -totalLength;//�����������񳤶�-��ǰ�ܳ��ȣ���Ϊ���һ������ĳ���
			
			//�����ţ����������_���ұ��
			//���������������������id�����񳤶ȣ�ִ��ʱ�䣬����ʱ�䣩
			taskNode = new TaskNode(Integer.toString(id)+"_"+Integer.toString(nodeNum),taskLength,(totalLength/capacity),(taskLength+totalLength)/capacity);	
			nodeList.add(taskNode);
			totalLength+=taskLength;
		}			
	}
	
	
	  /**
	 * @param bandWidth ��������
	 * @param size �����С
	 */
	/*public void randomBandWidths(double[][] bandWidth,int size){  		
		   for(int i=0;i<size;i++)
			   for(int j=0;j<=i;j++)
			   {
				 if(i==j)
					 bandWidth[i][j] = -1.00;
				 else
				 { 
					 bandWidth[i][j] = BandWidth.baseRate*(1+random()*bandWithRate);
					 bandWidth[j][i] = bandWidth[i][j];				 
				 }
		         
			   }
	  }	*/
	
	/**
	 * @param lastDagtime ��һ��Dag�ύ��ʱ��
	 * @param startTime ��ǰDag��ʼִ�е�ʱ��
	 * @return ������ɵ�Dag���ύʱ��
	 */
	public int randomSubmitTime(int lastDagtime,int startTime){
		return random(lastDagtime, startTime);
	}
	
	
	
	/**
	 * @param maxlength ���ϵĲ�ֵ
	 * @return ������ɵ����ݴ���ʱ��
	 */
	public int randomTranferData(int maxlength)
	{
		if(maxlength == 0)
			return 0;
		else
			return random(1, maxlength);
	}
	
	/**
	 * 
	* @Title: randomDagSize 
	* @Description: ���� ƽ��DAG������ �������ĳDAGͼ��������   
	* 				��Χ��0.5*ƽ��DAG��������1.5*ƽ��DAG��������
	* @return int    
	* @throws
	 */
	public int randomDagSize(int dagAverageSize){
		return random((int)(dagAverageSize*0.5),(int)(dagAverageSize*1.5));
		
	}
	
	/**
	 * 
	* @Title: randomLevelNum 
	* @Description: ���ݴ��ж���������DAGͼ�Ĳ���   
	* @return int    
	* @throws
	 */
	public int randomLevelNum(int dagSize,int levelFlag){
		int sqrt = (int)Math.sqrt(dagSize-2);
		if(levelFlag == 1)
			return random(1, sqrt);
		else if(levelFlag == 2)
			return random(sqrt,sqrt+3);
		else if(levelFlag == 3)
			return random(sqrt+3,dagSize-2);
		else
			return sqrt;		
	}
	
	/**
	 * 
	* @Title: randomLevelSizes 
	* @Description: ����DAGͼ�ĵڶ����������ڶ�����ÿһ���������Ŀ���������   
	* @return void    
	* @throws
	 */
	
	public void randomLevelSizes(int[] dagLevel,int nodeNumber){
		//�ȸ�ÿ���������Ŀ����ʼ��Ϊ1
		for(int j = 0;j < dagLevel.length;j++)
			 dagLevel[j] = 1;
		int i = nodeNumber - dagLevel.length;
		
		//���Ϊÿһ������������
		 while(i > 0)
		 {
			 for(int j = 0;j < dagLevel.length;j++)
		
				  if(random(0, 1) == 1)
				  {
					  dagLevel[j]++;
					  i--;
					  if(i == 0)
						  break;
				  }	
			 if(i == 0)
				 break;
		 }
	}
	
	/**
	 * @return ����[min,max]֮��������
	 */
	public int random(int min,int max){
		return (int)(min + Math.random()*(max-min+1));
	}
	

}

