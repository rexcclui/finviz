package run;

import java.util.ArrayList;
import java.util.HashMap;

import util.AnsiC;
import util.AppException;
import util.Arg;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;
import util.Range;
import data.Line;

public class COLColorR extends ParserCOL {

	private ArrayList<Line> lines;

	HashMap<Integer, RowClrRange> rowColor = new HashMap<Integer, RowClrRange>();

	Range rows;// null, for all rows

//	private Range cols;

	@Override
	public void _setArgs(String[] args) {
		rows = Range.extractRange(Arg.getArgValue(args, "-row"));
		//cols = Range.extractRange(Arg.getArgValue(args, "-col"));
/*		if (cols.getValues().size() == 0)
			cols = null;
		else {
*/            ArrayList<String> colStrs = Arg.getArgValues(args, "-col");
            for (String colStr : colStrs) {
                try {
                    Range tmpCols = Range.extractRange(colStr);
                    for (int col : tmpCols.getValues())
                        this.addColIndexOrName("" + col);
                } catch (NumberFormatException e) {
                    this.addColIndexOrName(colStr);
                }
            }
/*		}*/
		if (rows.getValues().size() == 0)
			rows = null;
		lines = new ArrayList<Line>(100);
	}

	@Override
	public void processHeader(Line line) throws ConfigException, PrintedAppException {
		//for (int i = 0; i < line.cols.size(); i++) {

			//if (cols == null || cols.inRange(i))
	    for (int col: this.impactCols) 
				line.cols.set(col, AnsiC.BOLD_EXTEND_BLUE + line.cols.get(col) + AnsiC.RESET_WHITE_BLACK);
		//}
		super.processHeader(line);
	}

	@Override
	public void processContent(Line line) throws AppException {
		if (rows == null || rows.inRange(line.lineNo)) {
			RowClrRange clrRange = new RowClrRange();
			rowColor.put(line.lineNo, clrRange);
			for (int col: this.impactCols) 
					clrRange.addRange(getColorValue(line.cols.get(col)));
		}
		lines.add(line);
		// System.out.println(tableColorRange);
		RowClrRange clrRange = rowColor.get(line.lineNo);
		if (clrRange != null)
			for (int col: this.impactCols) 
			    line.cols.set(col, clrRange.colorize(getColorValue(line.cols.get(col)), line.cols.get(col)));
		else {//ensure same length even for not highlight
			for (int col: this.impactCols)
					line.cols.set(col, AnsiC.RESET_WHITE_BLACK + line.cols.get(col)+ AnsiC.RESET_WHITE_BLACK);
		}
		super.processContent(line);
	}

	//@Override
	public void flushBAK() throws AppException {
		// System.out.println(tableColorRange);
		for (Line line : lines) {
			RowClrRange clrRange = rowColor.get(line.lineNo);
			if (clrRange != null)
				for (int col: this.impactCols) 
				    line.cols.set(col, clrRange.colorize(getColorValue(line.cols.get(col)), line.cols.get(col)));
			else {//ensure same length even for not highlight
				for (int col: this.impactCols)
						line.cols.set(col, AnsiC.RESET_WHITE_BLACK + line.cols.get(col)+ AnsiC.RESET_WHITE_BLACK);
			}

			super.processContent(line);// resume
		}
		super.flushHead();
		super.flush();
	}

}

// color range
class RowClrRange {

	String[] colors = TFCol.defaultColors;
	private Double max = null;

	private Double min = null;

	public void addRange(Double value) {
		if (value == null)
			return;
		if (max == null || value > max)
			max = value;
		if (min == null || value < min)
			min = value;
	}

	public String colorize(Double chkValue, String printVal) {
		String color = getColor(chkValue);
		return color + printVal + AnsiC.RESET_WHITE_BLACK;
	}

	private String getColor(Double value) {
		if (value == null)
			return AnsiC.RESET_WHITE_BLACK;
		try {
			double range = max - min;

			int level = (int) ((colors.length - 1) * (value - min) / range);
			return colors[level];
		} catch (Exception e) {
			return AnsiC.IMAGE_WHITE_BLACK;
		}

	}

	@Override
	public String toString() {
		return "ClrRange [max=" + max + ", min=" + min + "]";
	}

}

