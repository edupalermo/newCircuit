package org.circuit.population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.circuit.Application;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitRandomGenerator;
import org.circuit.circuit.CircuitScramble;
import org.circuit.context.Context;
import org.circuit.dao.CircuitWrapperDao;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.method.Method;
import org.circuit.period.Period;
import org.circuit.port.Port;
import org.circuit.random.RandomWeight;
import org.circuit.solution.TrainingSet;
import org.circuit.utils.CircuitUtils;
import org.circuit.utils.IoUtils;
import org.circuit.utils.RandomUtils;

public class Population {
	
	private static final Logger logger = Logger.getLogger(Population.class);
	
	private static final boolean CHECK_CONSISTENCY = false;
	
	private static final int PERIOD_ENRICHMENT = 10000;
	
	private static final double ENRICH_PERCENTAGE = 10d;
	
	private static long lastDatabaseQuery = System.currentTimeMillis();
	
	private static final long PERIOD_DATABASE_QUERY = 5 * 60 * 1000;
	
	public static void enrich(Context context, List<Circuit> population) {
		
		EvaluatorWrapper evaluatorWrapper = context.getEvaluatorWrapper();
		
		RandomWeight<Method> methodChosser = new RandomWeight<Method>();
		methodChosser.add(5000, Method.RANDOM_ENRICH);
		methodChosser.add(5000, Method.BETTER_RANDOM_ENRICH);
		methodChosser.add(50, Method.SCRAMBLE_WITH_NEW_RANDOM);
		methodChosser.add(10, Method.CIRCUITS_SCRABLE);
		methodChosser.add(1, Method.RANDOM_FROM_DATABASE);
		methodChosser.add(1, Method.RANDOM_CIRCUIT);

		Period period = new Period(PERIOD_ENRICHMENT);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet();
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		
		final int inputSize =  context.getTrainingSetWrapper().getTrainingSet().getInputSize();

		while (!period.alarm()) {
			
			//long initial = System.currentTimeMillis();

			Circuit newCircuit = null;

			Method method = null;
			if (System.currentTimeMillis() - lastDatabaseQuery > PERIOD_DATABASE_QUERY) {
				method = Method.RANDOM_FROM_DATABASE;
				lastDatabaseQuery = System.currentTimeMillis();
			}
			else {
				method = methodChosser.next();
			}
			
			switch (method) {
			case RANDOM_CIRCUIT:
				newCircuit = CircuitRandomGenerator.randomGenerate(inputSize, random.nextInt(300, 1000), context.getProblem().getUseMemory());
				break;
			case RANDOM_ENRICH:
				newCircuit = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();
				CircuitRandomGenerator.randomEnrich(newCircuit, (int)(1 + ((ENRICH_PERCENTAGE * newCircuit.size()) / 100)), context.getProblem().getUseMemory());
				break;
			case BETTER_RANDOM_ENRICH:
				newCircuit = (Circuit) population.get(0).clone();
				CircuitRandomGenerator.randomEnrich(newCircuit, (int)(1 + ((ENRICH_PERCENTAGE * newCircuit.size()) / 100)), context.getProblem().getUseMemory());
				break;
			case CIRCUITS_SCRABLE: {
				Circuit c1 = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();
				Circuit c2 = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();

				//logger.info(String.format("Method SCRAMBLE %d %d", c1.size(), c2.size()));
				if (c1.size() + c2.size() > 1500) {
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c1);

					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c2);
				}
				newCircuit = CircuitScramble.scramble(trainingSet, c1, c2);
			}

				break;
			case SCRAMBLE_WITH_NEW_RANDOM: {
				Circuit c1 = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();
				Circuit c2 = CircuitRandomGenerator.randomGenerate(inputSize, random.nextInt(300, 1500), context.getProblem().getUseMemory());

				//logger.info(String.format("Method SCRAMBLE WITH RANDOM %d %d", c1.size(), c2.size()));
				if (c1.size() + c2.size() > 1500) {
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c1);
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c2);
				}

				newCircuit = CircuitScramble.scramble(trainingSet, c1, c2);
			}

				break;
			case RANDOM_FROM_DATABASE: {
				CircuitWrapperDao circuitWrapperDao = Application.springContext.getBean(CircuitWrapperDao.class);
				int raffled = RandomUtils.raffle(50);
				String query = evaluator.getByIndex(context.getProblem(), context.getTrainingSetWrapper(), raffled);
				newCircuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
				
				if (newCircuit == null) {
					logger.error("Fail to receive a new circuit from database!");
					continue;
				}
				logger.info(String.format("Received a new circuit from database [%d]!", raffled));
				
			}

				break;
			default:
				throw new RuntimeException("Inconsistency");
			}

			if (CHECK_CONSISTENCY) {
				for (int i = 0; i < newCircuit.size(); i++) {
					Port port = newCircuit.get(i);
					port.checkConsistency(i);
				}
			}

			try {
				evaluator.evaluate(trainingSet, newCircuit);
			} catch (Throwable t) {
				logger.info(String.format("Method Name: " + method.name()));
				System.out.println(String.format("Object with error [%s]", IoUtils.objectToBase64(newCircuit)));
				throw new RuntimeException("Inconsistency!");
			}
			
			Circuit lastBetter = population.get(0);
			
			if (orderedAdd(population, evaluator.getComparator(), newCircuit) == 0) {
				Circuit simplifiedCircuit = (Circuit) newCircuit.clone();
				simplifiedCircuit = CircuitScramble.join(trainingSet, simplifiedCircuit, lastBetter);
				if (simplifiedCircuit.size() > 3000) { // This is done in better simplify, but some time it is better to do it first or we can run out of memory
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, simplifiedCircuit);
				}
				CircuitUtils.betterSimplify(trainingSet, simplifiedCircuit);
				evaluator.evaluate(trainingSet, simplifiedCircuit);
				
				orderedAdd(population, evaluator.getComparator(), simplifiedCircuit);
				
			}
			
			//logger.info(String.format("Took %d", (System.currentTimeMillis() - initial)));

		}

	}
	
	public static void genocide(Context context, List<Circuit> population, int populationLimit) {
		if (population.size() < populationLimit) {
			return;
		}
		
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet();
		Comparator<Circuit> comparator = evaluator.getComparator();
		int inputSize = context.getTrainingSetWrapper().getTrainingSet().getInputSize();
		
		Circuit betterCircuit = population.get(0);
		Circuit worstCircuit = population.get(populationLimit - 1);
		
		if (evaluator.similarity(betterCircuit, worstCircuit) > 0.5d) {
			logger.warn("Genocide is taking place...");
			
			for (Circuit c : population) {
				c.clear();
			}
			population.clear();
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			for (int i = 0; i < populationLimit / 10; i++) {
				Circuit newCircuit = CircuitRandomGenerator.randomGenerate(inputSize, random.nextInt(300, 1000), context.getProblem().getUseMemory());
				evaluator.evaluate(trainingSet, newCircuit);
				orderedAdd(population, comparator, newCircuit);
			}
		}
	}

	
	private static int orderedAdd(List<Circuit> population, Comparator<Circuit> comparator, Circuit newCircuit) {
		
		if (newCircuit == null) {
			throw new RuntimeException("Cannot add null circuit!");
		}
		
		if (comparator == null) {
			throw new RuntimeException("Comparator cannot be null!");
		}
		
		if (population == null) {
			throw new RuntimeException("Population cannot be null!");
		}
		
		int pos = Collections.binarySearch(population, newCircuit, comparator);
		if (pos < 0) {
			pos = ~pos;
			population.add(pos, newCircuit);
		} else {
			pos = -1;
		}
		return pos;
	}
	
	private static Circuit getCircuitRandomCircuitFromPopulation(List<Circuit> population) {
		return population.get(RandomUtils.raffle(population.size()));
	}

	public static List<Circuit> reEvaluatePopulation(Context context, List<Circuit> oldPopulation) {
		ArrayList<Circuit> newPopulation = new ArrayList<Circuit>();
		
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet();
		Comparator<Circuit> comparator = evaluator.getComparator(); 
		
		for (Circuit circuit : oldPopulation) {
			evaluator.evaluate(trainingSet, circuit);
			
			orderedAdd(newPopulation, comparator, circuit);
		}
		
		return newPopulation;
		
	}

}
