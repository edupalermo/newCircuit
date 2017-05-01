package org.circuit.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitOutputGenerator;
import org.circuit.pool.StatePool;
import org.circuit.port.Port;
import org.circuit.port.PortAnd;
import org.circuit.port.PortInput;
import org.circuit.port.PortMemorySetReset;
import org.circuit.port.PortNand;
import org.circuit.port.PortNor;
import org.circuit.port.PortNot;
import org.circuit.port.PortOr;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;
import org.circuit.solution.TrainingSet;

public class CircuitUtils {

	private static final Logger logger = Logger.getLogger(CircuitUtils.class);

	// Remove redundant ports
	public static void simplify(TrainingSet trainingSet, Circuit circuit) {

		Map<Integer, List<Integer>> same = new TreeMap<Integer, List<Integer>>(Collections.reverseOrder());

		for (Solution solution : trainingSet) {
			evaluateRepetition(circuit, same, solution);
		}
		
		
		//for (Map.Entry<Integer, List<Integer>> entry : same.entrySet()) {
		//	System.out.println(String.format("%d %s", entry.getKey(), entry.getValue().toString()));
		//}
		
		//int original = EvaluateHits.evaluate(circuit, solutions);
		
		final int inputSize = trainingSet.getInputSize();

		for (int i = inputSize; i < circuit.size(); i++) {
			for (Map.Entry<Integer, List<Integer>> entry : same.entrySet()) {
				if ((entry.getValue() != null) && (entry.getValue().size() > 0)) {
					if (circuit.get(i).references(entry.getKey().intValue())) {
						//Circuit backup = (Circuit) circuit.clone();
						//System.out.println(String.format("Adjusting %s %d %d ", circuit.get(i).toString(), entry.getKey().intValue(), entry.getValue().get(0).intValue()));
						circuit.get(i).adjust(entry.getKey().intValue(), entry.getValue().get(0).intValue());
						//System.out.println(String.format("Adjusted %s ", circuit.get(i).toString()));
						//if (EvaluateHits.evaluate(circuit, solutions) != original) {
						//	System.out.println(EvaluateHits.evaluate(backup, solutions));
						//}
					}
				}
			}
		}
		//System.out.println(EvaluateHits.evaluate(circuit, solutions));

		for (Map.Entry<Integer, List<Integer>> entry : same.entrySet()) {
			if (entry.getValue().size() > 0) {
				if (!(circuit.get(entry.getKey().intValue()) instanceof PortInput)) {
					circuit.removePort(entry.getKey().intValue());
					//System.out.println(EvaluateHits.evaluate(circuit, solutions));
				}
			}

			if (entry.getValue() != null) {
				entry.getValue().clear();
			}
		}

		same.clear();
	}
	
	
	public static void useLowerPortsWithSameOutput(TrainingSet trainingSet, Circuit circuit) {
		Map<Integer, List<Integer>> same = new TreeMap<Integer, List<Integer>>(Collections.reverseOrder());

		for (Solution solution : trainingSet) {
			evaluateRepetition(circuit, same, solution);
		}

//		for (Map.Entry<Integer, List<Integer>> entry : same.entrySet()) {
//			if (entry.getValue().size() > 0) {
//				logger.info(String.format("%d - %s", entry.getKey().intValue(), entry.getValue().toString()));
//			}
//		}
	
		final int inputSize = trainingSet.getInputSize();

		for (int i = inputSize; i < circuit.size(); i++) {
			for (Map.Entry<Integer, List<Integer>> entry : same.entrySet()) {
				if ((entry.getValue() != null) && (entry.getValue().size() > 0)) {
					if (circuit.get(i).references(entry.getKey().intValue())) {
						circuit.get(i).adjust(entry.getKey().intValue(), entry.getValue().get(0).intValue());
					}
				}
			}
		}
	}

	
	public static void removeOverhead(TrainingSet trainingSet, Circuit circuit) {
		int output[] =  CircuitOutputGenerator.generateOutput(trainingSet, circuit);
		
		int major = 0;
		
		for (int i : output) {
			major = Math.max(major, i);
		}
		
		while ((circuit.size() - 1) > major) {
			circuit.remove(circuit.size() - 1);
		}
		
	}

	
	
	// Remove ports not used by Output
	public static void simplifyByRemovingUnsedPorts(TrainingSet trainingSet, Circuit circuit) {

		int output[] =  CircuitOutputGenerator.generateOutput(trainingSet, circuit);

		TreeSet<Integer> canRemove = new TreeSet<Integer>();

		// Adding all ports!
		for (int i = 0; i < circuit.size(); i++) {
			if (!(circuit.get(i) instanceof PortInput)) {
				canRemove.add(i);
			}
		}

		// Removing port that can't be removed
		for (int i = 0; i < output.length; i++) {
			removeFromList(circuit, canRemove, output[i]);
		}

		for (Integer index : canRemove.descendingSet()) {
			circuit.removePort(index.intValue());
		}
	}

	
	private static void removeFromList(Circuit circuit, Set<Integer> canRemove, int index) {
		Port port = circuit.get(index); 
		if (!(port instanceof PortInput)) {
			if (!canRemove.remove(index)) { // If did not have then it has been removed before and don't need to continue
				return;
			}

			if (port instanceof PortAnd) {
				removeFromList(circuit, canRemove, ((PortAnd) port).getMinor());
				removeFromList(circuit, canRemove, ((PortAnd) port).getMajor());
			} else if (port instanceof PortOr) {
				removeFromList(circuit, canRemove, ((PortOr) port).getMinor());
				removeFromList(circuit, canRemove, ((PortOr) port).getMajor());
			} else if (port instanceof PortNand) {
				removeFromList(circuit, canRemove, ((PortNand) port).getMinor());
				removeFromList(circuit, canRemove, ((PortNand) port).getMajor());
			} else if (port instanceof PortNor) {
				removeFromList(circuit, canRemove, ((PortNor) port).getMinor());
				removeFromList(circuit, canRemove, ((PortNor) port).getMajor());
			} else if (port instanceof PortNot) {
				removeFromList(circuit, canRemove, ((PortNot) port).getIndex());
			} else if (port instanceof PortMemorySetReset) {
				removeFromList(circuit, canRemove, ((PortMemorySetReset) port).getMinor());
				removeFromList(circuit, canRemove, ((PortMemorySetReset) port).getMajor());
			} else {
				throw new RuntimeException("Inconsistency!");
			}

		}
	}


	private static void evaluateRepetition(Circuit circuit, Map<Integer, List<Integer>> same, Solution solution) {
		boolean state[] = null;
		
		try {
			state = StatePool.borrow(circuit.size());
			circuit.reset();

			for (TimeSlice timeSlice : solution) {
				circuit.assignInputToState(state, timeSlice.getInput());
				circuit.propagate(state);

				boolean firstTime = same.size() == 0;

				for (int i = 1; i < circuit.size(); i++) {
					List<Integer> list = same.get(Integer.valueOf(i));
					if (list == null) {
						list = new ArrayList<Integer>();
						same.put(Integer.valueOf(i), list);
					}

					if (firstTime) {
						for (int j = 0; j < i; j++) {
							if (state[i] == state[j]) {
								list.add(Integer.valueOf(j));
							}
						}
					} else {
						Iterator<Integer> it = list.iterator();
						while (it.hasNext()) {
							int j = it.next().intValue();
							if (state[i] != state[j]) {
								it.remove();
							}
						}

					}
				}
				
			}
		} finally {
			StatePool.retrieve(state);
		}
	}
	
	public static int getTotalOfPossibleHits(TrainingSet trainingSet) {
		int answer = 0;
		
		for (Solution solution : trainingSet) {
			for (TimeSlice timeSlice : solution) {
				answer = answer + timeSlice.getOutput().length;
			}
		}
		
		return answer;
	}


	public static void betterSimplify(TrainingSet trainingSet, Circuit circuit) {
		useLowerPortsWithSameOutput(trainingSet, circuit);
		simplifyByRemovingUnsedPorts(trainingSet, circuit);
	}

}
