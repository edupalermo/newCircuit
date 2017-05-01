package org.circuit.port;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Port implements Serializable, Comparable<Port>, Cloneable {

	private static final long serialVersionUID = 1L;

	public abstract boolean evaluate(boolean list[]);

	public abstract void reset();

	public abstract void adustLeft(int index);

	public abstract boolean references(int index);

	public abstract boolean checkConsistency(int index);

	public abstract int compareTo(Port port);
	
	public abstract void adjust(int oldIndex, int newIndex);
	
	public abstract void translate(Map<Integer, Integer> map);
	
	@Override
	public abstract Object clone();

	public static Port random(int size, boolean useMemory) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		switch (random.nextInt(useMemory ? 6 : 5)) {
		case 0:
			return PortAnd.random(size);
		case 1:
			return PortOr.random(size);
		case 2:
			return PortNand.random(size);
		case 3:
			return PortNor.random(size);
		case 4:
			return PortNot.random(size);
		case 5:
			return PortMemorySetReset.random(size);
		default:
			throw new RuntimeException("Inconsistency");
		}
	}

}
