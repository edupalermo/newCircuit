package org.circuit.entity;

import java.time.LocalDateTime;

import org.circuit.circuit.Circuit;

public class CircuitWrapper {
	
	private int id;
	private LocalDateTime created;
	private Problem problem;
	private Circuit circuit;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;
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
