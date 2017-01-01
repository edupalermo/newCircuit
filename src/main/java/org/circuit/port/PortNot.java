package org.circuit.port;

import java.util.Map;
import java.util.Random;

public class PortNot extends Port {

	private static final long serialVersionUID = 1L;
	
	private int index;
	
	public PortNot(int index) {
		this.index = index;
	}

	@Override
	public void reset() {}
	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortNot) {
			PortNot portNot = (PortNot) obj;
			equals = this.index == portNot.getIndex(); 
		}
		
		return equals;
	}

	@Override
	public void adustLeft(int index) {
		if (this.index > index) {
			this.index--;
		}
	}
	
	@Override
	public boolean references(int index) {
		return index == this.index;
	}

	public int getIndex() {
		return index;
	}
	

	public boolean evaluate(boolean list[]) {
		return !list[this.index];
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortNot) {
			PortNot portNot = (PortNot) port;
			answer = this.index - portNot.getIndex();
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}

	public static PortNot random(int size) {
		Random random = new Random();
		return new PortNot(random.nextInt(size));
	}

	public String toString() {
		return "NOT[" + this.index + "]";
	}

	@Override
	public boolean checkConsistency(int index) {
		return (this.index < index);
	}

	@Override
	public Object clone() {
		return new PortNot(this.index);
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


