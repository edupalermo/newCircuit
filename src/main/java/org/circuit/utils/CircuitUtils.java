package org.circuit.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitOutputGenerator;
import org.circuit.pool.StatePool;
import org.circuit.port.Port;
import org.circuit.port.PortAnd;
import org.circuit.port.PortInput;
import org.circuit.port.PortMemory;
import org.circuit.port.PortNand;
import org.circuit.port.PortNor;
import org.circuit.port.PortNot;
import org.circuit.port.PortOr;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;
import org.circuit.solution.TrainingSet;

public class CircuitUtils {

	// private static final Logger logger = LoggerFactory.getLogger(CircuitUtils.class);

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
			simplify(circuit, canRemove, output[i]);
		}

		for (Integer index : canRemove.descendingSet()) {
			circuit.removePort(index.intValue());
		}
	}

	
	private static void simplify(Circuit circuit, Set<Integer> canRemove, int index) {
		Port port = circuit.get(index); 
		if (!(port instanceof PortInput)) {
			canRemove.remove(index);

			if (port instanceof PortAnd) {
				simplify(circuit, canRemove, ((PortAnd) port).getMinor());
				simplify(circuit, canRemove, ((PortAnd) port).getMajor());
			} else if (port instanceof PortOr) {
				simplify(circuit, canRemove, ((PortOr) port).getMinor());
				simplify(circuit, canRemove, ((PortOr) port).getMajor());
			} else if (port instanceof PortNand) {
				simplify(circuit, canRemove, ((PortNand) port).getMinor());
				simplify(circuit, canRemove, ((PortNand) port).getMajor());
			} else if (port instanceof PortNor) {
				simplify(circuit, canRemove, ((PortNor) port).getMinor());
				simplify(circuit, canRemove, ((PortNor) port).getMajor());
			} else if (port instanceof PortNot) {
				simplify(circuit, canRemove, ((PortNot) port).getIndex());
			} else if (port instanceof PortMemory) {
				simplify(circuit, canRemove, ((PortMemory) port).getIndex());
			}
			else {
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
