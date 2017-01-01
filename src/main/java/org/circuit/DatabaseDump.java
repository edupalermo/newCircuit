package org.circuit;

import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitToString;
import org.circuit.dao.CircuitWrapperDao;
import org.circuit.dao.EvaluatorWrapperDao;
import org.circuit.dao.ProblemDao;
import org.circuit.dao.TrainingSetWrapperDao;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.solution.TrainingSet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseDump {
	
	private static final Logger logger = Logger.getLogger(DatabaseDump.class);

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

			for (int i = 0; i < 30; i++) {
				String query = evaluator.getByIndex(problem, trainingSetWrapper, i);
				Circuit circuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
				evaluator.evaluate(trainingSet, circuit);
				
				logger.info(String.format("[%d] %s", (i + 1), CircuitToString.toSmallString(evaluator, circuit)));
			}

		} catch (BeansException e) {
			if (springContext != null) {
				((ClassPathXmlApplicationContext) springContext).close();
			}
		}
	}

}
