package run;

import java.util.HashMap;

import data.Line;
import util.AnsiC;
import util.AppException;
import util.ConfigException;
import util.O;
import util.ParserCOL;
import util.PrintedAppException;
import util.Range;

/**
 * 
 * change color for  particular columns base on range in %
 *
 * @author rex_lui
 */
public class COLColorPerC extends ParserCOL
{

    HashMap<Integer, String> lastColValues = new HashMap<Integer, String> (10);

    Range cols;// null, for all rows

//    private String color=AnsiC.BOLD_BLUE_BLACK;

    private Double min;

    private Double max;

    private double range;

    @Override
    public void _setArgs(String[] args) {
    	for (int i=0 ; i< args.length ; i++) {
    		
    		if (args[i].equals("-min")) {
    			min = Double.parseDouble(args[++i]);
    		} else if (args[i].equals("-max")) {
    			max = Double.parseDouble(args[++i]);
    		} else
    			addColIndexOrName(args[i]);
    		
    	}
         max= (Double) O.def(max,3d);         
         min = (Double) O.def(min,-1d);
         range = max - min;
    }

    @Override
    public void processHeader(Line line) throws ConfigException, PrintedAppException {
        for (int col : impactCols) {
                line.cols.set(col, AnsiC.BOLD_EXTEND_BLUE + line.cols.get(col) + AnsiC.RESET_WHITE_BLACK);
        }
        

        super.processHeader(line);
    }

    @Override
    public void processContent(Line line) throws AppException {

        for (int col : impactCols) {
            Double currentValue = ParserCOL.trimValue(line.cols.get(col));

            if (currentValue == null)
                line.cols.set(col, AnsiC.RESET_WHITE_BLACK + line.cols.get(col) + AnsiC.RESET_WHITE_BLACK);
            else
                line.cols.set(col, AnsiC.color((currentValue-min)/range) + line.cols.get(col) + AnsiC.RESET_WHITE_BLACK);

        }

        super.processContent(line);
    }


}



