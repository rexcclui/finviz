package run;

import java.io.File;
import java.util.ArrayList;

import data.Line;
import util.AppException;
import util.Arg;
import util.ConfigException;
import util.FileTool;
import util.ParserCOL;
import util.PrintedAppException;
import util.S;

public class COLFixWth extends ParserCOL
{


    private String exportFile;

	private boolean preloadMode;

	@Override
    public void _setArgs(String[] args) throws ConfigException {
        trimHeader =Arg.exists(args, "-trimHeader");
        exportFile =Arg.getArgValue(args, "-export");
        String lenStr = null;
        if (exportFile!= null && new File(exportFile).exists()) {
        	try {
				 lenStr = FileTool.loadFile(exportFile);
				for (String len: lenStr.split(",")) 
					colLen.add(Integer.parseInt(len.trim()));
				preloadMode = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new ConfigException(e.toString() + " lenStr "+ lenStr + " " +lenStr.length());
			}
        }
        
    }

    private ArrayList<Integer> colLen = new ArrayList<Integer>(10);

    private ArrayList<Line> fixWidthLines = new ArrayList<Line>(20);

    private boolean headerOn;

    private boolean trimHeader;;

    @Override
    public void processContent(Line contentLine) throws AppException {
    	if (!preloadMode) {
	        for (int i = 0; i < contentLine.cols.size(); i++) {
	            if (colLen.size()<= i)
	                colLen.add(contentLine.cols.get(i).length());
	            else if (colLen.get(i) < contentLine.cols.get(i).length())
	                colLen.set(i, contentLine.cols.get(i).length() );
	
	        }
	        fixWidthLines.add(contentLine);
    	} else {//preload mode, column len is inputted
    		for (int i = 0; i < contentLine.cols.size(); i++) {
				String val = contentLine.cols.get(i);
				// if (lineNo == 0) System.out.println(i+">"+ colLen.get(i));

				val = S.padLeftSpace(val, colLen.get(i));
				contentLine.cols.set(i, val);
			}
			super.processContent(contentLine);
    	}
    }

    @Override
    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
    	if (!preloadMode) {
	        if (!trimHeader )
				try {
					processContent(headerLine);
				} catch (AppException e) {
					// TODO Auto-generated catch block
					throw new ConfigException ("headerLine " + headerLine,e);
				}
			else
	            fixWidthLines.add(headerLine);
	        headerOn = true;
		} else {
			for (int i = 0; i < headerLine.cols.size(); i++) {
				String val = headerLine.cols.get(i);
				// if (lineNo == 0) System.out.println(i+">"+ colLen.get(i));

				val = S.padLeftSpace(val, colLen.get(i));
				headerLine.cols.set(i, val);
			}
			super.processHeader(headerLine);
		}
	}



    @Override
    public void flush() throws    AppException   {
    	if (!preloadMode) {
	    	if (exportFile!=null) {
	    		String lenStr="";
	    		for (int len:colLen) {
	    			if (!lenStr.isEmpty())
	    				lenStr +=",";
	    			lenStr +=len;
	    		}
	    		FileTool.writeFile(exportFile, lenStr);
	    	}
	        for (int lineNo = 0 ; lineNo <fixWidthLines.size() ; lineNo++) {
	            Line line = fixWidthLines.get(lineNo);
	            for (int i = 0; i < line.cols.size(); i++) {
	                String val =line.cols.get(i);
	                //if (lineNo == 0) System.out.println(i+">"+ colLen.get(i));
	                if (lineNo == 0 &&trimHeader && val.length()> colLen.get(i))
	                    val = val.substring(0, colLen.get(i));
	                else
	                    val = S.padLeftSpace(val, colLen.get(i));
	                line.cols.set(i, val);
	            }
	     /*       for (int i= line.cols.size(); i < colLen.size(); i++) {
	                line.add(S.padLeftSpace("", colLen.get(i)));
	            }
	     */     //will add extra space for coloring case, comment first 
	            if (lineNo == 0 && headerOn)
	                super.processHeader(line);
	            else
	                super.processContent(line);// resume
	        }
    	}
        super.flushHead();
        super.flush();
    }

}

