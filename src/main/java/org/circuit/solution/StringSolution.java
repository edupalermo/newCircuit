package org.circuit.solution;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.circuit.circuit.Circuit;
import org.circuit.utils.IoUtils;

public class StringSolution extends Solution {

	private static final long serialVersionUID = 1L;

	private final static byte BYTE_ZERO = 0x00;

	private final static int BIT_ANSWER = 8;
	
	private final static int LIMIT_TRASH_TALK = 50;

	public StringSolution(String input, String output) {
		populateTimeSliceList(input, output);
	}

	private void populateTimeSliceList(String input, String output) {

		Clock clock = new Clock();

		try {
			for (byte b : input.getBytes("UTF-8")) {
				boolean inputArray[] = new boolean[12];
				populateByteIntoArray(b, inputArray, 0);
				inputArray[8] = clock.thick();
				inputArray[9] = true; // Always true
				inputArray[10] = false; // Always false
				inputArray[11] = true; // Input is Speaking

				boolean outputArray[] = new boolean[9];
				populateByteIntoArray(BYTE_ZERO, outputArray, 0);
				outputArray[8] = false; // Output is hearing

				this.add(new TimeSlice(inputArray, outputArray));
			}

			for (byte b : output.getBytes("UTF-8")) {
				boolean inputArray[] = new boolean[12];
				populateByteIntoArray(BYTE_ZERO, inputArray, 0);
				inputArray[8] = clock.thick();
				inputArray[9] = true; // Always true
				inputArray[10] = false; // Always false
				inputArray[11] = false; // Input is Hearing

				
				boolean outputArray[] = new boolean[9];
				populateByteIntoArray(b, outputArray, 0);
				outputArray[8] = true; // Output is speaking

				this.add(new TimeSlice(inputArray, outputArray));
			}

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		// Final comunication
		
		
		boolean inputArray[] = new boolean[12];
		populateByteIntoArray(BYTE_ZERO, inputArray, 0);
		inputArray[8] = clock.thick();
		inputArray[9] = true; // Always true
		inputArray[10] = false; // Always false
		inputArray[11] = false; // Input is Hearing

		boolean outputArray[] = new boolean[9];
		populateByteIntoArray(BYTE_ZERO, outputArray, 0);
		outputArray[8] = false; // Output is hearing

		this.add(new TimeSlice(inputArray, outputArray));

	}

	private static void populateByteIntoArray(byte b, boolean array[], int offset) {
		for (int i = 0; i < 8; i++) {
			array[i + offset] = getBit(b & 0xFF, i);
		}
	}

	private static byte listToByte(boolean state[], int output[], int offset, int size) {
		int answer = 0;

		for (int i = 0; i < size; i++) {
			if (state[output[i + offset]]) {
				answer = answer + (int) Math.pow(2, size - 1 - i);
			}
		}
		return (byte) answer;
	}

	private static boolean getBit(int b, int i) {
		return (0x01 & (b >> (7 - i))) == 1;
	}

	public static String evaluate(Circuit circuit, int[] output, String input) {
		Clock clock = new Clock();

		boolean state[] = circuit.generateInitialState();
		circuit.reset();

		try {
			for (byte b : input.getBytes("UTF-8")) {
				boolean inputArray[] = new boolean[12];
				populateByteIntoArray(b, inputArray, 0);
				inputArray[8] = clock.thick();
				inputArray[9] = true; // Always true
				inputArray[10] = false; // Always false
				inputArray[11] = true; // Input is Speaking

				circuit.assignInputToState(state, inputArray);
				circuit.propagate(state);

				if (state[output[BIT_ANSWER]]) {
					throw new RuntimeException("Inconsistency");
				}
			}
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}

		String answer = null;
		ByteArrayOutputStream baos = null;
		int count = 0;

		try {
			baos = new ByteArrayOutputStream();
			do {
				boolean inputArray[] = new boolean[12];
				populateByteIntoArray(BYTE_ZERO, inputArray, 0);
				inputArray[8] = clock.thick();
				inputArray[9] = true; // Always true
				inputArray[10] = false; // Always false
				inputArray[11] = false; // Input is hearing

				circuit.assignInputToState(state, inputArray);
				circuit.propagate(state);

				count++;

				if (count > LIMIT_TRASH_TALK) {
					state[output[BIT_ANSWER]] = false;
					// throw new RuntimeException("Inconsistency");
				}

				if (state[output[BIT_ANSWER]]) {
					baos.write(listToByte(state, output, 0, 8));
				}
			} while (state[output[BIT_ANSWER]]);

			answer = new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} finally {
			IoUtils.closeQuitely(baos);
		}

		return new String(answer);
	}

}
