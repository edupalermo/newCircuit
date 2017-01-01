package org.circuit.dao;

import org.circuit.circuit.Circuit;
import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;

public interface CircuitWrapperDao {
	
    CircuitWrapper create(Problem problem, Circuit circuit);

    Circuit findByQuery(EvaluatorWrapper evaluatorWrapper, String sql);

}
