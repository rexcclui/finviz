package run;
import java.util.ArrayList;
import java.util.List;

import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;
import data.Line;

/**
 * align the columns
 * @author rexcc
 *
 */
public class COLAlignCol extends ParserCOL
{
    private Line origHeader;
	private String[] alignCols;

	@Override
    public void _setArgs(String[] args) {

		alignCols = args;
        // System.out.println(colRange);

    }

    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
    	Line newHdrLine = new Line();
    	newHdrLine.init(headerLine);
        origHeader = headerLine;
        for (String alignCol: alignCols) {
            	newHdrLine.cols.add(alignCol);
 /*           else
            	System.out.println(alignCol + " " + origHeader + " / " + newHdrLine);
 */       }

        super.processHeader(newHdrLine);
    }


    @Override
    public void processContent(Line contentLine) throws AppException {
        List<String> origCols = contentLine.cols;
        contentLine.cols = new ArrayList<String>(10);
        for (String alignCol: alignCols) {
            int orgPos = origHeader.cols.indexOf(alignCol);
            if (orgPos>=0)
            	contentLine.cols.add(origCols.get(orgPos));
            else
            	contentLine.cols.add("");//add empty value
        }

        super.processContent(contentLine);
    }

}


