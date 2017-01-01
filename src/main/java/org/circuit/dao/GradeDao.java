package org.circuit.dao;

import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.TrainingSetWrapper;

public interface GradeDao {
	
    void create(CircuitWrapper circuitWrapper, TrainingSetWrapper trainingSetWrapper, EvaluatorWrapper evaluatorWrapper, String name, int value);

}
