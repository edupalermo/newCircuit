package org.circuit.circuit;

import org.circuit.pool.StatePool;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;
import org.circuit.solution.TrainingSet;

public class CircuitOutputGenerator {

	// private static final Logger logger = Logger.getLogger(CircuitOutputGenerator.class);

	public static int[] generateOutput(TrainingSet trainingSet, Circuit circuit) {

		final int outputSize = trainingSet.getOutputSize();

		int score[][] = new int[circuit.size()][outputSize];

		for (Solution solution : trainingSet) {
			evaluate(circuit, solution, score);
		}

		return generateOutput(score, outputSize);
	}

	private static int[] generateOutput(int score[][], int outputSize) {
		int[] output = new int[outputSize];
		for (int i = 0; i < score[0].length; i++) {
			int better = 0;
			for (int j = 1; j < score.length; j++) {
				if (score[j][i] > score[better][i]) {
					better = j;
				}
			}
			output[i] = better;

			// logger.info(String.format("%d %d %d", i, better,
			// score[better][i]));
		}

		return output;
	}

	private static void evaluate(Circuit circuit, Solution solution, int[][] score) {
		boolean state[] = null;

		try {
			state = StatePool.borrow(circuit.size());
			circuit.reset();

			for (TimeSlice timeSlice : solution) {
				circuit.assignInputToState(state, timeSlice.getInput());
				circuit.propagate(state);

				for (int i = 0; i < score.length; i++) {
					for (int j = 0; j < timeSlice.getOutput().length; j++) {
						if (state[i] == timeSlice.getOutput()[j]) {
							score[i][j]++;
						}
					}
				}
			}
		} finally {
			StatePool.retrieve(state);
		}
	}

}
