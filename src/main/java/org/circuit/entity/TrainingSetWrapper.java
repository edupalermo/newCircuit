package org.circuit.entity;

import java.time.LocalDateTime;

import org.circuit.solution.TrainingSet;

public class TrainingSetWrapper {
	
	private int id;
	private LocalDateTime created;
	private Problem problem;
	private TrainingSet trainingSet;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public TrainingSet getTrainingSet() {
		return trainingSet;
	}
	
	public void setTrainingSet(TrainingSet trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	public LocalDateTime getCreated() {
		return created;
	}
	
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public Problem getProblem() {
		return problem;
	}
	

	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	
}
