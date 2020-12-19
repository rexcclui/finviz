package run;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;

/**
 * 
 * add one column base on the filter
 * 
 * @author rex_lui
 */
public class COLRemove extends ParserCOL
{




    @Override
    public void _setArgs(String[] args) {
             addColIndexOrName(args[0].trim());
    }

    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
        if (impactCols.size()>0)
            headerLine.cols.remove((int)impactCols.get(0));
        else
            printErr("processHeader Invalid Remove Column " + this , headerLine.toCSVString());
        super.processHeader(headerLine);
    }

    @Override
    public void processContent(Line contentLine) throws AppException {
        if (impactCols.size()>0)
            contentLine.cols.remove((int)impactCols.get(0));
        else
            printErrOnce("processContent Invalid Remove Column ", this.headerLine, contentLine.toCSVString());
        super.processContent(contentLine);
    }

}
