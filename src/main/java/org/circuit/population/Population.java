package org.circuit.population;

import static org.circuit.Application.ADDITIONAL_INFO;

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
import org.circuit.port.Port;
import org.circuit.random.RandomWeight;
import org.circuit.solution.TrainingSet;
import org.circuit.time.Period;
import org.circuit.time.TimeMeasure;
import org.circuit.utils.CircuitUtils;
import org.circuit.utils.IoUtils;
import org.circuit.utils.RandomUtils;

public class Population {
	
	private static final Logger logger = Logger.getLogger(Population.class);
	
	private static final boolean CHECK_CONSISTENCY = false;
	
	private static final int PERIOD_ENRICHMENT = 20000;
	
	private static final double ENRICH_PERCENTAGE = 10d;
	
	private static RandomWeight<Method> methodChosser = new RandomWeight<Method>();
	
	static {
		methodChosser.addByWeight(5000, Method.RANDOM_ENRICH);
		methodChosser.addByWeight(2000, Method.BETTER_RANDOM_ENRICH);
		methodChosser.addByWeight(50, Method.SCRAMBLE_WITH_NEW_RANDOM);
		methodChosser.addByWeight(10, Method.CIRCUITS_SCRABLE);
		methodChosser.addByWeight(1, Method.RANDOM_CIRCUIT);

		methodChosser.addByPeriod(15 * TimeMeasure.MINUTE, Method.RANDOM_FROM_DATABASE);
		methodChosser.addByPeriod(2 * TimeMeasure.HOUR, Method.BETTER_FROM_DATABASE);
	}	
	
	public static void enrich(Context context, List<Circuit> population) {
		
		EvaluatorWrapper evaluatorWrapper = context.getEvaluatorWrapper();
		
		Period period = new Period(PERIOD_ENRICHMENT);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet();
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		
		final int inputSize =  context.getTrainingSetWrapper().getTrainingSet().getInputSize();

		while (!period.alarm()) {
			
			//long initial = System.currentTimeMillis();

			Circuit newCircuit = null;

			Method method = methodChosser.next();
			
			if (ADDITIONAL_INFO) {
				logger.info(String.format("Method: %s", method.name()));
			}
			
			TimeMeasure timeMeasure = new TimeMeasure();
			
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
				int raffled = RandomUtils.raffle(100);
				String query = evaluator.getByIndex(context.getProblem(), context.getTrainingSetWrapper(), raffled);
				newCircuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
				
				if (newCircuit == null) {
					logger.error(String.format("Fail to receive [%d] a new circuit from database!", raffled));
					continue;
				}
				logger.info(String.format("Received a new circuit from database [%d]!", raffled));
				
			}

				break;
			case BETTER_FROM_DATABASE: {
				CircuitWrapperDao circuitWrapperDao = Application.springContext.getBean(CircuitWrapperDao.class);
				String query = evaluator.getByIndex(context.getProblem(), context.getTrainingSetWrapper(), 0);
				newCircuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
				
				if (newCircuit == null) {
					logger.error(String.format("Fail to receive [%d] a new circuit from database!", 0));
					continue;
				}
				logger.info(String.format("Received a new circuit from database [%d]!", 0));
				
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

			if (ADDITIONAL_INFO) {
				logger.info(String.format("Circuit generated: %s", timeMeasure.elapsed()));
				timeMeasure.reset();
			}
			
			try {
				evaluator.evaluate(trainingSet, newCircuit);
			} catch (Throwable t) {
				logger.info(String.format("Method Name: " + method.name()));
				System.out.println(String.format("Object with error [%s]", IoUtils.objectToBase64(newCircuit)));
				throw new RuntimeException("Inconsistency!");
			}
			
			if (ADDITIONAL_INFO) {
				logger.info(String.format("Circuit evaluated:  %s", timeMeasure.elapsed()));
				timeMeasure.reset();
			}
			
			if (orderedAdd(population, evaluator.getComparator(), newCircuit) == 0) {
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
			
			if (ADDITIONAL_INFO) {
				logger.info(String.format("Circuit added to population:  %s", timeMeasure.elapsed()));
				timeMeasure.reset();
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

	
	public static int orderedAdd(List<Circuit> population, Comparator<Circuit> comparator, Circuit newCircuit) {
		
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
