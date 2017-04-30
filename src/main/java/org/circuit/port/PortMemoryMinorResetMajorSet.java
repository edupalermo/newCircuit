package org.circuit.port;

import java.util.Map;
import java.util.Random;

public class PortMemoryMinorResetMajorSet extends Port {

	private static final long serialVersionUID = 1L;
	
	private transient boolean memory = false;
	
	private int minor;
	private int major;

	public PortMemoryMinorResetMajorSet() { }
	
	public PortMemoryMinorResetMajorSet(int left, int right) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
	}
	
	@Override
	public void reset() {
		this.memory = false;
	};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortMemoryMinorResetMajorSet) {
			PortMemoryMinorResetMajorSet portAnd = (PortMemoryMinorResetMajorSet) obj;
			equals = (this.minor == portAnd.getMinor()) && (this.major == portAnd.getMajor()); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		if (minor > index) {
			this.minor--;
		}
		
		if (major > index) {
			this.major--;
		}
	}

	public int getMinor() {
		return minor;
	}
	

	public int getMajor() {
		return major;
	}
	
	public boolean evaluate(boolean list[]) {
		if (list[this.minor] && list[this.major]) {
			this.memory = !this.memory;			
		}
		else if (list[this.minor]) { // Reset
			this.memory = false;
		} else if (list[this.minor]) { // Set
			this.memory = true;
		}
		return this.memory;
	}

	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortMemoryMinorResetMajorSet) {
			PortMemoryMinorResetMajorSet portAnd = (PortMemoryMinorResetMajorSet) port;
			answer = this.minor - portAnd.getMinor();
			if (answer == 0) {
				answer = this.major - portAnd.getMajor();
			}
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}
	
	
	
	@Override
	public boolean references(int index) {
		return (index == this.minor) || (index == this.major);
	}
	
	

	@Override
	public boolean checkConsistency(int index) {
		return (this.minor < index) && (this.major < index);
	}

	public static PortMemoryMinorResetMajorSet random(int size) {
		Random random = new Random();
		int l = random.nextInt(size);
		int r = 0;
		while ((r = random.nextInt(size)) == l);
		return new PortMemoryMinorResetMajorSet(l, r);
	}

	public String toString() {
		return "MSR[" + this.minor + "," + this.major + "]";
	}

	@Override
	public Object clone() {
		return new PortMemoryMinorResetMajorSet(this.minor, this.major);
	}
    
	@Override
	public void adjust(int oldIndex, int newIndex) {
		if (this.minor == oldIndex) {
			this.minor = Math.min(newIndex, this.major);
			this.major = Math.max(newIndex, this.major);
		}
		
		if (this.major == oldIndex) {
			this.major = Math.max(newIndex, this.minor);
			this.minor = Math.min(newIndex, this.minor);
		}
	}

	@Override
	public void translate(Map<Integer, Integer> map) {
		
		int newMajor = map.get(this.major).intValue();
		int newMinor = map.get(this.minor).intValue();
		
		this.minor = Math.min(newMinor, newMajor);
		this.major = Math.max(newMinor, newMajor);
	}

	
	
}
