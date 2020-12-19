package run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import data.Line;
import util.AnsiC;
import util.AppException;
import util.ArrayTool;
import util.C;
import util.D;
import util.L;
import util.MultipleCSVIS;
import util.MultipleCSVIS1;
import util.ParserCOL;
import util.PrintedAppException;
import util.S;
import util.Stack;
import util.StringIS;

public class TFCol
{
  
    public static final String[] defaultColors =  { AnsiC.IMAGE_RED_BLACK, AnsiC.BOLD_RED_BLACK,
        AnsiC.RESET_RED_BLACK,
        AnsiC.FAINT_RED_BLACK,
        // AnsiC.FAINT_WHITE_BLACK, AnsiC.RESET_WHITE_BLACK,
        // AnsiC.BOLD_WHITE_BLACK,
        AnsiC.FAINT_YELLOW_BLACK, AnsiC.RESET_YELLOW_BLACK, AnsiC.BOLD_YELLOW_BLACK, AnsiC.FAINT_GREEN_BLACK,
        AnsiC.RESET_GREEN_BLACK, AnsiC.BOLD_GREEN_BLACK, AnsiC.IMAGE_GREEN_BLACK
    // AnsiC.FAINT_CYAN_BLACK, AnsiC.RESET_CYAN_BLACK, AnsiC.BOLD_CYAN_BLACK,
    // AnsiC.FAINT_BLUE_BLACK, AnsiC.RESET_BLUE_BLACK, AnsiC.BOLD_BLUE_BLACK,
    // AnsiC.FAINT_MAGENTA_BLACK, AnsiC.RESET_MAGENTA_BLACK,
    // AnsiC.BOLD_MAGENTA_BLACK,AnsiC.IMAGE_MAGENTA_BLACK

        };

    private ValueSet valueSet;
    protected HashMap<String, Object> sessionObj = new HashMap<String,Object> ();
    
    class ValueSet
    {

        private String parmName;

        private String parmValueString;

        private ValueSet nextValueSet;

        private ArrayList<Integer> values = new ArrayList<>(10);

        private int cursor = -1;

        // ParmX=1-100:2,105,109-120:5
        public ValueSet(String valueString) {
            parmName = valueString.split("=")[0];
            parmValueString = valueString.split("=")[1];// i.e.1-100:2,105,109-120:5

            String[] rangeValues = parmValueString.split(",");
            for (String rangeValue : rangeValues) {
                int increment = 1;
                if (rangeValue.split(":").length > 1)
                    increment = Integer.parseInt(rangeValue.split(":")[1]);// 2
                String startEndStr = rangeValue.split(":")[0];// 1-100
                int start = Integer.parseInt(startEndStr.split("-")[0]);
                int end = start;
                if (startEndStr.split("-").length > 1)
                    end = Integer.parseInt(startEndStr.split("-")[1]);
                if (start <= end)
                    for (int i = start; i <= end; i += increment) {
                        values.add(i);
                    }
                else if (start > end)
                    for (int i = start; i > end; i += increment) {
                        values.add(i);
                    }

            }
        }

        public boolean hasNext() {
            if (nextValueSet != null) {
                if (nextValueSet.hasNext()) return true;
            }
            if (cursor+1< values.size())//next will exceed
                return true;
            return false;
            
        }
        public void reset() {
            cursor=0;
            if (nextValueSet !=null) nextValueSet.reset();//self full, no next
        }
        public String[] set(String[] args) {
            String[] retArgs = new String[args.length];
            for (int i=0; i< retArgs.length; i++) {
                retArgs[i] = args[i].replace("["+parmName+"]", ""+values.get(cursor));
            }
            if (nextValueSet !=null) retArgs= nextValueSet.set(retArgs);//self full, no next
            return retArgs;
        }
        public void next() {
            if (cursor <0 )
                cursor++;
            if (nextValueSet !=null) {
                if (nextValueSet.hasNext()) {
                    nextValueSet.next();
                    return;
                }
                else {
                    nextValueSet.reset();
                    //nextValueSet.next();
                    cursor++;
                }
            } else
                cursor++;
        }

        public void add(ValueSet valueSet) {
            if (nextValueSet == null)
                nextValueSet = valueSet;
            else
                nextValueSet.add(valueSet);
        }

        @Override
        public String toString() {
            return "ValueSet [parmName=" + parmName + ", values=" + values + ", cursor=" + cursor + ", nextValueSet="
                + nextValueSet + "]";
        }

        

    }

    public TFCol(String[] args) {
        this.args = extractStaticParms(args);
    }

    private String[] extractStaticParms(String[] args2) {
        ArrayList<String> params = new ArrayList<>(args2.length);
        for (String arg : args2) {
            arg = arg.trim();
            if (arg.startsWith("{") && arg.endsWith("}")) {
                if (valueSet == null)
                    valueSet = new ValueSet(arg.substring(1, arg.length() - 1));
                else
                    valueSet.add(new ValueSet(arg.substring(1, arg.length() - 1)));

            } else
                params.add(arg);
        }
        if (valueSet != null)
            L.status(valueSet);
        /*
         * Integer i; while ((i = valueSet.next()) != null) L.status("i:" + i);
         */
        return params.toArray(new String[0]);
    }

    private String[] args;

    //ParserCOL startParser;

    private String delimiter = ",";

    private String printDelimiter = ",";

    private boolean noHeaderLine;

    private BufferedReader feedIN = null;

    private ArrayList<String> filePrefixes = new ArrayList<>(10);

    public void addFeedIn(InputStream feedIn) {
        if (feedIN == null) feedIN = new MultipleCSVIS(feedIn, true);
    }

    private boolean trimHeaderPrint;

    private boolean tailInfo;

    private boolean removeQuote = false;

    private boolean filenameCol = false;

    private boolean appendheaderAtEnd;

    private Line appendHeaderLine;

    private String filenameColname;

//    private TFCol nextTFCol = null;

    private StringIS stringIS;

    private Integer tailCnt=null;

    private Integer headCnt=null;

    private Stack<String> lineStack=null;
    private Stack<Line> lineNewStack=null;

    private boolean debug;

    private boolean perf;

    private boolean fastCSVParser=false;

    private long startTime;

    private long duration;

    private static boolean newParser=false;

    private MultipleCSVIS1 newFeedIN;

    private Line prevLine;

    private int fromLine;

	private boolean disablePrintBuffer=false;//default false, means buffered

    private ParserCOL parseArgs(String[] args) throws InstantiationException, IllegalAccessException,
        ClassNotFoundException, AppException, IOException {
        ParserCOL startParser = null;
//        nextTFCol = null;
        lineStack = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                i++;
                delimiter = args[i];
                if (printDelimiter == null)
                    printDelimiter = delimiter;
            } else if (args[i].equals("-noheader")) {
                noHeaderLine = true;
            } else if (args[i].equals("-fastCSVParser")) {
                fastCSVParser = true;
            } else if (args[i].equals("-appendheader")) {
                appendheaderAtEnd = true;
            } else if (args[i].equals("-tail")) {
                tailCnt = Integer.parseInt(args[++i]);
                if (newParser)
                    lineNewStack = new Stack<Line>(tailCnt);
                else
                    lineStack = new Stack<String>(tailCnt);
            } else if (args[i].equals("-head")) {
                headCnt = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-trimheader")) {// remove header line
                trimHeaderPrint = true;
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else if (args[i].equals("-perf")) {
                perf= true;
            } else if (args[i].equals("-printdelimiter")) {
                printDelimiter = args[i + 1];
                i++;
            } else if (args[i].equals("-spacedelimiter")) {
                printDelimiter = " ";

            } else if (args[i].equals("-removeQuote")) {
                removeQuote = true;

            } else if (args[i].equals("-disablePrintBuffer")) {
            	disablePrintBuffer = true;

            } else if (args[i].equals("-filenameCol")) {
                filenameCol = true;
                filenameColname = args[++i];
            } else if (args[i].equals("-fromLine")) {
                
                fromLine = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-def")) {
                if (startParser == null)
                    startParser = new COLSeq();
                else
                    startParser.addParser(new COLSeq());
                startParser.addParser(new COLRow());

                startParser.addParser(new COLFixWth());
                startParser.addParser(new COLInfo());
            } else if (args[i].equals("-info")) {
                tailInfo = true;
            
            } else if (args[i].startsWith("COL") || chkClass(args, i)) {
                int subI = i;
                Class<?> clazz;
                // System.out.println(args[i]);
                clazz = Class.forName("run." + args[i]);
                ParserCOL parse = (ParserCOL) clazz.newInstance();

                i++;
                for (subI = i; subI < args.length; subI++) {
                    if (args[subI].startsWith("COL") || chkClass(args, subI) || args[subI].equals("TFCol"))// next set,
                                                                                                           // break
                        break;

                }

                parse.setArgs(ArrayTool.extract(args, i, subI - 1));
                parse.setPrintDelimiter(printDelimiter);
                parse.setDisablePrintBuffer(disablePrintBuffer);
                i = subI - 1;

                if (startParser == null)
                    startParser = parse;
                else
                    startParser.addParser(parse);
            } else if (new File(args[i]).exists()) {
                try {
                    if (!newParser) {
                        if (feedIN == null) feedIN = new MultipleCSVIS(args[i], !noHeaderLine,filenameCol ,filenameColname,fromLine);                        
                        else ((MultipleCSVIS)feedIN).add(args[i]);
                    } else {
                        if (newFeedIN == null) newFeedIN = new MultipleCSVIS1(args[i] ,!noHeaderLine,filenameColname,fromLine);
                        else ((MultipleCSVIS1)newFeedIN).add(args[i]);
                    }
                    
                    //L.status("Load file: "+args[i]);
                    String filePrefix = args[i];
                    if (filePrefix.indexOf('.') >= 0)
                        filePrefix = filePrefix.substring(0, filePrefix.indexOf('.'));
                    if (filePrefix.indexOf(C.DIR_SEPARATOR) >= 0)
                        filePrefix = filePrefix.substring(filePrefix.lastIndexOf(C.DIR_SEPARATOR) + 1);
                    filePrefixes.add(filePrefix);
                    //L.status("Load file " + args[i] + "=" + filePrefix);
                } catch (FileNotFoundException e) {
                     L.status("FileNotFoundException: "+args[i]);
                }
            } /*else if (args[i].equals("TFCol")) {
                String[] params = ArrayTool.extract(args, i + 1, args.length - 1);
                nextTFCol = new TFCol(params);
                break;
            }*/ else {
            	String doneParam="";
            	for (int j=0; j<i;j++) doneParam += args[j]+ " ";
            	String pendParam="";;
				for (int j=i+1; j<args.length;j++) pendParam += args[j]+ " ";
throw new AppException("Invalid TFCol param (not class?) done("+doneParam+") " + args[i] + " pending("+pendParam+")");
            }
        }

        if(!newParser) {
            
            if (feedIN == null) feedIN = new MultipleCSVIS(System.in, !noHeaderLine);
            if (headCnt !=null) ((MultipleCSVIS)feedIN).setHeadCnt(headCnt);
        } else {
            if (newFeedIN == null) newFeedIN = new MultipleCSVIS1();
            if (headCnt !=null) ((MultipleCSVIS)feedIN).setHeadCnt(headCnt);
        }
        // System.out.println(parsers);
        
        if (startParser == null)//default handling
            startParser = new COLFixWth();

        if (tailInfo) {
            if (startParser == null)
                startParser = new COLInfo();
            else
                startParser.addParser(new COLInfo());
        }
  /*      if (nextTFCol != null)
            startParser.setBuf(true);
  */      if (valueSet != null)
            startParser.setNoprint(true);
          startParser.setTrimHeaderPrint(trimHeaderPrint);
        return startParser;
    }

    class TFColStream extends PrintStream
    {

        public TFColStream() throws FileNotFoundException {
            super("/dev/null");
        }

        ArrayList<String> strings = new ArrayList<>(10);

        StringBuilder buf = null;

        @Override
        public void print(String s) {
            if (buf == null)
                buf = new StringBuilder(100);
            buf.append(s);
        }

        @Override
        public void println(String x) {
            if (buf != null) {
                buf.append(x);
                strings.add(buf.toString());
                buf = null;
            } else
                strings.add(x);
        }

    }

    private boolean chkClass(String[] args, int i) {
        // System.out.println(args[i]);
        try {
            Class<?> clazz;
            clazz = Class.forName("run.COL" + args[i]);
            args[i] = "COL" + args[i];// override class name
            return true;
        } catch (ClassNotFoundException e) {

            return false;
        }

    }

    static {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    /**
     * @param args
     * @throws Throwable 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws AppException
     */

    public static void main(String[] args) throws Throwable  {

    	try {
    		TFCol ret = tfcol(args);
    		if (ret.getSessionObj("FAIL")!=null && ret.getSessionObj("FAIL").equals("Y"))
    			System.exit(1);
    		System.exit(0);
    	} catch (Throwable e) {
    		System.err.println(  " TFCol ("+e.toString()+")" + ArrayTool.to(args));
    		throw e;
    	}
    }
    public static TFCol tfcol(String[] args) throws Throwable  {

        
        try {
            TFCol transformCol = new TFCol(args);
            if (System.getenv("NEW")!=null && System.getenv("NEW").equals("Y"))
                newParser=true;
            if (!newParser)
                transformCol.feedLoop();
            else
                transformCol.feedLoopNew();
            return transformCol;
        } catch (Throwable e) {
            e.printStackTrace();
//System.err.println(System.getProperty("PARENT_COMMAND") +  " TFCol Args: " + S.toString(args).replaceAll(",.*,.*,.*,.*,.*,.*,.*,.*,.*,.*,.*,.*,.*,.*,.*\\.csv","..."));
            System.err.println(System.getProperty("PARENT_COMMAND") +  " TFCol Args: " + S.toString(args));
System.exit(1);
            throw e;
        }
    }


    private Line parseLine(String readLine) {
        Line line;
        // String[] tokens = readLine.split(delimiter) ;
        if (fastCSVParser)
            line = new Line(S.toArrayList(readLine, delimiter.charAt(0), removeQuote), null);
        else
            line = new Line(S.toArrayList1(readLine, delimiter, removeQuote), null);
        return line;
    }

    

            
    void feedLoop() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, AppException {
        ArrayList<String[]> argsSet = new ArrayList<>(10);
        if (valueSet!=null) {
            while (valueSet.hasNext()) {
                valueSet.next();
                String[] newArgs = valueSet.set(args);
                argsSet.add(newArgs);
            }
        } else
            argsSet.add(args);
        
        if (argsSet.size()>1)
            stringIS = new StringIS();
        for (String[] processedArgs:argsSet ) {
            feed(processedArgs);
            if (stringIS !=null) {
                feedIN = stringIS;//use buffer from second time
                stringIS = null;//reset to null , so no need to buffer again from feed()
            } else if (feedIN instanceof StringIS) {
                ((StringIS)feedIN).reset(); //reset the buffer 
            }
        }
    }
    void feedLoopNew() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, AppException {
        ArrayList<String[]> argsSet = new ArrayList<>(10);
        if (valueSet!=null) {
            while (valueSet.hasNext()) {
                valueSet.next();
                String[] newArgs = valueSet.set(args);
                argsSet.add(newArgs);
            }
        } else
            argsSet.add(args);
        //L.status("argSet" + argsSet.size());
        
/*        if (argsSet.size()>1)
            stringIS = new StringIS();
*/        for (String[] processedArgs:argsSet ) {
            feedNew(processedArgs);
            
        }
    }
     
    public void startTiming() {
        startTime = System.nanoTime();
    }

    public void endTiming() {
        long endTime = System.nanoTime();
        duration += endTime - startTime;
        startTime = System.nanoTime();
        // if (endTime ==startTime) zeroCnt++;

    }

    void feed(String[] processedArgs)
        throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, AppException {
        ParserCOL startParser = parseArgs(processedArgs);
        if (debug)
            L.status("startParser: " + startParser);
        if (((MultipleCSVIS)feedIN).hasFiles())
            L.status("Load "+((MultipleCSVIS)feedIN).size()+ " files ...");
        
        String line = null;

        if (!noHeaderLine) {
            String hdrLineStr = feedIN.readLine();
            if (hdrLineStr == null)
                return;
            if (stringIS != null)
                stringIS.writeLine(hdrLineStr);
            Line headerLine = parseLine(hdrLineStr);
            headerLine.header = true;
            // System.out.println(this.getClass().getSimpleName() +
            // headerLine);
            //if (!trimHeaderPrint) { show not handle here, just suppress the print, not suppress processing otherwise, header info lost
                try {
                    startParser.preProcessHeader(headerLine);
                } catch (Exception e) {
                    L.status("Exception headerLine:" + hdrLineStr + "," + headerLine);
                    e.printStackTrace();
                }
            //}
            if (appendheaderAtEnd)
                appendHeaderLine = headerLine;
        }
        int processedCnt = 0;
        startTiming();
        while ((line = feedIN.readLine()) != null) {// read the fix msg
            if (stringIS != null)
                stringIS.writeLine(line);
            try {
                if (line.trim().isEmpty())
                    continue;
                if (lineStack == null) {
                    Line contentLine = parseLine(line);
                    endTiming();
                    startParser.buffer(contentLine);
                    startTiming();
                } else
                    lineStack.append(line);
                processedCnt++;
                if (startParser.isTeriminated()) {
                    // System.out.println("breaked");
                    break;
                }
            } catch (java.lang.IndexOutOfBoundsException e) {

            } catch (PrintedAppException e) {// skipped if already printed
                if (processedCnt == 0)// exit, should break for no succeed ever
                    return;
            } catch (Exception e) {
   L.status("Exception(processedCnt:"+processedCnt+"):" + line);
            
                e.printStackTrace();
                if (processedCnt == 0)// exit, should break for no succeed ever
throw new AppException("TFCol " + S.toString(processedArgs), e);
                	//return 0;
            }
        
        }
        if (lineStack != null) {
            for (String line1 : lineStack.getList()) {
                Line contentLine = parseLine(line1);
                endTiming();
                startParser.buffer(contentLine);
                startTiming();
            }
        }

        endTiming();
        startParser.preFlushHead();
        startParser.preFlush();
        if (appendheaderAtEnd)
            startParser.prePrintContent(appendHeaderLine);
        startParser.flushPrintStream();
        startTiming();
        /*
         * if (nextTFCol != null) nextTFCol.feed(startParser.getBufLines(), startParser);
         */
        
        if (perf||C.printPerf) {
            L.status("TFCOL PERF Dur:" + D.f(duration / 1000000, 2) );
            L.status("PERF: " + startParser.toPerfString());
        }
      
        this.sessionObj.putAll(startParser.getSessions());
    }

    void feedNew(String[] processedArgs)
        throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, AppException {
        ParserCOL startParser = parseArgs(processedArgs);
        if (debug)
            L.status("startParser: " + startParser);
        if (((MultipleCSVIS1)newFeedIN).hasFiles())
            L.status("Load New "+((MultipleCSVIS1)newFeedIN).size()+ " files ...");
        

        if (!noHeaderLine) {
            Line headerLine = newFeedIN.readLine();
            if (headerLine == null)
                return;
            
            headerLine.header = true;
            // System.out.println(this.getClass().getSimpleName() +
            // headerLine);
            if (!trimHeaderPrint) {
                try {
                    startParser.preProcessHeader(headerLine);
                } catch (Exception e) {
                    L.status("Exception headerLine:" +  headerLine);
                    e.printStackTrace();
                }
            }
            if (appendheaderAtEnd)
                appendHeaderLine = headerLine;
        }
        int processedCnt = 0;
        startTiming();
        Line line = null;
        while ((line = newFeedIN.readLine()) != null) {// read the fix msg
            try {
                if (line.cols.size() ==0)
                    continue;
                
                if (lineNewStack == null) {
                    endTiming();
                    startParser.buffer(line);
                    startTiming();
                } else
                    lineNewStack.append(line);
                processedCnt++;
                if (startParser.isTeriminated()) {
                    // System.out.println("breaked");
                    break;
                }
                prevLine = line;
            } catch (java.lang.IndexOutOfBoundsException e) {

            } catch (PrintedAppException e) {// skipped if already printed
                L.status((" newFeedIN.getCurrentFileNameOnly() " + newFeedIN.getCurrentFileNameOnly()));
                L.status("TFCol.PrintedAppException prev " + prevLine);
                L.status("TFCol.PrintedAppException line " + line);
                if (processedCnt == 0) {// exit, should break for no succeed ever
                    L.status((" newFeedIN.getCurrentFileNameOnly() " + newFeedIN.getCurrentFileNameOnly()));
                    return;
                }
            } catch (Exception e) {
                L.status("Exception:" + line);
                e.printStackTrace();
                if (processedCnt == 0)// exit, should break for no succeed ever
                    return;
            }
        
        }
        
        
        if (lineNewStack != null) {
            System.out.println("unexpected");
            for (Line contentLine : lineNewStack.getList()) {
                endTiming();
                startParser.buffer(contentLine);
                startTiming();
            }
        }

        endTiming();
        startParser.preFlushHead();
        startParser.preFlush();
        if (appendheaderAtEnd)
            startParser.prePrintContent(appendHeaderLine);
        startParser.flushPrintStream();
        startTiming();
        /*
         * if (nextTFCol != null) nextTFCol.feed(startParser.getBufLines(), startParser);
         */
        
        if (perf||C.printPerf) {
            L.status("TFCOL PERF Dur:" + D.f(duration / 1000000, 2) );
            L.status("PERF: " + startParser.toPerfString());
        }
      
        this.sessionObj.putAll(startParser.getSessions());
    }
        
    //for refeeed to next TFCol if exist
/*    private void feed(ArrayList<Line> bufLines, ParserCOL startParser) {

        for (Line line : bufLines) {
            try {
                if (line.header) {
                    Line bufHeadLine = (Line) line.clone();
                    startParser.preProcessHeader(bufHeadLine);
                    if (appendheader)
                        appendHeaderLine = bufHeadLine;

                } else
                    startParser.preProcessContent((Line) line.clone());

            } catch (Exception e) {
                L.status("Exception:" + line);
                e.printStackTrace();
            }
        }
        startParser.flushHead();
        startParser.flush();
        if (appendheader)
            startParser.printContent(appendHeaderLine);
        if (nextTFCol != null)
            nextTFCol.feed(startParser.getBufLines(),startParser);

    }
*/
 /*   public int getPrintedLineCnt() {
        return startParser.printLineCnt;
    }*/


    public Object getSessionObj( String key) {
    	return this.sessionObj.get(key);
    }
}



