package org.circuit.circuit;

import org.apache.commons.lang3.tuple.Pair;
import org.circuit.evaluator.Evaluator;

public class CircuitToString {
	
	private static final String BUFFER_TO_SMALL_STRING = "toSmallString";
	private static final String BUFFER_TO_STRING = "toString";
	
	public static String toString(Evaluator evaluator, Circuit circuit) {
		
		String temporary = circuit.getBuffer(BUFFER_TO_STRING, String.class);
		if (temporary != null) {
			return temporary;
		}
		
		StringBuffer sb = new StringBuffer();

		sb.append(toSmallString(evaluator, circuit));
		sb.append(" ");
		
		for (int i = 0; i < circuit.size(); i++) {
			sb.append("[").append(i).append(" ").append(circuit.get(i).toString()).append("] ");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		circuit.setBuffer(BUFFER_TO_STRING, sb.toString());
		
		return sb.toString();
	}

	public static String toSmallString(Evaluator evaluator, Circuit circuit) {
		
		
		
		String temporary = circuit.getBuffer(BUFFER_TO_SMALL_STRING, String.class);
		if (temporary != null) {
			return temporary;
		}
		
		StringBuffer sb = new StringBuffer();
		
		for (Pair<String, Boolean> pair : evaluator.getOrders()) {
			
			sb.append("[");
			sb.append(pair.getLeft());
			sb.append("=");
			sb.append(circuit.getBuffer(pair.getLeft(), Integer.class).toString());
			sb.append("] ");
			
		}
		
		sb.deleteCharAt(sb.length() - 1);
		
		circuit.setBuffer(BUFFER_TO_SMALL_STRING, sb.toString());
		
		return sb.toString();
	}

}
