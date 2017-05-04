package org.circuit.table;

import java.util.ArrayList;
import java.util.List;

public class Row {
	
	private final List<Cell> cells;
	
	private Row(List<Cell> cells) {
		this.cells = cells;
	}

	public List<Cell> getCells() {
		return cells;
	}
	

	public static class RowBuilder {
		private List<Cell> cells = new ArrayList<Cell>();
		
		public RowBuilder appendCell(Cell cell) {
			this.cells.add(cell);
			return this;
		}

		public Row build() {
			return new Row(this.cells);
		}
		
	}
	
}
