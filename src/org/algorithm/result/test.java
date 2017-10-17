package org.algorithm.result;

import java.awt.List;
import java.util.ArrayList;

import org.schedule.FillbacknewWithoutInsert;
import org.schedule.fillbacknew;

public class test {

	/**
	 * @throws Throwable
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		run();
		System.out
				.println("=========================================================>");
		System.out
				.println("=========================================================>");
		System.out
				.println("=========================================================>");
		System.out
				.println("=========================================================>");
		runAlgorithm();

	}

	public static void run() throws Throwable {
		fillbacknew fi = new fillbacknew();

		String pathXML = "E:\\DagCasesXML\\deadLineTimes1.1\\0\\";// 200+
		// String pathXML = "F:\\DagCasesXML\\deadLineTimes1.1\\1\\";//25
		// String pathXML = "G:\\0\\";//85

		fi.runMakespan(pathXML, "F:\\DagCasesXML\\deadLineTimes1.1\\0");

	}

	public static void runAlgorithm() throws Throwable {

		FillbacknewWithoutInsert fWithoutInsert = new FillbacknewWithoutInsert();

		String pathXML = "E:\\DagCasesXML\\deadLineTimes1.1\\0\\";// 200+
		// String pathXML = "F:\\DagCasesXML\\deadLineTimes1.1\\1\\";//25
		// String pathXML = "G:\\0\\";//85

		fWithoutInsert.runMakespan(pathXML,
				"F:\\DagCasesXML\\deadLineTimes1.1\\0");
	}

}
