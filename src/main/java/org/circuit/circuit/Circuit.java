package org.circuit.circuit;

import java.util.ArrayList;
import java.util.TreeMap;

import org.circuit.port.Port;
import org.circuit.port.PortInput;

public class Circuit extends ArrayList<Port> implements Cloneable {

	private static final long serialVersionUID = 1L;

	private transient boolean checkConsistency = true;
	
	private transient TreeMap<String, Object> buffer = new TreeMap<String, Object>();
	
	private Circuit() {
	}

	public Circuit(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new PortInput(i));
		}
	}
	
	public void setBuffer(String name, Object object) {
		if (this.buffer == null) {
			this.buffer = new TreeMap<String, Object>();
		}
		this.buffer.put(name, object);
	}

	public <T> T getBuffer(String name, Class<T> clazz) {
		if (this.buffer == null) {
			this.buffer = new TreeMap<String, Object>();
		}
		return clazz.cast(this.buffer.get(name));
	}

	public boolean[] generateInitialState() {
		return new boolean[size()];
	}

	public void assignInputToState(boolean state[], boolean input[]) {
		for (int i = 0; i < input.length; i++) {
			state[i] = input[i];
		}
	}

	public void propagate(boolean state[]) {
		for (int i = 0; i < this.size(); i++) {
			state[i] = this.get(i).evaluate(state);
		}
	}

	public void reset() {
		for (Port port : this) {
			port.reset();
		}
	}

	public void removePort(int index) {
		
		if (checkConsistency) {
			//logger.info(String.format("Checking [%d] size [%d]", index, size()));
			for (int i = size()-1; i >= index + 1; i--) {
				//logger.info(String.format("Checking [%d] %s", index, get(i).toString()));
				if (get(i).references(index)) {
					//logger.info(String.format("Recurring on [%d]", i));
					throw new RuntimeException("Inconsistency");
				}
			}
		}
		
		for (int i = index + 1; i < size(); i++) {
			this.get(i).adustLeft(index);
		}
		//logger.info("Removing port: " + this.get(index).toString());
		this.remove(index);
	}

	@Override
	public Object clone() {
		Circuit circuit = new Circuit();
		for (Port port : this) {
			circuit.add((Port) port.clone());
		}
		
		return circuit;
	}
	
}
