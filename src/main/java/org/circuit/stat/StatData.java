package org.circuit.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.circuit.method.Method;

public class StatData {

	public enum Field {
		GENERATION,
		EVALUATION,
		ADD_TO_POPULATION
	};
	
	public enum InternalField {
		MIN,
		MAX,
		TOTAL,
		COUNT
	};
	
	Map<Method, Map<Field, Map<InternalField, Long>>> session = null;
	Map<Method, Map<Field, Map<InternalField, Long>>> allTime = null;
	
	public void endSession() {
		
		if (session != null) {
			
			for (Map.Entry<Method, Map<Field, Map<InternalField, Long>>> entryMethod : session.entrySet()) {
				
				for (Map.Entry<Field, Map<InternalField, Long>> entryField : entryMethod.getValue().entrySet()) {
					
					if (allTime == null) {
						allTime = new HashMap<Method, Map<Field, Map<InternalField, Long>>>();
					}
					
					if (allTime.get(entryMethod.getKey()) == null) {
						allTime.put(entryMethod.getKey(), new HashMap<Field, Map<InternalField, Long>>());
					}
					
					if (allTime.get(entryMethod.getKey()).get(entryField.getKey()) == null) {
						allTime.get(entryMethod.getKey()).put(entryField.getKey(), new HashMap<InternalField, Long>());
						
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.MIN, Long.valueOf(Long.MAX_VALUE));
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.MAX, Long.valueOf(0));;
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.TOTAL, Long.valueOf(0));;
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.COUNT, Long.valueOf(0));;
					}
					
					if (entryField.getValue().get(InternalField.MIN).longValue() < allTime.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.MIN).longValue()) {
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.MIN, Long.valueOf(entryField.getValue().get(InternalField.MIN).longValue()));
					}
					
					if (entryField.getValue().get(InternalField.MAX).longValue() > allTime.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.MAX).longValue()) {
						allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.MAX, Long.valueOf(entryField.getValue().get(InternalField.MAX).longValue()));
					}
					
					long newTotal = allTime.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.TOTAL).longValue() + session.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.TOTAL).longValue();
					allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.TOTAL, Long.valueOf(newTotal));
					
					long newCount = allTime.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.COUNT).longValue() + session.get(entryMethod.getKey()).get(entryField.getKey()).get(InternalField.COUNT).longValue();
					allTime.get(entryMethod.getKey()).get(entryField.getKey()).put(InternalField.COUNT, Long.valueOf(newCount));
					
				}
				
			}
		}
			
	}
	
	public void setData(Method method, Field field, long value) {
		if (session == null) {
			session = new HashMap<Method, Map<Field, Map<InternalField, Long>>>(); 
		}
		
		if (session.get(method) == null) {
			session.put(method, new HashMap<Field, Map<InternalField, Long>>());
		}
		
		if (session.get(method).get(field) == null) {
			session.get(method).put(field, new HashMap<InternalField, Long>());
			
			session.get(method).get(field).put(InternalField.MIN, Long.valueOf(Long.MAX_VALUE));
			session.get(method).get(field).put(InternalField.MAX, Long.valueOf(0));;
			session.get(method).get(field).put(InternalField.TOTAL, Long.valueOf(0));;
			session.get(method).get(field).put(InternalField.COUNT, Long.valueOf(0));;
		}
		
		if (value < session.get(method).get(field).get(InternalField.MIN).longValue()) {
			session.get(method).get(field).put(InternalField.MIN, Long.valueOf(value));
		}
		
		if (value > session.get(method).get(field).get(InternalField.MAX).longValue()) {
			session.get(method).get(field).put(InternalField.MAX, Long.valueOf(value));
		}
		
		session.get(method).get(field).put(InternalField.TOTAL, Long.valueOf(session.get(method).get(field).get(InternalField.TOTAL).longValue() + value));
		
		session.get(method).get(field).put(InternalField.COUNT, Long.valueOf(session.get(method).get(field).get(InternalField.COUNT).longValue() + 1));
		
		
	}
	
	public void dump() {
		StatDataDumper.dump(session);
		StatDataDumper.dump(allTime);
	} 

}
