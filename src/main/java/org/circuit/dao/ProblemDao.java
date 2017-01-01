package org.circuit.dao;

import org.circuit.entity.Problem;

public interface ProblemDao {
	
    Problem findByName(String name);
    
    Problem getById(int id);

}
