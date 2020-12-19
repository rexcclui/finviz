package run;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.AppException;
import util.ConfigException;
import util.FIXStack;
import util.ParserCOL;
import util.PrintedAppException;
import data.Line;

/**
 * To display multi page horizontally, should be the last parser
 * @author rexcc
 *
 */
public class COLMultiPage extends ParserCOL
{


    

    private int noOfPage;


    private ArrayList<Line> lineStack ;

	@Override
    public void _setArgs(String[] args) {
      
    	 noOfPage = Integer.parseInt(args[0]);
        // System.out.println(colRange);
    	 
    	 lineStack = new ArrayList<Line>(noOfPage *2);

    }


    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
    	Line newHdrLine = new Line();

    	for (int i=0; i< noOfPage; i++) newHdrLine.cols.addAll(headerLine.cols);
        super.processHeader(newHdrLine);
    }



    Line showLine = new Line();


	private int lineBufCnt;

    @Override
    public void processContent(Line contentLine) throws AppException {

    	 showLine.cols.addAll(contentLine.cols);
    	 lineBufCnt ++;
    	 if(lineBufCnt == noOfPage) {
        	super.processContent(showLine);
        	
        	lineBufCnt = 0;
        	showLine = new Line();
        	
    	 }
        
    }
    

    @Override
    public void flush() throws AppException {

    	if(lineBufCnt >0) 
        	super.processContent(showLine);
        super.flushHead();
        super.flush();
    }

}


