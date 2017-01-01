package org.circuit.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.circuit.port.Port;
import org.circuit.port.PortAnd;

public class PortPool {
	
	private static final Object semaphore = new Object();

	private static TreeMap<String, List<Port>> cache = new TreeMap<String, List<Port>>(); 
	
	
	public static <T> T borrow(Class<T> clazz) {
		T result = null;
		
		List<Port> l = cache.get(clazz.getName());
		
		if (l != null && l.size() > 0) {
			result = clazz.cast(l.remove(0));
		}
		else {
			try {
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return result;
	}
	
	public static void retrieve(Port port) {
		List<Port> l = cache.get(port.getClass().getName());
		
		if (l == null) {
			l = new ArrayList<Port>();
			cache.put(port.getClass().getName(), l);
		}
		l.add(port);
	}

	public static void main(String[] args) {
		
		PortAnd p1 = borrow(PortAnd.class);
		System.out.println(cache.size());
		PortAnd p2 = borrow(PortAnd.class);
		System.out.println(cache.size());
		retrieve(p1);
		System.out.println(cache.size());
		retrieve(p2);
		System.out.println(cache.size());
		
		
		
		
	}
	

}
