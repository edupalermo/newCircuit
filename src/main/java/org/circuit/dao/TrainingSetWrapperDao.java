package org.circuit.dao;

import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.solution.TrainingSet;

public interface TrainingSetWrapperDao {
	
    TrainingSetWrapper create(Problem problem, TrainingSet traningSet);

    TrainingSetWrapper findLatest(Problem problem);

}
