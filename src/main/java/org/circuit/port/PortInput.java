package org.circuit.port;

import java.util.Map;

public class PortInput extends Port {

	private static final long serialVersionUID = 1L;
	
	private final int index;
	
	public PortInput(int index) {
		this.index = index;
	}
	
	@Override
	public void reset() {};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortInput) {
			PortInput portInput = (PortInput) obj;
			equals = this.index == portInput.getIndex(); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		throw new RuntimeException("Inconsistency");
	}

	public int getIndex() {
		return index;
	}
	

	public boolean evaluate(boolean list[]) {
		return list[this.index];
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortInput) {
			PortInput portInput = (PortInput) port;
			answer = this.index - portInput.getIndex();
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}
	
	@Override
	public boolean references(int index) {
		return false;
	}


	public String toString() {
		return "INPUT[" + this.index + "]";
	}
	
	@Override
	public boolean checkConsistency(int index) {
		return true;
	}

	@Override
	public Object clone() {
		return new PortInput(this.index);
	}
	
	@Override
	public void adjust(int oldIndex, int newIndex) {
		throw new RuntimeException("Inconsistency");
	}
	
	@Override
	public void translate(Map<Integer, Integer> map) {
		throw new RuntimeException("Inconsistency");
	}

}
