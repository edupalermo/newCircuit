package org.circuit.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.circuit.method.Method;
import org.circuit.stat.StatData.Field;

public class StatDataDumper {
	
	public static enum Align {
		LEFT,
		RIGHT,
		CENTER
	};
	
	// private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.0");
	private static final DecimalFormat DEFAULT_LONG_FORMAT = new DecimalFormat("#,###");

	private static final String TITLE_METHOD = "Method";
	private static final String TITLE_MIN = "Min";
	private static final String TITLE_MID = "Mid";
	private static final String TITLE_MAX = "Max";
	private static final String TITLE_COUNT = "Count";
	
	private static final String methodTitle[] = new String[] {"Generation", "Evaluation", "SortedAddition"};
	

	
	public static void dump(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input) {
		
		if (input == null) {
			return;
		}

		int column[] = calculateColumnsSize(input);
		
		List<String> lines = generateLines(input, column);
		
		for (String line : lines) {
			System.out.println(line);
		}
		
	}
	
	private static List<String> generateLines(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input, int column[]) {
		
		List<String> lines = new ArrayList<String>();
		

		addLine(lines, "  ", repeat(column[0], ' ')                    , " +-", repeat(column[1] + 1 + column[2]+ 1 + column[3], '-')                                                                                 , "-+-", repeat(column[4] + 1 + column[5]+ 1 + column[6], '-')                                                                                  , "-+-", repeat(column[7] + 1 + column[8]+ 1 + column[9], '-')                                                                                 , "-+ ");
		addLine(lines, "  ", repeat(column[0], ' ')                    , " | ", fill(Align.CENTER, column[1] + 1 + column[2]+ 1 + column[3], methodTitle[0])                                                          , " | ", fill(Align.CENTER, column[4] + 1 + column[5]+ 1 + column[6], methodTitle[1])                                                           , " | ", fill(Align.CENTER, column[7] + 1 + column[8] + 1 + column[9], methodTitle[2])                                                         , " | ");
		addLine(lines, "+-", repeat(column[0], '-')                    , "-+-", repeat(column[1] + 1 + column[2]+ 1 + column[3], '-')                                                                                 , "-+-", repeat(column[4] + 1 + column[5]+ 1 + column[6], '-')                                                                                  , "-+-", repeat(column[7] + 1 + column[8]+ 1 + column[9], '-')                                                                                 , "-+-", repeat(column[10], '-')                   , "-+");
		addLine(lines, "| ", fill(Align.LEFT, column[0] , TITLE_METHOD), " | ", fill(Align.RIGHT, column[1] , TITLE_MIN), " ", fill(Align.RIGHT, column[2] , TITLE_MID), " ", fill(Align.RIGHT, column[3] , TITLE_MAX), " | ", fill(Align.RIGHT, column[4] , TITLE_MIN), " ", fill(Align.RIGHT, column[5] , TITLE_MID), " ", fill(Align.RIGHT, column[6] , TITLE_MAX) , " | ", fill(Align.RIGHT, column[7] , TITLE_MIN), " ", fill(Align.RIGHT, column[8] , TITLE_MID), " ", fill(Align.RIGHT, column[9] , TITLE_MAX), " | ", fill(Align.RIGHT, column[10], TITLE_COUNT), " |");
		addLine(lines, "+-", repeat(column[0], '-')                    , "-+-", repeat(column[1] + 1 + column[2]+ 1 + column[3], '-')                                                                                 , "-+-", repeat(column[4] + 1 + column[5]+ 1 + column[6], '-')                                                                                  , "-+-", repeat(column[7] + 1 + column[8]+ 1 + column[9], '-')                                                                                 , "-+-", repeat(column[10], '-')                   , "-+");

		
		for (Map.Entry<Method, Map<Field, Map<StatData.InternalField, Long>>> entry : input.entrySet()) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("| ");
			sb.append(fill(Align.LEFT, column[0] , entry.getKey().name()));
			sb.append(" | ");
			sb.append(fill(Align.LEFT, column[0] , entry.getValue().get(StatData.Field.)));
			
			
		}
		
		addLine(lines, "+-", repeat(column[0], '-')                    , "-+-", repeat(column[1] + 1 + column[2]+ 1 + column[3], '-')                                                                                 , "-+-", repeat(column[4] + 1 + column[5]+ 1 + column[6], '-')                                                                                  , "-+-", repeat(column[7] + 1 + column[8]+ 1 + column[9], '-')                                                                                 , "-+-", repeat(column[10], '-')                   , "-+");
		
		
		
		return lines;
	}
	
	private static void addLine(List<String> lines, String... fields) {
		
		StringBuilder sb = new StringBuilder();
		for (String s : fields) {
			sb.append(s);
		}
		
		lines.add(sb.toString());
	}
	
	
	private static int[] calculateColumnsSize(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input) {
		int column[] = new int[11];
		
		column[0] = getLargestMethodName(input, TITLE_METHOD);
		
		column[1] = getLargestFieldValue(input, StatData.Field.GENERATION, StatData.InternalField.MIN, TITLE_MIN, DEFAULT_LONG_FORMAT);
		column[2] = getLargestFieldMidleValue(input, StatData.Field.GENERATION, TITLE_MID, DEFAULT_LONG_FORMAT);
		column[3] = getLargestFieldValue(input, StatData.Field.GENERATION, StatData.InternalField.MAX, TITLE_MAX, DEFAULT_LONG_FORMAT);
		
		column[4] = getLargestFieldValue(input, StatData.Field.EVALUATION, StatData.InternalField.MIN, TITLE_MIN, DEFAULT_LONG_FORMAT);
		column[5] = getLargestFieldMidleValue(input, StatData.Field.EVALUATION, TITLE_MID, DEFAULT_LONG_FORMAT);
		column[6] = getLargestFieldValue(input, StatData.Field.EVALUATION, StatData.InternalField.MAX, TITLE_MAX, DEFAULT_LONG_FORMAT);
		
		column[7] = getLargestFieldValue(input, StatData.Field.ADD_TO_POPULATION, StatData.InternalField.MIN, TITLE_MIN, DEFAULT_LONG_FORMAT);
		column[8] = getLargestFieldMidleValue(input, StatData.Field.ADD_TO_POPULATION, TITLE_MID, DEFAULT_LONG_FORMAT);
		column[9] = getLargestFieldValue(input, StatData.Field.ADD_TO_POPULATION, StatData.InternalField.MAX, TITLE_MAX, DEFAULT_LONG_FORMAT);
		
		column[10] = getLargestFieldValue(input, StatData.Field.ADD_TO_POPULATION, StatData.InternalField.COUNT, TITLE_COUNT, DEFAULT_LONG_FORMAT);
		
		adjust(column, 1, methodTitle[0].length());
		adjust(column, 4, methodTitle[1].length());
		adjust(column, 7, methodTitle[2].length());

		return column;
	}
	
	private static void adjust(int column[], int index, int titleSize) {
		
		int diff = Math.abs(titleSize - (column[index] + column[index + 1] + column[index + 2] + 2));
		
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				column[index + (i % 3)]++;
			}
		} 
	}
	
	private static int getLargestMethodName(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input, String title) {
		int width = title.length();
		
		for (Map.Entry<Method, Map<Field, Map<StatData.InternalField, Long>>> entry : input.entrySet()) {
			width = Math.max(width, entry.getKey().name().length());
		}
		
		return width;
	}

	private static int getLargestFieldValue(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input, StatData.Field field, StatData.InternalField internalField, String title, DecimalFormat decimalFormat) {
		int width = title.length();
		
		for (Map.Entry<Method, Map<Field, Map<StatData.InternalField, Long>>> entry : input.entrySet()) {
			width = Math.max(width, decimalFormat.format(entry.getValue().get(field).get(internalField).longValue()).length());
		}
		
		return width;
	}

	private static int getLargestFieldMidleValue(Map<Method, Map<Field, Map<StatData.InternalField, Long>>> input, StatData.Field field, String title, DecimalFormat decimalFormat) {
		int width = title.length();
		
		for (Map.Entry<Method, Map<Field, Map<StatData.InternalField, Long>>> entry : input.entrySet()) {
			double mid = entry.getValue().get(field).get(StatData.InternalField.TOTAL).doubleValue() / entry.getValue().get(field).get(StatData.InternalField.COUNT).doubleValue();
			width = Math.max(width, decimalFormat.format(mid).length());
		}
		
		return width;
	}

	private static String fill(Align align, int size, String input, char filler) {
		StringBuffer sb = new StringBuffer();
		
		if (input.length() > size) {
			throw new RuntimeException("Trying to fit content in a small space");
		}
		
		int diff = size - input.length();
		
		if (align == Align.LEFT) {
			sb.append(input).append(repeat(diff, filler));
		}
		else if (align == Align.RIGHT) {
			sb.append(repeat(diff, filler)).append(input);
		}
		else if (align == Align.CENTER) {
			int inc = diff % 2;
			int half = diff / 2;
			sb.append(repeat(half, filler)).append(input).append(repeat(half + inc, filler));
		}
		else {
			throw new RuntimeException(String.format("Unknow align %s", align.name()));
		}
		
		return sb.toString();
	}
	
	private static String fill(Align align, int size, String input) {
		return fill(align, size, input, ' ');
	}
	
	private static String repeat(int size, char filler) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < size; i++) {
			sb.append(filler);
		}
		
		return sb.toString();
	}

	

}
