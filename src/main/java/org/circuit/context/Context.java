package org.circuit.context;

import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;

public class Context {
	
	private final TrainingSetWrapper trainingSetWrapper;
	
	private final EvaluatorWrapper evaluatorWrapper;
	
	private final Problem problem;
	
	public Context(Problem problem, TrainingSetWrapper trainingSetWrapper, EvaluatorWrapper evaluatorWrapper) {
		this.trainingSetWrapper = trainingSetWrapper;
		this.evaluatorWrapper = evaluatorWrapper;
		this.problem = problem;
	}

	public TrainingSetWrapper getTrainingSetWrapper() {
		return trainingSetWrapper;
	}
	

	public EvaluatorWrapper getEvaluatorWrapper() {
		return evaluatorWrapper;
	}

	public Problem getProblem() {
		return problem;
	}

}
