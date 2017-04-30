package org.circuit;

import java.io.FileWriter;

import org.circuit.problem.vowel.WorkingTrainingSet;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;

public class TraningSetGenerator {

	public static void main(String[] args) throws Exception {
		FileWriter fw = new FileWriter("c:\\temp\\training_set_java.txt");
		
		WorkingTrainingSet wts = new WorkingTrainingSet();
		
		for (Solution s : wts) {
			fw.write("--------------------\n");
			for (TimeSlice ts : s) {
				fw.write("I");
				for (boolean b : ts.getInput()) {
					fw.write(b ? " 1" : " 0");
				}
				
				fw.write(" O");
				for (boolean b : ts.getOutput()) {
					fw.write(b ? " 1" : " 0");
				}
				fw.write("\n");
			}
		}
		fw.close();
	}

}
