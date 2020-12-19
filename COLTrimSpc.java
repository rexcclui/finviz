package run;

import java.util.LinkedHashSet;

import data.Line;
import util.AppException;
import util.ConfigException;
import util.ParserCOL;
import util.PrintedAppException;

public class COLTrimSpc extends ParserCOL
{

    LinkedHashSet<Integer> cols = new LinkedHashSet<Integer>();

    @Override
    public void _setArgs(String[] args) {
    }

    @Override
    public void processHeader(Line headerLIne) throws ConfigException, PrintedAppException {
        for (int col =0; col< headerLIne.cols.size(); col++) {
               String value = headerLIne.cols.get(col);
               headerLIne.cols.set(col, value.trim());
        }

        super.processHeader(headerLIne);
    }


    @Override
    public void processContent(Line contentLine) throws AppException {
        for (int col =0; col< contentLine.cols.size(); col++) {
               String value = contentLine.cols.get(col);
               contentLine.cols.set(col, value.trim());
        }

        super.processContent(contentLine);
    }

}
