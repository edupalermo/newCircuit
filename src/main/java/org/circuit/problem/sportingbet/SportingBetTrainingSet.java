package org.circuit.problem.sportingbet;

import org.circuit.solution.TrainingSet;

public class SportingBetTrainingSet extends TrainingSet {
	
	
	public static enum Team {
		BOTAFOGO(0),
		FLAMENGO(1);
		
		private int id;
		
		private Team(int id) {
			this.id = id;
		}
	}
	
	public static enum Championship {
		CAMPEONATO_CARIOCA(0),
		CAMPEONATO_BRASILEIRO(1);
		
		private int id;
		
		private Championship(int id) {
			this.id = id;
		}
	}
	
	
	
	private void add(long date, Team home, Team visitor) {
		
		
	} 
	
	

}
