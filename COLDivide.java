package run;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;
import util.S;


public class COLDivide extends ParserCOL
{

    private String colName;



    
    @Override
    public void _setArgs(String[] args) {
        for (int i=0; i<args.length; i++) {
             if (args[i].equals("+n")) {
                i++;
                colName = args[i];
            } else {
                addColIndexOrName(args[i].trim());
            }
        }
        
    }

    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
        //setImpactColIndex(headerLine);
        if (colName == null) {
            for (int col : impactCols) {
                if (colName != null)
                    colName += "/";
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
                Double quotient = null;
             Double dividend = S.trimValue( contentLine.cols.get(impactCols.get(0)));
            
             if (dividend == null)
            	 dividend=0.0;
            for (int i = 1; i < impactCols.size(); i++) {
            	Double denominator= S.trimValue(contentLine.cols.get(impactCols.get(i)));
            	if (denominator != null )
            		quotient = dividend / denominator;
            	
            }
            if (quotient !=null)
            	contentLine.cols.add(""+quotient);
            else
            	contentLine.cols.add("");
            	
            
            
        } catch (NumberFormatException e) {
            contentLine.cols.add("");
        }
        super.processContent(contentLine);
    }

}

