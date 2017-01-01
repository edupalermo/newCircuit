package org.circuit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitRandomGenerator;
import org.circuit.circuit.CircuitToString;
import org.circuit.context.Context;
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
import org.circuit.population.Population;
import org.circuit.solution.TrainingSet;
import org.circuit.utils.CircuitUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Hive {

	private static final Logger logger = Logger.getLogger(Hive.class);

	private static final int POPULATION_LIMIT = 10000;
	
	public static void main(String[] args) {

		try {
			Application.springContext = new ClassPathXmlApplicationContext("application-context.xml");

			ProblemDao problemDao = Application.springContext.getBean(ProblemDao.class);

			Problem problem = problemDao.findByName("CHAR_TYPE");
			
			hive(problem);

		} catch (BeansException e) {
			if (Application.springContext != null) {
				((ClassPathXmlApplicationContext) Application.springContext).close();
			}
		}
	}
	
	private static Context getLatestContext(Problem problem) {
		TrainingSetWrapper trainingSetWrapper = Application.springContext.getBean(TrainingSetWrapperDao.class).findLatest(problem); 
		EvaluatorWrapper evaluatorWrapper = Application.springContext.getBean(EvaluatorWrapperDao.class).findLatest(problem);
		
		if (trainingSetWrapper == null) {
			throw new RuntimeException("No training set in the database");
		}
		
		if (evaluatorWrapper == null) {
			throw new RuntimeException("No evaluator set in the database");
		}
		
		return new Context(problem, trainingSetWrapper, evaluatorWrapper);
	}

	private static void hive(Problem problem) {
		
		Context context = getLatestContext(problem);
		List<Circuit> population = new ArrayList<Circuit>();
		

		Circuit newCircuit  = getAdao(context);
		context.getEvaluatorWrapper().getEvaluator().evaluate(context.getTrainingSetWrapper().getTrainingSet(), newCircuit);
		population.add(newCircuit);

		Circuit lastBetter = population.get(0);
		
		for (;;) {
			Population.enrich(context, population);
			dump(context, population);
			limitPopulation(population);
			
			persistIfThereIsNewChamp(population, context, lastBetter);
			lastBetter = population.get(0);
			
			Context newContext = getLatestContext(problem);
			if (contextChanged(context, newContext)) {
				logger.info("Detected a change in the Context. Reevaluating the population.");
				context = newContext;
				population = Population.reEvaluatePopulation(newContext, population);
			}
		}
	}
	
	private static boolean contextChanged(Context actualContext, Context newContext) {
		boolean changed = false;
		
		if (actualContext.getEvaluatorWrapper().getId() != newContext.getEvaluatorWrapper().getId()) {
			changed = true;
		}
		else if (actualContext.getTrainingSetWrapper().getId() != newContext.getTrainingSetWrapper().getId()) {
			changed = true;
		}
		
		return changed;
	}
	
	private static void persistIfThereIsNewChamp(List<Circuit> population, Context context, Circuit lastBetter) {
		EvaluatorWrapper evaluatorWrapper =context.getEvaluatorWrapper(); 
		Evaluator evaluator = evaluatorWrapper.getEvaluator();
		Comparator<Circuit> comparator = evaluator.getComparator();
		TrainingSetWrapper trainingSetWrapper = context.getTrainingSetWrapper();
		Problem problem = context.getProblem();
				
		Circuit actualBetter = population.get(0);
		if (actualBetter != lastBetter && (comparator.compare(actualBetter, lastBetter) < 0)) {
			CircuitWrapperDao circuitWrapperDao = Application.springContext.getBean(CircuitWrapperDao.class);
			CircuitWrapper circuitWrapper = circuitWrapperDao.create(problem, actualBetter);
					
			GradeDao gradeDao = Application.springContext.getBean(GradeDao.class);
			
			for (Pair<String, Boolean> pair: evaluator.getOrders()) {
				gradeDao.create(circuitWrapper, trainingSetWrapper, evaluatorWrapper, pair.getLeft(), actualBetter.getBuffer(pair.getLeft(), Integer.class).intValue());
			}
			
		}
	}
	
	public static void dump(Context context, List<Circuit> population) {
		
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet(); 
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		
		logger.info("====================================================================");
		for (int i = 0; i < Math.min(30, population.size()); i++) {
			logger.info(String.format("[%5d] %s", i + 1, CircuitToString.toSmallString(evaluator, population.get(i))));
		}

		if (population.size() > 3) {
			int limit = Math.min(POPULATION_LIMIT, population.size());
			for (int i = limit - 3; i < limit; i++) {
				logger.info(String.format("[%5d] %s", i + 1, CircuitToString.toSmallString(evaluator, population.get(i))));
			}
		}

		DecimalFormat myFormatter = new DecimalFormat("###,###");
		int populationSize = population.size();
		int totalHits = CircuitUtils.getTotalOfPossibleHits(trainingSet);
		String quantityOfPorts = myFormatter.format(sumTotalOfPort(population));
		
		logger.info(String.format("Population [%d] Total Hits [%d] Total of Ports [%s]", populationSize, totalHits, quantityOfPorts));

	}
	
	private static int sumTotalOfPort(List<Circuit> population) {
		int total = 0;
		
		for (Circuit circuit : population) {
			total += circuit.size();
		}
		return total;
	}
	
	private static void limitPopulation(List<Circuit> population) {
		while (population.size() > POPULATION_LIMIT) {
			Circuit circuit = population.remove(population.size() - 1);
			circuit.clear();
		}
	}

	
	private static Circuit getAdao(Context context) {
		Circuit circuit = getBetterFromDatabase(context);
		
		if (circuit == null) {
			final int inputSize = context.getTrainingSetWrapper().getTrainingSet().getInputSize();
			circuit  = CircuitRandomGenerator.randomGenerate(inputSize, 500);
		}
		return circuit;
	}
	
	private static Circuit getBetterFromDatabase(Context context) {
		Circuit circuit = null;
		
		EvaluatorWrapper evaluatorWrapper = context.getEvaluatorWrapper();
		Evaluator evaluator = evaluatorWrapper.getEvaluator();
		
		CircuitWrapperDao circuitWrapperDao = Application.springContext.getBean(CircuitWrapperDao.class);
		String query = evaluator.getByIndex(context.getProblem(), context.getTrainingSetWrapper(), 0);
		circuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
		
		return circuit;
	}

}
