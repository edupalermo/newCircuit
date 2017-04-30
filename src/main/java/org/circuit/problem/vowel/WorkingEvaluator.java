package org.circuit.problem.vowel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitHitsEvaluator;
import org.circuit.circuit.CircuitToString;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.evaluator.Evaluator;
import org.circuit.solution.TrainingSet;

public class WorkingEvaluator implements Evaluator, Serializable {
	
	private final static Logger logger = Logger.getLogger(WorkingEvaluator.class);

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
				
				
				if (c1.size() != c2.size()) {
					logger.warn("They should have the same size!");
					logger.warn(CircuitToString.toString(getOuter(), c1));
					logger.warn(CircuitToString.toString(getOuter(), c2));
				}
				
				int i = c1.size() - 1;
				while ((answer == 0) && (i >= 0)) {
					answer = c1.get(i).toString().compareTo(c2.get(i).toString());
					i--;
				}
				
				return answer;
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
		sb.append("   (select g.value from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.evaluator_id = :evaluatorId and g.name = 'GRADE_HIT') GRADE_HIT, ");
		sb.append("   (select g.value from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.evaluator_id = :evaluatorId and g.name = 'GRADE_CIRCUIT_SIZE') GRADE_CIRCUIT_SIZE ");
		sb.append(" from ");
		sb.append("    circuit c ");
		sb.append(" where ");
		sb.append("    c.problem_id = ").append(problem.getId()).append(" ");
		sb.append(" and ");
		sb.append("    exists (select 1 from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.evaluator_id = :evaluatorId and g.name = 'GRADE_CIRCUIT_SIZE') ");
		sb.append(" and ");
		sb.append("    exists (select 1 from grade g where g.circuit_id = c.circuit_id and g.training_set_id = ").append(trainingSetWrapper.getId()).append(" and g.evaluator_id = :evaluatorId and g.name = 'GRADE_HIT') ");
		sb.append(" order by ");
		sb.append("    GRADE_HIT desc, GRADE_CIRCUIT_SIZE asc OFFSET ").append(index).append(" ROWS FETCH NEXT 1 ROWS ONLY");
		return sb.toString();
	}

	@Override
	public double similarity(Circuit c1, Circuit c2) {
		
		if (c1.getBuffer(GRADE_HIT, Integer.class).intValue() != c2.getBuffer(GRADE_HIT, Integer.class).intValue()) {
			return 0d;
		}
		
		int dist = getLevenshteinDistance(c1, c2);
		int max = Math.max(c1.size(), c2.size());
		
		return ((double)max - (double)dist) / (double)max;
	}
	
	public int getLevenshteinDistance (Circuit lhs, Circuit rhs) {                          
	    int len0 = lhs.size() + 1;                                                     
	    int len1 = rhs.size() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.get(i - 1).equals(rhs.get(j-1))) ? 0 : 1;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}


	
	

}
