package com.cvrskidz.servedown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The com.cvrskidz.servedown.MarkdownConverter class is responsible for converting a markdown file to html.
 */
public class MarkdownConverter {
    //header information for the output file
    private static final String META_FORMAT = "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>\n";
    private static final String STYLE_FORMAT = "<link rel='stylesheet' type='text/css' href='%s'>\n";
    private static final String SCRIPT_FORMAT = "<script src='%s'></script>\n";

    private final int headings = 6; //smallest heading level
    private String[] contents; //contents of the html file
    private int line; //current line number of the file (-1 to be used as an index)
    private Stack<Integer> indentationLevel; //how far to indent the line converted to html in the output file
    private StringBuilder result, body; //result: the output. body: the boy tag of the output file
    private String head; //the header of the output file
    private ArrayList<String> documentHeadings; //list of all headings in file
    private boolean paragraphOpen, listOpen, blockOpen; //flags to indicate the status of conversion

    /**
     * Creates a new instance of a com.cvrskidz.servedown.MarkdownConverter object, converting the given content.
     *
     * @param contents Markdown to convert to html
     * @param title HTML page title
     * @param styles the paths to all stylesheets desired to be included in the page in order of precedence
     * @param scripts the paths of all scripts to be included in the html
     * @param procs inline javascript to include in the html
     */
    public MarkdownConverter(String contents, String title, String[] styles, String[] scripts, String[] procs) {
        this.contents = contents.split("\n"); //separate file by lines.
        result = new StringBuilder("<html>\n");
        body = new StringBuilder("<div id='content'>\n"); //#content is an id used for styling the output content area
        head = writeHeader(title, styles, scripts, procs); //write the page header
        paragraphOpen = false;
        listOpen = false;
        indentationLevel = new Stack<>();
        documentHeadings = new ArrayList<>();

        line = 0;
        while(line < this.contents.length) { //convert one line at a time
            convertLine(this.contents[line]);
            line++;
        }

        body.append("</div>\n");
        body.append(generateTOC()); //table of contents appended to file (after headings have been found)

        //concatenate output file
        result.append(head);
        result.append("<body>" + body.toString() + "</body>");
        result.append("</html>");
    }

    private String writeHeader(String title, String[] styles, String[] scripts, String[] procs) {
        StringBuilder head = new StringBuilder("<head>\n");
        head.append("<title>" + title + "</title>\n");
        head.append(META_FORMAT); //set charset

        //append all styles
        for(String ref : styles) {
            head.append(String.format(STYLE_FORMAT, ref));
        }

        //append all scripts
        for(String ref : scripts) {
            head.append(String.format(SCRIPT_FORMAT, ref));
        }
        head.append("<script id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>");

        //append all inline scripts
        for(String func : procs) {
            head.append("<script>"+func+"</script>\n");
        }

        head.append("</head>\n");
        return head.toString();
    }

    /**
     * Converts a line of markdown into HTML, with regard to the current document. Items that span multiple
     * lines such as code blocks, and single line items are identified and converted.
     * @param line The line to convert
     */
    private void convertLine(String line) {
        String trimmedLine = line.trim(); //remove whitespace for regex simpler without it

        if(trimmedLine.isBlank()) { //if blank line close the tag of whatever we were converting
            closeOpen();
        }
        else {
            if(!blockOpen) {
                convertLineItem(line);
            }
            else if(!convertSeperator(trimmedLine) && !convertTable(trimmedLine)){ //convert tables and separators first, if they fail proceed to text
                if (blockOpen) { //if line is apart of open paragraph
                    line = line.replace("<", "&lt;");
                    line = line.replace(">", "&gt;");

                    body.append(line + "\n");
                }
            }
        }
    }

    /**
     * Helper function.
     * Converts a single line markdown item into HTML. For example headings, lists items, and quotes.
     * @param line The single line item.
     * @see MarkdownConverter#convertLine
     */
    private void convertLineItem(String line){
        int level = 0; //buffer for style of element, heading level for headings, bullet indentation for lists etc.
        String trimmedLine = line.trim();

        if ((level = checkHeading(trimmedLine)) > 0) { //headings
            convertHeading(line, level);
        }
        else if((level = checkList(line)) >= 0) { //bullet points
            convertListItem(line, level);
        }
        else if(checkQuote(trimmedLine)) { //quotes
            convertQuote(trimmedLine);
        }
        else if(!convertSeperator(trimmedLine) && !convertTable(trimmedLine)) {
            convertParagraph(trimmedLine);
        }
    }

    private int checkHeading(String heading) {
        heading = heading.trim();
        char[] contents = heading.split(" ")[0].toCharArray();
        int level = 0;

        for(int i =0; i < contents.length; ++i) {
            if(contents[i] != '#') {
                return 0;
            }
            else {
                level++;
            }
        }

        return level;
    }

    private void convertHeading(String line, int level) {
        closeOpen();
        line = formatLine(line.trim());
        String headingContent = line.substring(level, line.length()).trim();
        String headingName = headingContent.replaceAll("<[^<]*>", ""); //remove all html tags from headingName
        String id = headingName.replace(' ', '-');
        body.append(String.format("<div id=%s><h%d>%s</h%d></div>\n", id, level, headingContent, level));
        documentHeadings.add(headingName);
    }

    private int checkList(String line) {
        char[] lineContents = line.toCharArray();
        int whitespace = 0;

        for (int i =0; i < lineContents.length; ++i) {
            if(Character.isWhitespace(lineContents[i])) {
                ++whitespace;
            }
            else if (lineContents[i] == '-' || lineContents[i] == '*') {
                if(lineContents[i+1] == ' ') {
                    return whitespace;
                }
                else {
                    return - 1;
                }
            }
            else {
                return - 1;
            }
        }

        return -1;
    }

    private void convertListItem(String line, int level) {
        String trimmedLine = line.trim();
        String text = "";

        try {
            text = trimmedLine.substring(2, trimmedLine.length());
        }
        catch (StringIndexOutOfBoundsException e) { //invalid bullet point
            return;
        }

        if(indentationLevel.size() == 0 || indentationLevel.lastElement() < level) {
            body.append("<ul>");
            indentationLevel.add(level);
            listOpen = true;
        }
        else if(indentationLevel.lastElement() > level) {
            body.append("</ul>");
            indentationLevel.pop();
        }

        if(listOpen) {
            body.append("<li>" + formatLine(text));
        }
        else {
            body.append("<ul>\n<li>" + formatLine(text));
            listOpen = true;
        }
    }

    private boolean checkQuote(String line) {
        line = line.trim();
        String[] tokens = line.split("\\s");

        if(tokens[0].equals(">")) {
            return true;
        }

        return false;
    }

    private void convertQuote(String line) {
        StringBuilder quote = new StringBuilder("<blockquote>");
        line = line.trim();
        quote.append(line.substring(1, line.length()));
        quote.append("</blockquote>\n");
        body.append(quote.toString());
    }

    private boolean convertSeperator(String line) {
        if(line.matches("---\\s*")) {
            body.append("<hr>\n");
            return true;
        }
        else if(line.matches("\\s*```.*")) {
            blockOpen = !blockOpen;
            if(blockOpen) {
                body.append(String.format("<pre class='language-%s'>\n<code>", line.contains("Python") ? "py" : "cpp"));
            }
            else {
                body = body.replace(body.length()-1, body.length(), "");
                body.append("</code>\n</pre>\n");
            }

            return true;
        }
        return false;
    }

    private boolean convertTable(String line) {
        //check if 1st line is header followed by separator
        if(checkTableRow(line) && checkTableDelim(this.contents[this.line + 1])) {
            this.line += 2; //move current parsing line
            StringBuilder table = new StringBuilder("<table>");

            //create column headings
            table.append(convertTableRow(line, true));

            //convert all applicable table rows below delimiter
            String lineBuffer = this.contents[this.line].trim();
            while(checkTableRow(lineBuffer)) {
                table.append(convertTableRow(lineBuffer, false));
                lineBuffer = this.contents[++this.line].trim();
            }

            body.append(table + "</table>\n");
            return true;
        }

        return false;
    }

    private boolean checkTableRow(String line) {
        // detects text within two (|)'s and any following text ended with a (|)
        String format = "(\\|\\s*.*\\s*\\|)(\\s*.*\\s*\\|)*";

        return line.matches(format);
    }

    private String convertTableRow(String line, boolean headers) {
        String cellTag = headers ? "th" : "td";
        StringBuilder out = new StringBuilder("<tr>");

        //replace math blocks to prevent column conflicts (|)
        String compatibleLine = line.replaceAll("\\$[^\\$]*\\$", "[math]");
        String[] mathBlocks = this.regexDelimiterContents("\\$[^\\$]*\\$", line);

        String[] rawCells = compatibleLine.split("\\|");
        //remove first blank row from raw cell strings
        List<String> cells = Arrays.asList(rawCells).subList(1, rawCells.length);

        int head = 0; //current math block index

        for (String i : cells) {
            i = formatLine(i);
            //reinsert math blocks
            int numOfBlocks = this.occurrences("\\[math\\]", i);
            for(int j = 0; j < numOfBlocks; ++j) {
                mathBlocks[head] = mathBlocks[head].replace("\\","\\\\");
                i = i.replaceFirst("\\[math\\]", mathBlocks[head].replace("$", "\\$"));
                head++;
            }
            out.append(String.format("<%s>%s</%s>", cellTag, i, cellTag));
        }

        out.append("</tr>");
        return out.toString();
    }

    private boolean checkTableDelim(String line) {
        // detects (-)'s within two (|)'s and any following (-)'s ended with a (|)
        String format = "(\\|\\s*-+\\s*\\|)(\\s*-+\\s*\\|)*\\s*";

        return line.matches(format);
    }

    private void convertParagraph(String line) {
        if(paragraphOpen) {
            body.append(formatLine(line)+ "\n");
        }
        else if (listOpen) {
            boolean buffer = paragraphOpen;
            paragraphOpen = false;
            closeOpen();
            paragraphOpen = buffer;
        }
        else {
            body.append(String.format("<p>\n%s\n", formatLine(line)));
            paragraphOpen = true;
        }
    }

    private String formatLine(String line) {
        line = line.trim();
        line = emphasiseLine(line, MarkdownCharacterFormat.HIGHLIGHT); //format highlights first to escape characters used for code
        line = emphasiseLine(line, MarkdownCharacterFormat.BOLD);
        line = emphasiseLine(line, MarkdownCharacterFormat.ITALIC);

        return completeLinks(line);
    }

    private String completeLinks(String line) {
        String linkFormat = "\\[[^\\[]*\\]\\([^\\(]*\\)";
        String imageFormat = "!\\[[^\\[]*\\]\\([^\\(]*\\)";

        if(line.matches("\\s*.*" + linkFormat + ".*\\s*")) {
            StringBuilder out = new StringBuilder();
            String[] tokens = line.split(linkFormat);
            boolean isImage = line.matches("\\s*.*" + imageFormat + ".*\\s*") ? true : false;
            int linepos = 0;

            //complete links in line
            for (int i = 0; i < tokens.length - 1; ++i) {
                int start = tokens[i].length() + linepos;
                linepos = line.indexOf(')', start);
                String contents = line.substring(start, linepos + 1);
                if (tokens[i].contains("!")) {
                    tokens[i] = tokens[i].replace('!', ' ');
                }
                out.append(tokens[i]);
                out.append(anchorURLTag(contents, isImage));
            }

            //complete leftovers at the end of lines
            switch (tokens.length) {
                case 1:
                    if (isImage) { tokens[0] = tokens[0].replace('!', ' '); }
                    out.append(tokens[0]);
                    out.append(anchorURLTag(line, isImage));
                    break;
                case 0: //can't be an image
                    out.append(anchorURLTag(line, isImage));
                    break;
                default:
                    out.append(tokens[tokens.length - 1]);
                    break;
            }

            return out.toString();
        }

        return line;
    }

    private String anchorURLTag(String markdownLink, boolean isImage) {
        if(isImage) {
            return imageURLTag(markdownLink);
        }

        String text = markdownLink.substring(markdownLink.indexOf('[') + 1, markdownLink.indexOf(']'));
        String ref = markdownLink.substring(markdownLink.indexOf('(') + 1, markdownLink.indexOf(')'));

        return "<a href = " + ref + ">" + text + "</a> ";
    }

    private String imageURLTag(String markdownLink) {
        String text = markdownLink.substring(markdownLink.indexOf('[') + 1, markdownLink.indexOf(']'));
        String ref = markdownLink.substring(markdownLink.indexOf('(') + 1, markdownLink.indexOf(')'));
        String image = "";
        if(ref.matches("data:image/.*;base64,.*")) {
            image = ref;
        }
        else {
            String imageType  = ref.substring(ref.lastIndexOf('.') + 1);
            image = String.format("data:image/%s;base64,", imageType);
            ref = ref.replaceFirst("\\.\\./", "/");
            ref = ref.replaceFirst("\\.\\.\\\\", "/");

            try {
                image += CacheHandler.readFileBytes(ref);
            }
            catch (IOException e) {
                System.err.println("--LOG--\nError reading image from " + ref + "\nInserting blank link\n--END LOG--");
            }
        }

        return "<image src = '" + image + "' alt = " + text + ">";
    }

    private String emphasiseLine(String line, MarkdownCharacterFormat emphasis) {
        line = line.replaceAll("\\\\\\*", "&#42;"); //replace all escaped formatters
        StringBuilder emphasised = new StringBuilder();
        int tokenLength = emphasis.getToken().length();
        while(line.matches(genRegexBoundary(emphasis.getToken()))) {
            int startIndex = line.indexOf(emphasis.getToken());
            int endIndex = line.indexOf(emphasis.getToken(), startIndex + tokenLength);
            // Store before first asterisk
            emphasised.append(line.substring(0, startIndex));
            // Store bold portion
            emphasised.append(emphasis.enclose(line.substring(startIndex + tokenLength, endIndex)));
            // Remove appended values
            line = line.substring(endIndex + tokenLength, line.length());
        }

        // Append remaining
        emphasised.append(line);
        return emphasised.toString();
    }

    private String generateTOC() {
        StringBuilder out = new StringBuilder("<div id='toc'>");

        for (String id : documentHeadings) {
            out.append(String.format("<a href=\"#%s\">%s</a>", id.replace(' ', '-'), id));
        }

        out.append("</div>");
        return out.toString();
    }

    private void closeOpen() {
        if(paragraphOpen) {
            body.append("</p>\n");
            paragraphOpen = false;
        }
        else if(listOpen) {
            body.append("</ul>\n");
            indentationLevel.pop();
            if (indentationLevel.size() > 0) {
                closeOpen();
            }
            else {
                listOpen = false;
            }
        }
    }

    /**
     * Generates a regular expression to find characters within boundary characters
     * @return a regular expression to be used with matches()
     */
    public static String genRegexBoundary(String token) {
        StringBuilder expression = new StringBuilder(".*");
        char[] characters = token.toCharArray();

        for(int pass = 0; pass < 2; pass++) {
            for (char i : characters) {
                switch(i) {
                    case '*':
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                        expression.append("\\");
                        break;
                }
                expression.append(i);
            }

            expression.append(".*");
        }

        return expression.toString();
    }

    /**
     * Returns the removed contents of a String after calling split() with a regex delimiter
     *
     * @param delimiter The delimiter (token) to be separate the string.
     * @param line The original line containing the tokens o be removed by split()
     * @return The removed tokens
     */
    public static String[] regexDelimiterContents(String delimiter, String line) {
        String[] splitLine = line.split(delimiter); //split the line
        int start = 0; //start position of the current delimiter
        ArrayList<String> contents = new ArrayList<>();

        for (int i = 0; i < splitLine.length-1; ++i) {
            String before = splitLine[i]; //the token before delimiter
            String after = splitLine[i + 1]; //the token after delimiter

            start += before.length(); //update the start position to the beginning of current delimiter
            int end = line.indexOf(after, start); //end index of delimiter

            contents.add(line.substring(start, end));
            start = end; //update start position ready for next iteration
        }

        String[] out = new String[contents.size()];
        return contents.toArray(out);
    }

    public static int occurrences(String regex, String line) {
        Pattern find = Pattern.compile(regex);
        Matcher finder = find.matcher(line);

        int count = 0;
        while(finder.find()) { count++; }

        return count;
    }

    public String toString() {
        return result.toString();
    }
}
