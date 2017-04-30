package org.circuit;

import org.circuit.dao.ProblemDao;
import org.circuit.dao.TrainingSetWrapperDao;
import org.circuit.entity.Problem;
import org.circuit.problem.vowel.WorkingTrainingSet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RecordNewTraningSet {
	//private static final Logger logger = Logger.getLogger(RecordNewTraningSet.class);
	
	public static void main(String[] args) {
		
		ApplicationContext context = null;
		
		try {
			context = new ClassPathXmlApplicationContext("application-context.xml");

			ProblemDao problemDao = context.getBean(ProblemDao.class);
			
			Problem problem = problemDao.findByName("CHAR_TYPE");
			
			TrainingSetWrapperDao trainingSetWrapperDao = context.getBean(TrainingSetWrapperDao.class);
			
			trainingSetWrapperDao.create(problem, new WorkingTrainingSet());
			
			//TrainingSetWrapper trainingSetWrapeer = trainingSetWrapperDao.findLatest();
			
			//logger.info(trainingSetWrapeer);
			
			
			
		} catch (BeansException e) {
			if (context != null) {
				((ClassPathXmlApplicationContext) context).close();
			}
		}
		
		
	}
}
