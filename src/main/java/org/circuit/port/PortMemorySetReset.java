package org.circuit.port;

import java.util.Map;
import java.util.Random;

public class PortMemorySetReset extends Port {

	private static final long serialVersionUID = 1L;
	
	private transient boolean memory = false;

	private int type;
	
	private int minor;
	private int major;

	public PortMemorySetReset() { }
	
	public PortMemorySetReset(int left, int right, int type) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
		this.type = type;
	}
	
	@Override
	public void reset() {
		this.memory = false;
	};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortMemorySetReset) {
			PortMemorySetReset portMemorySetReset = (PortMemorySetReset) obj;
			equals = (this.minor == portMemorySetReset.getMinor()) && (this.major == portMemorySetReset.getMajor()) && (this.type == portMemorySetReset.getType()); 
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
	
	public int getType() {
		return type;
	}
	
	public boolean evaluate(boolean list[]) {
		int set , reset;
		
		if (type == 0) {
			set = this.minor;
			reset = this.major;
		}
		else {
			set = this.major;
			reset = this.minor;
		}
		
		if (list[set] && list[reset]) {
			this.memory = !this.memory;			
		}
		else if (list[reset]) { // Reset
			this.memory = false;
		} else if (list[set]) { // Set
			this.memory = true;
		}
		return this.memory;
	}

	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortMemorySetReset) {
			PortMemorySetReset portMemorySetReset = (PortMemorySetReset) port;
			answer = this.minor - portMemorySetReset.getMinor();
			if (answer == 0) {
				answer = this.major - portMemorySetReset.getMajor();
				if (answer == 0) {
					answer = this.type - portMemorySetReset.getType();
				}
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

	public static PortMemorySetReset random(int size) {
		Random random = new Random();
		int l = random.nextInt(size);
		int r = 0;
		while ((r = random.nextInt(size)) == l);
		return new PortMemorySetReset(l, r, random.nextInt(2));
	}

	public String toString() {
		return String.format("MT%d[%d,%d]", this.type, this.minor, this.major);
	}

	@Override
	public Object clone() {
		return new PortMemorySetReset(this.minor, this.major, this.type);
	}
    
	@Override
	public void adjust(int oldIndex, int newIndex) {
		if (this.minor == oldIndex) {
			if (newIndex > this.major) {
				this.type = (this.type + 1) % 2;
			}
			this.minor = Math.min(newIndex, this.major);
			this.major = Math.max(newIndex, this.major);
		}
		
		if (this.major == oldIndex) {
			if (newIndex < this.minor) {
				this.type = (this.type + 1) % 2;
			}
			this.major = Math.max(newIndex, this.minor);
			this.minor = Math.min(newIndex, this.minor);
		}
	}

	@Override
	public void translate(Map<Integer, Integer> map) {
		
		int newMajor = map.get(this.major).intValue();
		int newMinor = map.get(this.minor).intValue();
		
		if (newMajor < newMinor) {
			this.type = (this.type + 1) % 2;
		}
		
		this.minor = Math.min(newMinor, newMajor);
		this.major = Math.max(newMinor, newMajor);
	}

	
	
}
