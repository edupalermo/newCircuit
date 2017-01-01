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
	
	private static final boolean CHECK_CONSISTENCY = true;
	
	private static final int PERIOD_ENRICHMENT = 10000;
	
	public static void enrich(Context context, List<Circuit> population) {
		
		EvaluatorWrapper evaluatorWrapper = context.getEvaluatorWrapper();
		
		RandomWeight<Method> methodChosser = new RandomWeight<Method>();
		methodChosser.add(10000, Method.RANDOM_ENRICH);
		methodChosser.add(50, Method.SCRAMBLE_WITH_NEW_RANDOM);
		methodChosser.add(10, Method.CIRCUITS_SCRABLE);
		methodChosser.add(5, Method.RANDOM_FROM_DATABASE);
		methodChosser.add(1, Method.RANDOM_CIRCUIT);

		Period period = new Period(PERIOD_ENRICHMENT);
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		TrainingSet trainingSet = context.getTrainingSetWrapper().getTrainingSet();
		Evaluator evaluator = context.getEvaluatorWrapper().getEvaluator();
		
		final int inputSize =  context.getTrainingSetWrapper().getTrainingSet().getInputSize();

		while (!period.alarm()) {

			Circuit newCircuit = null;

			Method method = methodChosser.next();
			switch (method) {
			case RANDOM_CIRCUIT:
				newCircuit = CircuitRandomGenerator.randomGenerate(inputSize, random.nextInt(300, 1000));
				break;
			case RANDOM_ENRICH:
				newCircuit = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();
				CircuitRandomGenerator.randomEnrich(newCircuit, 1 + ((15 * newCircuit.size()) / 100));
				break;
			case CIRCUITS_SCRABLE: {
				Circuit c1 = (Circuit) getCircuitRandomCircuitFromPopulation(population).clone();
				Circuit c2 = getCircuitRandomCircuitFromPopulation(population);

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
				Circuit c2 = CircuitRandomGenerator.randomGenerate(inputSize, random.nextInt(300, 1500));

				//logger.info(String.format("Method SCRAMBLE WITH RANDOM %d %d", c1.size(), c2.size()));
				if (c1.size() + c2.size() > 1500) {
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c1);
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, c2);
				}

				newCircuit = CircuitScramble.scramble(trainingSet, c1, c2);
			}

				break;
			case RANDOM_FROM_DATABASE: {
				logger.info("Ping!!!");
				CircuitWrapperDao circuitWrapperDao = Application.springContext.getBean(CircuitWrapperDao.class);
				String query = evaluator.getByIndex(context.getProblem(), context.getTrainingSetWrapper(), RandomUtils.raffle(10));
				newCircuit = circuitWrapperDao.findByQuery(evaluatorWrapper, query);
				
				if (newCircuit == null) {
					continue;
				}
				
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
				if (simplifiedCircuit.size() > 3000) {
					CircuitUtils.simplifyByRemovingUnsedPorts(trainingSet, simplifiedCircuit);
				}
				CircuitUtils.betterSimplify(trainingSet, simplifiedCircuit);
				evaluator.evaluate(trainingSet, simplifiedCircuit);
				
				orderedAdd(population, evaluator.getComparator(), simplifiedCircuit);
				
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
