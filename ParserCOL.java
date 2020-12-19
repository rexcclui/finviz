package util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import data.Line;
import data.LineGrp;
import data.LineIndex;

public abstract class ParserCOL
{

    protected static final String DEFAULT_KEY = "DEFAULT_KEY";

    protected static final String INDEX = "INDEX";

    private static final double MULTIPLER_0_01 = 0.01;

    private static final double MULTIPLER_T = 1000000000000l;

    private static final double MULTIPLIER_B = 1000000000;

    private static final double MULTIPLIER_M = 1000000;

    private static final double MULTIPLIER_K = 1000;

    private static int seed = 0;

    protected String[] args;

    private String myname = getClass().getSimpleName();

    public LineIndex getLineIndex() {
        return lineIndex;
    }

    protected boolean firstLineInd = true;

    // protected int lineCnt = 0;

    protected String printDelimiter = ",";

    protected boolean asyn = false;// run in asyn mode

    /**
     * INDEX > KEY1 >VALUE1>KEY2>VALUE2 > LINE e.g. INDEX > DATE > 1-JAN-2018 > SYMBOL > GOOG > LINES
     * 
     */
    /*
     * protected LinkedHashMap<String,Object> sessionContext = new LinkedHashMap<>();
     * 
     * public LinkedHashMap<String,Object> getSessionContext() { return sessionContext; }
     */
    protected LineIndex lineIndex = new LineIndex();

    private Integer prvColSize;
    /**
     * 
     * @param keyValues
     *        DATE, 20180101, SYMBOL, GOOG
     * @param line
     * @return
     * @throws AppException
     */
    public ArrayList<Line> indexLine(String[] keyValues, Line line) throws AppException {
        return lineIndex.indexLine(keyValues, line);
    }

    public Set<String> getIndexKeyValues(String[] keyValues) throws AppException {
        return lineIndex.getIndexKeyValues(keyValues);
    }

    public ArrayList<Line> getIndexLines(String[] keyValues) throws AppException {
        return lineIndex.getIndexLines(keyValues);
    }

    public ArrayList<Line> removeIndexLines(String[] keyValues) throws AppException {
        return lineIndex.removeIndexLines(keyValues);
    }

    public void setLineIndex(LineIndex lineIndex) {
        // System.err.println(this+" " + sessionContext + " " + sessionContext.size());
        this.lineIndex = lineIndex;
        if (nextParser != null)
            nextParser.setLineIndex(lineIndex);

    }

    public String getPrintDelimiter() {
        return printDelimiter;
    }

    protected PrintStream out = new PrintStream(new BufferedOutputStream(System.out, 20000));

    public void setOut(PrintStream out) {
        // L.status("set out " + out.getClass().getSimpleName());

        this.out = new PrintStream(new BufferedOutputStream(out, 20000));
        if (nextParser != null)
            nextParser.setOut(this.out);
    }

    protected ParserCOL nextParser;

    protected int lineNoCnt = 0;

    protected boolean buf = false;// buffer , not to print

    protected boolean noprint = false;// not to print

    public boolean isNoprint() {
        return noprint;
    }

    public void setNoprint(boolean noprint) {
        this.noprint = noprint;
    }

    protected int level = 0;

    protected boolean debugConsole =
        System.getenv("DEBUG_CONSOLE") == null ? false : System.getenv("DEBUG_CONSOLE").equals("Y");

    public int printLineCnt;

    // protected ArrayList<String> colnames = new ArrayList<>(10);

    protected ArrayList<String> impactColnames = new ArrayList<>();

    protected ArrayList<Integer> impactCols = new ArrayList<>(10);

    protected ArrayList<Line> bufLines;

    private int consolePrintCnt = 0;

    private Integer consoleLineLimit;

    private StringBuilder debugMsgBuf;

    private int maxDebugLineLen = 0;

    protected Line headerLine;

    private long startTime, startPrintTime;

    private long duration, printDuration;

    private int zeroCnt = 0;

    public boolean teriminated;

    private String lastErrMsg = "NoError";

    protected Line printHeaderLine;

    private StringBuilder sb;

    private int id = ParserCOL.seed++;

    private String executorName = this.getClass().getSimpleName() + "_" + id;

    protected PrintedAppException exception;

    private ThreadPoolExecutor executor;

    private ThreadPoolExecutor printExecutor;

    private long receivedPrintTime;

    private boolean trimHeaderPrint=false;

    // protected long batchProcessTime = 100;

    // protected long batchStartTime = System.currentTimeMillis();

    protected HashMap<String, Object> sessionObj = new HashMap<String,Object> ();

    private boolean chkCommaInVal;

	private Line prvContentLIne;

	private boolean disablePrintBuffer=false;
    
    public void setChkCommaInVal(boolean chkCommaInVal) {
        this.chkCommaInVal = chkCommaInVal;
    }

    
    public void addColIndexOrName(ArrayList<String> colIndexOrNames) {
        for (String colIndexOrNam : colIndexOrNames)
            addColIndexOrName(colIndexOrNam);
    }

    public boolean isTeriminated() {
        if (!teriminated && nextParser != null)
            return nextParser.isTeriminated();
        return teriminated;

    }

    public void addColIndexOrName(String... colIndexOrNames) {
        for (String colIndexOrNam : colIndexOrNames)
            addColIndexOrName(colIndexOrNam);
    }

    protected static void addColIndexOrName(String colIndexOrName, ArrayList<String> colNames,
        ArrayList<Integer> cols) {
        try {
            Range colRange;
            colRange = Range.extractRange(colIndexOrName);
            for (int col : colRange.getValues()) {
                cols.add(col);
            }
        } catch (NumberFormatException e) {
            colNames.add(colIndexOrName);
        }
    }

    protected void addColIndexOrName(String colIndexOrName) {
        if (!colIndexOrName.startsWith("-"))// skip for -flag
            addColIndexOrName(colIndexOrName, impactColnames, impactCols);
    }

    // Pad names into col position
    protected boolean setImpactColIndex(Line headerLine) {
        /*
         * // System.err.println(S.toString(impactCols)); List<String> origCols = headerLine.cols; for (String
         * chkColname : impactColnames) {// add all by names Integer colIndex = 0; boolean found = false; for (String
         * colname : origCols) { if (colname.trim().equals(chkColname)) {// Search the colname with its index
         * impactCols.add(colIndex); found = true; break; } colIndex++; } if (!found)
         * System.err.println(this.getClass().getSimpleName() + " not found ImpactCol " + chkColname + " from " +
         * headerLine); } impactColnames.clear();// reset all, align to impactCols
         * //System.err.println(">"+S.toString(impactCols)); for (int i = 0; i < impactCols.size(); i++) { if
         * (impactCols.get(i)>=headerLine.cols.size()) { impactCols.remove(i--);//remove invalid column continue; }
         * impactColnames.add(headerLine.cols.get(impactCols.get(i))); }
         */// use below static method
       return  ParserCOL.setColIndex(headerLine, impactColnames, impactCols);
    }

    protected static boolean setColIndex(Line headerLine, ArrayList<String> colnames, ArrayList<Integer> cols) {
        // System.err.println(S.toString(impactCols));
        List<String> origCols = headerLine.cols;
        boolean chkStatus=true;
        for (String chkColname : colnames) {// add all by names
            Integer colIndex = 0;
            boolean found = false;
            for (String colname : origCols) {
                if (colname.trim().equals(chkColname)) {// Search the colname with its index
                    cols.add(colIndex);
                    found = true;
                    break;
                }
                colIndex++;
            }
            if (!found) {
            	for (String colname : origCols) {
                    if (colname.trim().contains(" "+chkColname+"\u001B")||colname.trim().contains("m"+chkColname+"\u001B")) {// Search the colname within color code
                        cols.add(colIndex);
                        found = true;
                        break;
                    }
                    colIndex++;
                }
            	if (!found) {
            		printErr1("Not found ImpactCol " + chkColname + " from " + headerLine);
            		chkStatus =false;
            	}
            }
        }
        
       
        
        
        colnames.clear();// reset all, align to impactCols
        // System.err.println(">"+S.toString(impactCols));
        for (int i = 0; i < cols.size(); i++) {
            if (cols.get(i) >= headerLine.cols.size()) {
                cols.remove(i--);// remove invalid column
                continue;
            }
            colnames.add(headerLine.cols.get(cols.get(i)));
        }
		return chkStatus;
    }

    /*
     * protected int getColPos(String colNameOrPos2) { for (int i = 0; i < colnames.size(); i++) { String colName =
     * colnames.get(i); if (colName.equals(colNameOrPos2)) return i; } //
     * System.err.println("ParserCOL.getColPos colname not found " + colNameOrPos2 + " " + S.toString(colnames)); return
     * -1; }
     */

    public void setArgs(String[] args) throws ConfigException {

        try {
            ArrayList<String> tmpArgs = new ArrayList<String>(10);
            for (String arg : args) {
                if (arg.toLowerCase().equals("-debug"))
                    debugConsole = true;
                else if (arg.toLowerCase().equals("-asyn")) {
                    asyn = true;
                    executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                } else
                    tmpArgs.add(arg);
            }
            this.args = tmpArgs.toArray(new String[0]);
            _setArgs(this.args);
        } catch (Exception e) {
            throw new ConfigException("setArgs: " + this, e);
        }
    }

    public abstract void _setArgs(String[] args2) throws ConfigException;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + Arrays.toString(args) + " buf:" + (buf ? "Y" : "N") + " noPrint:"
            + (noprint ? "Y" : "N") + (nextParser != null ? (">" + nextParser) : "");
    }

    public double getTotalDur() {
        double totalDur = getDuration();
        if (nextParser != null)
            totalDur += nextParser.getTotalDur();
        return totalDur;
    }

    public double getDuration() {
        return duration / 1000000; // from ns to ms
    }

    public double getPrintDuration() {
        return printDuration / 1000000; // from ns to ms
    }

    public String toPerfString() {
        return this.getClass().getSimpleName() + "(Dur:" + D.f(getDuration(), 2) + ", printDur:"
            + D.f(getPrintDuration(), 2) + ")"

            + (nextParser != null ? (" > " + nextParser.toPerfString())

                : "");
    }

    public void addParser(ParserCOL parse) {
        parse.setLineIndex(lineIndex);
        if (nextParser == null) {
            nextParser = parse;
            nextParser.level = level + 1;
        } else
            nextParser.addParser(parse);

    }

    public final void preProcessHeader(Line headerLine) throws ConfigException, PrintedAppException {
        startTiming();
        // System.out.println(this.getClass().getSimpleName() + headerLine);
        try {
            boolean chkStatus = setImpactColIndex(headerLine);
            if (!chkStatus)
            	printErr1("Failed to setImpactColIndex " + toString()  + " headerLine "+ headerLine);
            // colnames = new ArrayList<>(10);
            /*
             * for (String colname : headerLine.cols) // pre set header names colnames.add(colname);
             */ processHeader(headerLine);

        } catch (PrintedAppException e) {
            throw e;
        } catch (Exception e) {
            printErr("preProcessHeader", headerLine, e);
            throw new PrintedAppException("preProcessHeader", e);

        }
        endTiming();

    }

    public void processHeader(Line headerLine) throws ConfigException, PrintedAppException {
        // System.out.println(this.getClass().getSimpleName() + headerLine);
        // colnames = new ArrayList<>(10);

        /*
         * for (String colname : headerLine.cols) // reset header names colnames.add(colname);O
         */ this.headerLine = headerLine;
        if (nextParser != null)
            nextParser.preProcessHeader(headerLine);
        else
            printHeader(headerLine);
    }

    final protected void printHeader(Line headerLine) {
        if (trimHeaderPrint)
            return;
        startPrintTiming();
        printHeaderLine = headerLine;
        printContent(headerLine);
        printLineCnt--;// temporary solution
        endPrintTiming();
    }

    public final void buffer(final Line contentLine) throws AppException {
        if (this.exception != null)
            throw exception;
        
        int colSize = contentLine.cols.size();
        if (prvColSize!=null && prvColSize != colSize) {
        	System.err.println("Skip Problem Line: prvColSize("+prvColSize+") != colSize("+colSize+") " + "\n"+prvContentLIne+ "\n"+ contentLine);
        	
        	return;
        }
        prvColSize = colSize;
        prvContentLIne = contentLine;
        if (asyn) {
            if (bufLines == null)
                bufLines = new ArrayList<Line>(13000);
            bufLines.add(contentLine);
            if (bufLines.size() > 10000) {
                while (executor.getQueue().size() > 4)
                    ;
                /*
                 * try { Thread.sleep(5); } catch (InterruptedException e) { // TODO Auto-generated catch block
                 * e.printStackTrace(); }
                 */
                this.flushLines();// flush
            }

        } else
        		preProcessContent(contentLine);

    }

    private final void preProcessContent(final Line contentLine) throws AppException {

        if (exception != null)// throw prev exception if exist;
            throw exception;

        /*
         * Runnable run = new Runnable() {
         * 
         * @Override public void run() {
         */
        startTiming();
        contentLine.lineNo = lineNoCnt++;
        /*
         * if (lineNoCnt % 1000 == 0) { batchProcessTime = System.currentTimeMillis() - batchStartTime; batchStartTime =
         * System.currentTimeMillis(); }
         */
        try {
            processContent(contentLine);
        } catch (IndexOutOfBoundsException e) {
            printErr("preProcessContent:" + contentLine, ExpTool.getStackTraceStr(e));
            // throw new PrintedAppException("preProcessContenFiltered", e);
            throw new PrintedAppException("preProcessContenFiltered", e);
        } catch (PrintedAppException e) {
            throw e;
        } catch (NullPointerException e) {
        	//throw new AppException("preProcessContenFiltered ", e);
        	throw new AppException (" preProcessContenFiltered ("+S.toString(args)+")\n headerLine " + this.headerLine  + " \n" + contentLine  ,e);
        	
        } catch (Exception e) {
            printErr("preProcessContent:" + contentLine, e);
            throw new PrintedAppException("preProcessContenFiltered", e);

        }
        endTiming();
        /*
         * }
         * 
         * };
         */
        if (asyn) {
            /*
             * while (executor.getQueue().size() > 1000) try { //System.err.println(this.getClass().getSimpleName() +
             * " executor.getQueue().size() "+executor.getQueue().size() +":"+batchProcessTime*90);
             * Thread.sleep(batchProcessTime/4); } catch (InterruptedException e) { // TODO Auto-generated catch block
             * throw new PrintedAppException("preProcessContenFiltered", e); } executor.execute(run);
             */
        } /*
           * else run.run();
           */
        if (exception != null)
            throw exception;

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

    public void startPrintTiming() {
        startPrintTime = System.nanoTime();
    }

    public void endPrintTiming() {
        long endPrintTime = System.nanoTime();

        printDuration += endPrintTime - startPrintTime;
        // if (endTime ==startTime) zeroCnt++;

    }

    protected void processContent(Line contentLine) throws AppException {

        // try {

        if (nextParser != null) {
            if (debugConsole)
                prePrintContent(contentLine);// print row to debug current state
            nextParser.buffer(contentLine);
        } else
            prePrintContent(contentLine);
        /*
         * } catch (Exception e) { printErr("processContent", contentLine, e);
         * 
         * }
         */
    }

    public Object getSessionObj( String key) {
    	Object ret = this.sessionObj.get(key);
    	if (ret!=null)    		return ret;
    	if (nextParser!=null) return nextParser.getSessionObj(key);
    	return null;
    }
    
    public Map<String,Object> getSessions() {
    	HashMap<String, Object> ret = new HashMap<String,Object> (10);
    	ret.putAll(this.sessionObj);
    	if (nextParser!=null) ret.putAll(nextParser.getSessions());
    	return ret;
    }
    public int getSessionObjSize( ) {
    	int ret = this.sessionObj.size();
    	
    	if (nextParser!=null) return ret+ nextParser.getSessionObjSize();
    	return ret;
    }
    
    public final void prePrintContent(Line line) {
        // startPrintTiming();
        printContent(line);
        // endPrintTiming();
    }

    protected void printContent(Line line) {
        printContent(line, out);
    }

    protected void printContent(final Line line, final PrintStream printOut) {
        if (printExecutor == null)
            printExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        if (noprint)
            return;
        startPrintTiming();
        // System.out.println("test");
        if (buf) {
            if (bufLines == null)
                bufLines = new ArrayList<Line>(100);
            bufLines.add(line);
            return;
        }
        if (debugConsole) {// for debug only

            debug(line);
        } else {
            if (sb == null)
                sb = new StringBuilder(8096 * 2);
            for (int i = 0; i < line.cols.size(); i++) {
                if (i > 0) {
                    if (!line.isNotPrintDeimiter())
                        sb.append(printDelimiter);
                    else
                        sb.append(' ');
                }
                String value = line.cols.get(i);
                if (chkCommaInVal && value.indexOf(',')>=0)
                    value = "\"" + value + "\"";
                sb.append(value);
            }
            sb.append('\n');
            if (disablePrintBuffer) {//disable print buffer for realtime information show
            	//System.err.println("disabled");
            	startPrintTiming();
                printOut.print(sb.toString());
                endPrintTiming();
                sb = new StringBuilder(8096 * 2);
            }
            else if (sb.length() > 8096 || (System.currentTimeMillis() - receivedPrintTime) > 1000) {// print right away if
                                                                                                // each print receive >
                                                                                                // 1 second
                final StringBuilder printBuf = sb;
                Runnable run = new Runnable()
                {

                    @Override
                    public void run() {
                        startPrintTiming();
                        printOut.print(printBuf.toString());
                        endPrintTiming();
                    }
                };
                endPrintTiming();
                printExecutor.execute(run);
                sb = new StringBuilder(8096 * 2);
                // run.run();
            } else
                endPrintTiming();
            printLineCnt++;
        }

        receivedPrintTime = System.currentTimeMillis();
    }

    public boolean isDisablePrintBuffer() {
		return disablePrintBuffer;
	}

	public void setDisablePrintBuffer(boolean disablePrintBuffer) {
		this.disablePrintBuffer = disablePrintBuffer;
		out = System.out;
	}

	protected void debug(Line line) {
        StringBuilder sb = new StringBuilder(line.cols.size() * 10);
        for (int i = 0; i < line.cols.size(); i++) {
            if (i > 0)
                sb.append(printDelimiter);
            sb.append(line.cols.get(i));
        }

        if (debugMsgBuf == null)
            debugMsgBuf = new StringBuilder(3000);

        String debugMsg = line.lineNo + "-" + level + " : " + sb.toString() + "\t< " + this + "\n";
        debugMsgBuf.append(debugMsg);
        if (maxDebugLineLen < debugMsg.length())
            maxDebugLineLen = debugMsg.length();

        consolePrintCnt++;
        if (consoleLineLimit == null)
            consoleLineLimit = Console.getLines();

        if (consolePrintCnt >= (consoleLineLimit - 1)) {

            try {
                System.err.print(S.toFixWidthColumn(debugMsgBuf.toString(), ",", " "));
            } catch (IOException e1) {
                e1.printStackTrace();
            } // limitation, last buf cannot print

            byte[] b = new byte[100];
            try {
                int len = System.in.read(b);
                for (int i = 0; i < consoleLineLimit; i++) {

                    System.err.print(Console.CURSOR_UP);

                    // for (int i= 0 ; i< debugMsg.length() ; i++) System.err.print("" + (char) 8 );
                    String eraseMsg = "";
                    for (int y = 0; y < maxDebugLineLen + 20; y++)
                        eraseMsg += " ";
                    System.err.print(eraseMsg);
                    eraseMsg = "";
                    for (int z = 0; z < maxDebugLineLen + 20; z++)
                        eraseMsg += (char) 8;
                    System.err.print(eraseMsg);
                }
                String input = new String(b, 0, len);
                if (input.toUpperCase().contains("GO"))
                    debugConsole = false;
            } catch (IOException e) {
            }
            consolePrintCnt = 0;// reset count
            maxDebugLineLen = 0;
            consoleLineLimit = Console.getLines();// update console screen lines
            debugMsgBuf = new StringBuilder(3000);
            printContent(headerLine);// add header for each debug print
        }
    }

    public ArrayList<Line> getBufLines() {
        return bufLines;
    }

    public boolean isBuf() {
        return buf;
    }

    public void setBuf(boolean buf) {
        this.buf = buf;
        if (nextParser != null)
            nextParser.setBuf(buf);
    }

    public final void preFlushHead() throws ConfigException, PrintedAppException {
        startTiming();
        flushHead();
        endTiming();
    }

    public void flushHead() throws ConfigException, PrintedAppException {
        flushLastAsynLines();
        if (nextParser != null)
            nextParser.preFlushHead();
    }

    /**
     * ensure all lines process on preProcessConten() asyn before.
     * 
     * @throws PrintedAppException
     */
    private void flushLines() throws PrintedAppException {
        if (!asyn)
            return;// no need to check for not asyn

        if (bufLines == null)
            return;
        final ArrayList<Line> swapBufLines = this.bufLines;
        this.bufLines = null;
        executor.execute(new Runnable()
        {

            @Override
            public void run() {
                for (Line contentLine : swapBufLines)
                    try {
                        preProcessContent(contentLine);
                    } catch (IndexOutOfBoundsException e) {
                        printErr("preProcessContent:" + contentLine, ExpTool.getStackTraceStr(e));
                        // throw new PrintedAppException("preProcessContenFiltered", e);
                        exception = new PrintedAppException("preProcessContenFiltered", e);
                        break;
                    } catch (PrintedAppException e) {
                        exception = e;
                        break;
                    } catch (Exception e) {
                        printErr("preProcessContent:" + contentLine, e);
                        exception = new PrintedAppException("preProcessContenFiltered", e);
                        break;
                    }
            }

        });

    }

    private void flushLastAsynLines() throws PrintedAppException {
        if (!asyn)
            return;// no need to check for not asyn
        if (nextParser != null) {
            nextParser.flushLastAsynLines();
        }
        if (exception != null)// throw prev exception if exist;
            throw exception;

        flushLines();
        if (exception != null)// throw prev exception if exist;
            throw exception;
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch blockp
            e.printStackTrace();
        }
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public final void preFlush() throws AppException {
        startTiming();
        flush();
        endTiming();

    }

    public void flush() throws AppException {
        if (nextParser != null) {
            nextParser.preFlush();
        }

    }

    public void flushPrintStream() throws PrintedAppException {
        flushLastAsynLines();
        startPrintTiming();
        if (printExecutor != null) {
            printExecutor.shutdown();
            try {
                printExecutor.awaitTermination(1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (sb != null)
            out.print(sb.toString());
        out.flush();
        if (nextParser != null)
            nextParser.flushPrintStream();
        endPrintTiming();
    }

    public void setPrintDelimiter(String printDelimiter2) {
        // TODO Auto-generated method stub
        printDelimiter = printDelimiter2;
    }

    protected Double getColorValue(String value) {
        return ParserCOL.trimValue(value);
    }

    @Deprecated
    /**
     * 
     * Use S.trimValue instead
     * @param value
     * @return
     */
    public static Double trimValue(String value) {
       return S.trimValue(value);
    }

    public static Double trimValueQuick(String value) {

        if (value.indexOf('.') >= 0)
            return Double.parseDouble(value);
        else
            return (double) Long.parseLong(value);

    }

    public static String trimStrValue(String value) {
        if (value.contains(AnsiC.prefix)) {
            // out.println(value + ":"+value.indexOf(AnsiC.prefix)+ ":"+value.length());
            String ret = value.substring(0, value.indexOf(AnsiC.prefix));
            if (value.length() > (value.indexOf(AnsiC.prefix) + AnsiC.BLINK_BLACK_BLACK.length()))
                ret += value.substring(value.indexOf(AnsiC.prefix) + AnsiC.BLINK_BLACK_BLACK.length());
            // out.println(ret + ":"+ret.length());

            return trimStrValue(ret).trim();
        }
        return value.trim();
    }

    public static Integer getColPosByName(String keyColName, Line headerLine) {
        for (int i = 0; i < headerLine.cols.size(); i++) {// search by name
            String colName = headerLine.cols.get(i);
            colName = trimStrValue(colName);
            if (colName.equals(keyColName))
                return i;
        }
        /*
         * System.err.println("ParserCOL.getColPos colname not found " + keyColNameOrPos + " " +
         * S.toString(headerLine.cols));
         */return null;
    }

    public static Integer getColPos(String keyColNameOrPos, Line headerLine) {
        try {
            Integer colPos = Integer.parseInt(keyColNameOrPos);
            if (headerLine == null)
                return colPos;// valid number and no header , assume ok
            else if (colPos < headerLine.cols.size())
                return colPos;
            else {
                System.err.println("colPos invalid  " + colPos + " > " + headerLine.cols.size());
                return null;
            }
        } catch (NumberFormatException e) {

        }
        for (int i = 0; i < headerLine.cols.size(); i++) {// search by name
            String colName = headerLine.cols.get(i);
            colName = trimStrValue(colName);
            if (colName.equals(keyColNameOrPos))
                return i;
        }
        /*
         * System.err.println("ParserCOL.getColPos colname not found " + keyColNameOrPos + " " +
         * S.toString(headerLine.cols));
         */return null;
    }

    public static ArrayList<Integer> getColPos(ArrayList<String> keyColNameOrPosList, Line headerLine) {
    	ArrayList<Integer> ret = new ArrayList<Integer>(10);
    	for (String keyColNameOrPos: keyColNameOrPosList) {
    		ret.add(getColPos (keyColNameOrPos, headerLine));
    	}
    	return ret;
    }

    protected void _indexChange(LineGrp lineGrp) throws AppException {
        startTiming();
        indexChange(lineGrp);
        endTiming();
        if (nextParser != null)
            nextParser._indexChange(lineGrp);
    }

    protected void indexChange(LineGrp lineGrp) throws AppException {
    }

    public static Double getCol(int backDate, ArrayList<Line> lines, Integer colPos) {
        Double origValue = null;
        if (lines.size() - 1 - backDate >= 0)
            origValue = lines.get(lines.size() - 1 - backDate).getDbleCol(colPos);
        if (origValue == null)
            return null;

        return origValue;
    }

    public static Double getColDiff(int backDate, int period, ArrayList<Line> lines, Integer colPos) {
        Double currentValue = lines.get(lines.size() - 1 - backDate).getDbleCol(colPos);
        if (currentValue == null)
            return null;
        Double origValue = null;
        if (lines.size() - 1 - backDate - period >= 0)
            origValue = lines.get(lines.size() - 1 - backDate - period).getDbleCol(colPos);
        if (origValue == null)
            return null;

        return currentValue - origValue;
    }

    public static Double getColDiffPer(int backDate, int period, ArrayList<Line> lines, Integer colPos) {
        Double currentValue = lines.get(lines.size() - 1 - backDate).getDbleCol(colPos);
        if (currentValue == null)
            return null;
        Double origValue = null;
        if (lines.size() - 1 - backDate - period >= 0)
            origValue = lines.get(lines.size() - 1 - backDate - period).getDbleCol(colPos);
        if (origValue == null)
            return null;

        return (currentValue - origValue) / origValue;
    }

    public String getImpactCol(int i) {
        if (impactColnames.size() > i)
            return impactColnames.get(i);
        return "" + impactCols.get(i);
    }

    public Double sumLines(ArrayList<Line> bLines, int colPos) throws AppException {
        Double total = 0.0;
        for (Line line : bLines) {
            Double value = line.getDbleCol(colPos);
            if (value == null)
                continue;
            total += value;
        }
        return total;
    }

    public void printErr(Object... msgs) {
        printErr(this, msgs);
    }

    public static void printErr1(Object... msgs) {
        printErr((ParserCOL) null, msgs);
    }

    public static void printErr(ParserCOL info, Object... msgs) {
        L.status("ERROR " + info);
        for (Object msg : msgs) {
            if (msg instanceof Exception)
                L.status("\t" + ExpTool.getStackTraceStr((Throwable) msg));
            else
                L.status("\t\t" + msg);
        }
    }

    public void printErrOnce(Object... msgs) {
        if (lastErrMsg.equals(msgs[0]))
            return;
        printErr(msgs);
    }

    public void setTrimHeaderPrint(boolean trimHeaderPrint) {
        this.trimHeaderPrint=trimHeaderPrint;
        if (nextParser!= null)
            nextParser.setTrimHeaderPrint(trimHeaderPrint);
    }

}
