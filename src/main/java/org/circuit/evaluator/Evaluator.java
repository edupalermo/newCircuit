package org.circuit.evaluator;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.circuit.circuit.Circuit;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.solution.TrainingSet;

public interface Evaluator {
	
	void evaluate(TrainingSet trainingSet, Circuit circuit);
	
	Comparator<Circuit> getComparator();
	
	List<Pair<String,Boolean>> getOrders();
	
	String getByIndex(Problem problem, TrainingSetWrapper trainingSetWrapper, int index);
	
	double similarity(Circuit c1, Circuit c2);

}
