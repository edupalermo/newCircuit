package org.circuit;

import org.circuit.dao.EvaluatorWrapperDao;
import org.circuit.dao.ProblemDao;
import org.circuit.entity.Problem;
import org.circuit.problem.vowel.WorkingEvaluator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RecordEvaluator {
	//private static final Logger logger = Logger.getLogger(RecordEvaluator.class);
	
	public static void main(String[] args) {
		
		ApplicationContext context = null;
		
		try {
			context = new ClassPathXmlApplicationContext("application-context.xml");

			ProblemDao problemDao = context.getBean(ProblemDao.class);
			
			Problem problem = problemDao.findByName("CHAR_TYPE");
			
			EvaluatorWrapperDao evaluatorWrapperDao = context.getBean(EvaluatorWrapperDao.class);
			
			evaluatorWrapperDao.create(problem, new WorkingEvaluator());
			
			//EvaluatorWrapper evaluatorWrapeer = evaluatorWrapperDao.findLatest();
			//logger.info("Id: " + evaluatorWrapeer.getId());
			
			
		} catch (BeansException e) {
			if (context != null) {
				((ClassPathXmlApplicationContext) context).close();
			}
		}
		
		
	}
}
