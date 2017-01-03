package org.circuit.dao;

import java.util.List;

import org.circuit.circuit.Circuit;
import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;

public interface CircuitWrapperDao {
	
    CircuitWrapper create(Problem problem, Circuit circuit);

    Circuit findByQuery(EvaluatorWrapper evaluatorWrapper, String sql);
    
	List<CircuitWrapper> findWithoutGrades(EvaluatorWrapper evaluatorWrapper, TrainingSetWrapper trainingSetWrapper);

}
