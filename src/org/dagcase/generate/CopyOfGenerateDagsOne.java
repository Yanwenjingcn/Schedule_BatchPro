package org.dagcase.generate;

import java.io.File;

import org.generate.BuildParameters;
import org.generate.DagBuilder;

public class CopyOfGenerateDagsOne {

	static File fileXML;
	static File fileTXT;
	static File fileResult;

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) {

		// System.out.println("BuildParameters.dagAverageSize="+BuildParameters.dagAverageSize);

		String basePathXML = "E:\\DagCasesXML\\dagAverageSize" + 30 + "\\";
		String basePathTXT = "E:\\DagCasesTXT\\dagAverageSize" + 30 + "\\";

		cycle(basePathXML, basePathTXT);

	}

	/**
	 * 
	 * @Title: cycle
	 * @Description: 穿入路径然后循环100次
	 * @param @param basePathXML
	 * @param @param basePathTXT
	 * @return void
	 * @throws
	 */

	public static void cycle(String basePathXML, String basePathTXT) {

		for (int i = 0; i < 10; i++) {
			String pathXML = basePathXML + i + "\\";
			String pathTXT = basePathTXT + i + "\\";

			fileXML = new File(pathXML);
			fileTXT = new File(pathTXT);

			fileXML.mkdirs();
			fileTXT.mkdirs();

			DagBuilder dagbuilder = new DagBuilder(i, pathXML, pathTXT);
			dagbuilder.BuildDAG(i, pathXML, pathTXT);
		}

	}

}
