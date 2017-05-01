package org.circuit.random;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class RandomWeight<E> {
	
	private Map<E, List<Long>> tableTimeControl =  new HashMap<E, List<Long>>();
	
	private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
	private double total = 0;

	public void addByWeight(double weight, E result) {
		if (weight <= 0)
			return;
		total += weight;
		map.put(total, result);
	}

	public void addByPeriod(long period, E result) {
		this.tableTimeControl.put(result, Arrays.asList(new Long[] {Long.valueOf(period), null}));
	}

	public E next() {
		for (Map.Entry<E, List<Long>> entry : tableTimeControl.entrySet()) {
			long now = System.currentTimeMillis();
			Long last = entry.getValue().get(1);
			if ((last == null) || (now - last.longValue() > entry.getValue().get(0).longValue())) {
				entry.getValue().set(1, Long.valueOf(System.currentTimeMillis()));
				return entry.getKey();
			}
		}
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double value = random.nextDouble() * total;
		return map.ceilingEntry(value).getValue();
	}
}
