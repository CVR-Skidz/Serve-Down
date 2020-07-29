package com.cvrskidz.servedown;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The com.cvrskidz.servedown.HTMLHandler class is responsible for processing all file requests that should
 * return HTML as a response. It controls the reading of HTML files, or conversion from
 * markdown to HTML.
 */
public class HTMLHandler extends FileHandler {
    //references to include in HTML output, relative to the servers content root.
    private static final String LOAD_SYNTAX = "hljs.initHighlightingOnLoad();";
    private static final String[] STYLE_REF = { "..\\.client\\style.css", "..\\.client\\syntax\\styles\\tomorrow.css"};
    private static final String[] SCRIPT_REF = {
            "..\\.client\\syntax\\highlight.pack.js",
            "..\\.client\\math\\mathjaxconfig.js",
            "https://polyfill.io/v3/polyfill.min.js?features=es6"
    };

    //flag to control the conversion of the target file
    private boolean doNotConvert;

    /**
     * Returns a new instance of a com.cvrskidz.servedown.HTMLHandler object, storing the HTML
     * inside the specified file and compiling it if necessary.
     *
     * @param path The path to the file containing HTML to read, or markdown to convert.
     * @param compileFlag A manual flag to force the compilation of a markdown file.
     *                    Setting this to false does not prevent markdown files from being compiled
     *                    if no corresponding HTML output can be found.
     */
    public HTMLHandler(String path, boolean compileFlag) {
        super(path);

        if(!hadError()) {
            doNotConvert = checkCache();

            if(compileFlag || !doNotConvert) {
                serve(true);
            }
            else {
                serve(false);
            }
        }
    }

    /**
     * Confirms whether a HTML file of the same name as the assigned file is present on disk.
     *
     * @return True if an output from compiling the assigned file is stored on disk. False otherwise.
     */
    protected boolean checkCache() {
        if(Files.exists(Path.of(cwd + directoryname + filename + ".html"))) {
            extension = ".html";
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Sets the response of this object to the correct contents, either compiling or reading from disk.
     *
     * @param compile A flag to compile the extracted path or read directly from it.
     */
    private void serve(boolean compile) {
        if(compile) {
            try {
                contents = compile();
                extension = ".html";
            }
            catch (Exception e) {
                System.err.println("--LOG--\nError reading file " + filename);
                e.printStackTrace();
                error = true;
            }
        }
        else {
            try {
                contents = readFile(".html");
            }
            catch (Exception e) {
                System.err.println("--LOG--\nError reading file\n" + e.toString() + "\n--END LOG--");
                error = true;
            }
        }
    }

    /**
     * Reads the contents of the assigned file into a new com.cvrskidz.servedown.MarkdownConverter, returning the
     * contents of the markdown file as HTML.
     *
     * @return The contents of a markdown file as HTML.
     * @throws IOException If there is an error reading the target file.
     */
    private String compile() throws IOException{
        System.out.println("Compiling: " + filename);
        String contents = readFile(".md");
        String result = new MarkdownConverter(contents, filename.substring(1), STYLE_REF, SCRIPT_REF,
                new String[] {LOAD_SYNTAX}).toString();

        FileWriter out = new FileWriter(cwd + directoryname + filename + ".html");
        out.write(result);
        out.flush();
        out.close();
        return result;
    }
}
