package org.circuit.dao;

import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.evaluator.Evaluator;

public interface EvaluatorWrapperDao {
	
    EvaluatorWrapper create(Problem problem, Evaluator evaluator);

    EvaluatorWrapper findLatest(Problem problem);

}
