package run;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;

public class COLAddCol extends ParserCOL {

	private String colName;
	private String value;
	private boolean firstOnly = false;
	private boolean first = true;
	private boolean startCol;
	private boolean alreadyExists;

	@Override
	public void _setArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("+n")) {
				i++;
				colName = args[i];
			} else if (args[i].equals("-first")) {// set first row only
				firstOnly = true;
			} else if (args[i].equals("-start")) {// set first col not end
				startCol = true;
			} else
				value = args[i];
		}

	}

	@Override
	public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
		// setImpactColIndex(headerLine);
		if (colName == null) {
			throw new ConfigException("NO new colName");
		}
		if (headerLine.cols.contains(colName))
			alreadyExists = true;
		else
			if (!startCol)
				headerLine.cols.add(colName);
			else
				headerLine.cols.add(0,colName);
		super.processHeader(headerLine);
	}

	@Override
	public void processContent(Line contentLine) throws AppException {

		if (!alreadyExists) {
			
			if (firstOnly) {
				if (first) {
					if (!startCol)
						contentLine.cols.add(value);
					else
						contentLine.cols.add(0,value);
				}
				else
					if (!startCol)
						contentLine.cols.add("");
					else
						contentLine.cols.add(0,"");
			} else
				if (!startCol)
					contentLine.cols.add(value);
				else
					contentLine.cols.add(0,value);
			
			first = false; // not first row anymore
			super.processContent(contentLine);
		}
	}

}
