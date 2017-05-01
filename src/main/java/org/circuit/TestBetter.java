package org.circuit;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitOutputGenerator;
import org.circuit.circuit.CircuitToString;
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
import org.circuit.utils.CircuitUtils;
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
			
			dumpCircuit(evaluator, trainingSet, circuit);
			logger.info("***********************************************************************************");
			Circuit newCircuit = (Circuit) circuit.clone();
			//CircuitUtils.betterSimplify(trainingSet, newCircuit);
			CircuitUtils.removeOverhead(trainingSet, newCircuit);
			CircuitUtils.useLowerPortsWithSameOutput(trainingSet, newCircuit);
			dumpCircuit(evaluator, trainingSet, newCircuit);
			
			logger.info("***********************************************************************************");
			Circuit again = (Circuit) newCircuit.clone();
			CircuitUtils.betterSimplify(trainingSet, again);
			//CircuitUtils.useLowerPortsWithSameOutput(trainingSet, again);
			dumpCircuit(evaluator, trainingSet, again);
			
			//System.exit(0);
			
			
			
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
		logger.info(c + " [" + answer + "]");
		
	}
	
	public static void dumpCircuit(Evaluator evaluator, TrainingSet trainingSet, Circuit circuit) {
		evaluator.evaluate(trainingSet, circuit);
		logger.info("Better: " + CircuitToString.toString(evaluator, circuit));

		int[] output = CircuitOutputGenerator.generateOutput(trainingSet, circuit);
		logger.info("Output: " + Arrays.toString(output));
		
	}
	
}
