package org.circuit.entity;

import java.time.LocalDateTime;

public class Grade {
	
	private int id;
	private TrainingSetWrapper trainingSetWrapper;
	private EvaluatorWrapper evaluatorWrapper;
	private String name;
	private int value;
	private LocalDateTime created;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public TrainingSetWrapper getTrainingSetWrapper() {
		return trainingSetWrapper;
	}
	
	public void setTrainingSetWrapper(TrainingSetWrapper trainingSetWrapper) {
		this.trainingSetWrapper = trainingSetWrapper;
	}
	
	public EvaluatorWrapper getEvaluatorWrapper() {
		return evaluatorWrapper;
	}
	
	public void setEvaluatorWrapper(EvaluatorWrapper evaluatorWrapper) {
		this.evaluatorWrapper = evaluatorWrapper;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public LocalDateTime getCreated() {
		return created;
	}
	
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	
}
