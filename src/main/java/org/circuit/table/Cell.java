package org.circuit.table;

public class Cell {
	
	private final String content;
	private final VerticalAlign verticalAlign;
	private final HorizontalAlign horizontalAlign;
	private final int colspan;
	private final int rowspan;
	
	private Cell(String content, VerticalAlign verticalAlign, HorizontalAlign horizontalAlign, int colspan, int rowspan) {
		this.content = content;
		this.verticalAlign = verticalAlign;
		this.horizontalAlign = horizontalAlign;
		this.colspan = colspan;
		this.rowspan = rowspan;
	}
	
	public String getContent() {
		return content;
	}

	public VerticalAlign getVerticalAlign() {
		return verticalAlign;
	}

	public HorizontalAlign getHorizontalAlign() {
		return horizontalAlign;
	}

	public int getColspan() {
		return colspan;
	}

	public int getRowspan() {
		return rowspan;
	}

	public static CellBuilder getBuilder() {
		return new CellBuilder();
	}

	public static class CellBuilder {
		
		private String content;
		private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
		private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
		private int colspan = 1;
		private int rowspan = 1;

		public CellBuilder setContent(String content) {
			this.content = content;
			return this;
		}
		
		public CellBuilder setVerticalAlign(VerticalAlign verticalAlign) {
			this.verticalAlign = verticalAlign;
			return this;
		}
		
		public CellBuilder setHorizontalAlign(HorizontalAlign horizontalAlign) {
			this.horizontalAlign = horizontalAlign;
			return this;
		}
		
		public CellBuilder setColspan(int colspan) {
			this.colspan = colspan;
			return this;
		}
		
		public CellBuilder setRowspan(int rowspan) {
			this.rowspan = rowspan;
			return this;
		}
		
		public Cell build() {
			return new Cell(this.content, this.verticalAlign, this.horizontalAlign, this.rowspan, this.colspan);
			
		}

	}


}
