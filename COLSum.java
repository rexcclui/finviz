package run;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;


public class COLSum extends ParserCOL
{

    private String colName;



    @Override
    public void _setArgs(String[] args) {
        for (int i=0; i<args.length; i++) {
             if (args[i].equals("+n")) {
                i++;
                colName = args[i];
            } else {
                addColIndexOrName(args[i]);
            }
        }
        
    }

    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
       // setImpactColIndex(headerLine);
        if (colName == null) {
            for (int col : impactCols) {
                if (colName != null)
                    colName += "+";
                else
                    colName = "";
                colName += headerLine.cols.get(col);
            }
        }
        headerLine.cols.add(colName);
        super.processHeader(headerLine);
    }
    @Override
    public void processContent(Line contentLine) throws AppException {
         try {
            double sumValue = 0.0;
            
            for (int col: impactCols) {
                sumValue += Double.parseDouble(contentLine.cols.get(col));
            }
            contentLine.cols.add(""+sumValue);
        } catch (NumberFormatException e) {
            contentLine.cols.add("");
        }
        super.processContent(contentLine);
    }

}


