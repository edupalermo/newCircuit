package org.circuit;

import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitOutputGenerator;
import org.circuit.dao.CircuitWrapperDao;
import org.circuit.dao.EvaluatorWrapperDao;
import org.circuit.dao.ProblemDao;
import org.circuit.dao.TrainingSetWrapperDao;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.solution.StringSolution;
import org.circuit.solution.TrainingSet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestBetter {
	
	private static final Logger logger = Logger.getLogger(TestBetter.class);

	private static ApplicationContext springContext = null;
	
	public static void main(String[] args) {

		try {
			springContext = new ClassPathXmlApplicationContext("application-context.xml");

			ProblemDao problemDao = springContext.getBean(ProblemDao.class);

			Problem problem = problemDao.findByName("CHAR_TYPE");
			
			EvaluatorWrapperDao evaluatorWrapperDao = springContext.getBean(EvaluatorWrapperDao.class);
			TrainingSetWrapperDao trainingSetWrapperDao = springContext.getBean(TrainingSetWrapperDao.class);
			CircuitWrapperDao circuitWrapperDao = springContext.getBean(CircuitWrapperDao.class);
			
			EvaluatorWrapper evaluatorWrapper = evaluatorWrapperDao.findLatest(problem);
			TrainingSetWrapper trainingSetWrapper = trainingSetWrapperDao.findLatest(problem);
			
			Evaluator evaluator = evaluatorWrapper.getEvaluator();
			TrainingSet trainingSet = trainingSetWrapper.getTrainingSet();
			
			String query = evaluator.getByIndex(problem, trainingSetWrapper, 0);
			Circuit circuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);

			int[] output = CircuitOutputGenerator.generateOutput(trainingSet, circuit);
			
			
			for (char c = 'a'; c <= 'z'; c++) {
				dump(c, circuit, output);
			}
			
			for (char c = 'A'; c <= 'Z'; c++) {
				dump(c, circuit, output);
			}
			
			for (char c = '0'; c <= '9'; c++) {
				dump(c, circuit, output);
			}
			
			dump('á', circuit, output);
			dump('à', circuit, output);
			dump('ã', circuit, output);
			dump('â', circuit, output);
			
			dump('é', circuit, output);
			dump('ê', circuit, output);
			
			dump('í', circuit, output);
			
			dump('ó', circuit, output);
			dump('õ', circuit, output);
			dump('ô', circuit, output);
			
			dump('ú', circuit, output);

			
			


		} catch (BeansException e) {
			if (springContext != null) {
				((ClassPathXmlApplicationContext) springContext).close();
			}
		}
	}

	private static void dump(char c, Circuit circuit, int[] output) {
		
		String answer = null;
		
		try {
			answer = StringSolution.evaluate(circuit, output, Character.toString(c));
		} catch (Exception e) {
			answer = "error";
		}
		System.out.println(c + " [" + answer + "]");
		
	}
	
}
