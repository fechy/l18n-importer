package org.kingmonkey.tools.l18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: add description
 *
 * @author Fernando Giovanini <fernando@teamkingmonkey.com>
 * @since 14.11.17
 */
public class CSVReader {

    private BufferedReader br;

    private boolean hasNext = true;

    private char separator;

    private char quotechar;

    private int skipLines;

    private boolean linesSkiped;

    public int linesCount = 0;

    public static final char DEFAULT_SEPARATOR = ',';
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final int DEFAULT_SKIP_LINES = 1;

    public CSVReader(Reader reader) {
        this(reader, DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_SKIP_LINES);
    }

    public CSVReader(Reader reader, char separator, char quotechar, int line) {
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
        this.skipLines = line;
    }

    public String[] readNext() throws IOException {
        String nextLine = getNextLine();
        return hasNext ? parseLine(nextLine) : null;
    }

    public String getNextLine() throws IOException {
        if (!this.linesSkiped) {
            for (int i = 0; i < skipLines; i++) {
                br.readLine();
            }
            this.linesSkiped = true;
        }
        String nextLine = br.readLine();
        if (nextLine == null) {
            hasNext = false;
        }
        return hasNext ? nextLine : null;
    }


    public List<String[]> readAll() throws IOException {

        List<String[]> allElements = new ArrayList<String[]>();
        while (hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null)
                allElements.add(nextLineAsTokens);
        }
        return allElements;

    }

    private String[] parseLine(String nextLine) throws IOException {

        if (nextLine == null) {
            return null;
        }

        List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                // continuing a quoted section, reappend newline
                sb.append("\n");
                nextLine = getNextLine();
                linesCount++;
                if (nextLine == null)

                    break;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                char c = nextLine.charAt(i);
                if (c == quotechar) {
                    if( inQuotes
                            && nextLine.length() > (i+1)
                            && nextLine.charAt(i+1) == quotechar ){
                        sb.append(nextLine.charAt(i+1));
                        i++;
                    }else{
                        inQuotes = !inQuotes;
                        if(i>2
                                && nextLine.charAt(i-1) != this.separator
                                && nextLine.length()>(i+1) &&
                                nextLine.charAt(i+1) != this.separator
                                ){
                            sb.append(c);
                        }
                    }
                } else if (c == separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return (String[]) tokensOnThisLine.toArray(new String[0]);

    }

    public void close() throws IOException{
        br.close();
    }
}
