package org.circuit.port;

import java.util.Map;
import java.util.Random;

public class PortMemory extends Port {

	private static final long serialVersionUID = 1L;
	
	private transient boolean memory = false;
	
	private int index;
	
	public PortMemory(int index) {
		this.index = index;
	}

	@Override
	public void reset() {
		this.memory = false;
	}

	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortMemory) {
			PortMemory portMemory = (PortMemory) obj;
			equals = this.index == portMemory.getIndex(); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		if (this.index > index) {
			this.index--;
		}
	}

	public int getIndex() {
		return index;
	}
	
	public boolean evaluate(boolean list[]) {
		boolean answer = this.memory;
		this.memory = list[this.index];
		return answer;
	}
	
	@Override
	public boolean references(int index) {
		return index == this.index;
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortMemory) {
			PortMemory portMemory = (PortMemory) port;
			answer = this.index - portMemory.getIndex();
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}

	public static PortMemory random(int size) {
		Random random = new Random();
		return new PortMemory(random.nextInt(size));
	}

	public String toString() {
		return "MEM[" + this.index + "]";
	}
	
	@Override
	public boolean checkConsistency(int index) {
		return (this.index < index);
	}

	@Override
	public Object clone() {
		return new PortMemory(this.index);
	}

	@Override
	public void adjust(int oldIndex, int newIndex) {
		if (this.index == oldIndex) {
			this.index = newIndex;
		}
	}

	@Override
	public void translate(Map<Integer, Integer> map) {
		this.index = map.get(this.index).intValue();
	}

}
