package run;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;
import util.S;


/**
 * calculate per difference between two column   e.g. +n chgPer price close
 * @author rexcc
 *
 */
public class COLColDiffPer extends ParserCOL
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
                    colName += "-";
                else
                    colName = "";
                colName += headerLine.cols.get(col);
            }
            colName += "%";
        }
        headerLine.cols.add(colName);
        super.processHeader(headerLine);
    }
    @Override
    public void processContent(Line contentLine) throws AppException {
         try {
             Double upvalue = S.trimValue( contentLine.cols.get(impactCols.get(0)));
             Double downvalue = S.trimValue( contentLine.cols.get(impactCols.get(1)));

             
            if (upvalue !=null) {
            	double value = (upvalue - downvalue)/downvalue;
            	contentLine.cols.add(""+S.toPer(value, 2));
            }
            else
            	contentLine.cols.add("");
            	
            
            
        } catch (NumberFormatException e) {
            contentLine.cols.add("");
        }
        super.processContent(contentLine);
    }

}

