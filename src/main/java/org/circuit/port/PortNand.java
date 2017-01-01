package org.circuit.port;

import java.util.Map;
import java.util.Random;

public class PortNand extends Port {

	private static final long serialVersionUID = 1L;
	
	private int minor;
	private int major;
	
	public PortNand(int left, int right) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
	}
	
	@Override
	public void reset() {};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortNand) {
			PortNand portAnd = (PortNand) obj;
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
		return !(list[this.minor] && list[this.major]);
	}

	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortNand) {
			PortNand portAnd = (PortNand) port;
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

	public static PortNand random(int size) {
		Random random = new Random();
		int l = random.nextInt(size);
		int r = 0;
		while ((r = random.nextInt(size)) == l);
		return new PortNand(l, r);
	}

	public String toString() {
		return "AND[" + this.minor + "," + this.major + "]";
	}

	@Override
	public Object clone() {
		return new PortNand(this.minor, this.major);
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
