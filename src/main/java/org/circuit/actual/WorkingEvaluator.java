package org.circuit.actual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitHitsEvaluator;
import org.circuit.circuit.CircuitToString;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.solution.TrainingSet;

public class WorkingEvaluator implements Evaluator, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final static String GRADE_HIT = "GRADE_HIT";
	private final static String GRADE_CIRCUIT_SIZE = "GRADE_CIRCUIT_SIZE";

	@Override
	public void evaluate(TrainingSet trainingSet, Circuit circuit) {
		circuit.setBuffer(GRADE_HIT, CircuitHitsEvaluator.evaluate(trainingSet, circuit));
		circuit.setBuffer(GRADE_CIRCUIT_SIZE, Integer.valueOf(circuit.size()));
	}
	
	@Override
	public Comparator<Circuit> getComparator() {
		return new Comparator<Circuit> () {

			@Override
			public int compare(Circuit c1, Circuit c2) {
				
				int answer = 0;
				
				for (Pair<String, Boolean> pair : getOrders()) {
					
					if (pair.getRight().booleanValue()) {
						answer = c1.getBuffer(pair.getLeft(), Integer.class).compareTo(c2.getBuffer(pair.getLeft(), Integer.class));
						if (answer != 0) {
							return answer;
						}
						
					}
					else {
						answer = c2.getBuffer(pair.getLeft(), Integer.class).compareTo(c1.getBuffer(pair.getLeft(), Integer.class));
						if (answer != 0) {
							return answer;
						}
						
					}
					
				}
				
				return CircuitToString.toString(getOuter(), c1).compareTo(CircuitToString.toString(getOuter(), c2));
			}
			
		};
		
	}
	
	private Evaluator getOuter() {
		return this;
	}
	
	private List<Pair<String, Boolean>> orders = null; 

	@Override
	public List<Pair<String, Boolean>> getOrders() {
		if (orders != null) {
			return orders;
		}
		
		orders = new ArrayList<Pair<String, Boolean>>();
		orders.add(Pair.of(GRADE_HIT, Boolean.FALSE));
		orders.add(Pair.of(GRADE_CIRCUIT_SIZE, Boolean.TRUE));
		return orders;
	}

	@Override
	public String getByIndex(Problem problem, TrainingSetWrapper trainingSetWrapper, int index) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(" select "); 
		sb.append("   c.object, ");
		sb.append("   (select g.value from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.training_set_id = :trainingSetId and g.name = 'GRADE_HIT') GRADE_HIT, ");
		sb.append("   (select g.value from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.training_set_id = :trainingSetId and g.name = 'GRADE_CIRCUIT_SIZE') GRADE_CIRCUIT_SIZE ");
		sb.append(" from ");
		sb.append("    circuit c ");
		sb.append(" where ");
		sb.append("    c.problem_id = ").append(problem.getId()).append(" ");
		sb.append(" order by ");
		sb.append("    GRADE_HIT asc, GRADE_CIRCUIT_SIZE desc OFFSET ").append(index).append(" ROWS FETCH NEXT 1 ROWS ONLY");
		return sb.toString();
	}

	

}
