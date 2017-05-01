package org.circuit;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitToString;
import org.circuit.dao.CircuitWrapperDao;
import org.circuit.dao.EvaluatorWrapperDao;
import org.circuit.dao.GradeDao;
import org.circuit.dao.ProblemDao;
import org.circuit.dao.TrainingSetWrapperDao;
import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.solution.TrainingSet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseUpdateCircuits {
	
	private static final Logger logger = Logger.getLogger(DatabaseUpdateCircuits.class);

	private static ApplicationContext springContext = null;
	
	public static void main(String[] args) {

		try {
			springContext = new ClassPathXmlApplicationContext("application-context.xml");

			ProblemDao problemDao = springContext.getBean(ProblemDao.class);

			Problem problem = problemDao.findByName("CHAR_TYPE");
			
			EvaluatorWrapperDao evaluatorWrapperDao = springContext.getBean(EvaluatorWrapperDao.class);
			TrainingSetWrapperDao trainingSetWrapperDao = springContext.getBean(TrainingSetWrapperDao.class);
			CircuitWrapperDao circuitWrapperDao = springContext.getBean(CircuitWrapperDao.class);
			GradeDao gradeDao = springContext.getBean(GradeDao.class);
			
			EvaluatorWrapper evaluatorWrapper = evaluatorWrapperDao.findLatest(problem);
			TrainingSetWrapper trainingSetWrapper = trainingSetWrapperDao.findLatest(problem);
			
			Evaluator evaluator = evaluatorWrapper.getEvaluator();
			TrainingSet trainingSet = trainingSetWrapper.getTrainingSet();
			
			
			
			List<CircuitWrapper> list = circuitWrapperDao.findWithoutGrades(evaluatorWrapper, trainingSetWrapper);
			
			for (CircuitWrapper circuitWrapper : list) {
				Circuit circuit = circuitWrapper.getCircuit();
				evaluator.evaluate(trainingSet, circuit);
				for (Pair<String, Boolean> pair : evaluator.getOrders()) {
					gradeDao.create(circuitWrapper, trainingSetWrapper, evaluatorWrapper, pair.getKey(), circuit.getGrade(pair.getKey(), Integer.class).intValue());
				}
				
				logger.info(String.format("%s", CircuitToString.toSmallString(evaluator, circuit)));
			}

		} catch (BeansException e) {
			if (springContext != null) {
				((ClassPathXmlApplicationContext) springContext).close();
			}
		}
	}

}
