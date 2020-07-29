package com.cvrskidz.servedown;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

/**
 * A worker responsible for reading the contents of files on disk, and processing
 * any necessary conversion between formats as well as not found (404) errors.
 */
public class FileHandler {
    private static final List<String> HTML_EXTENSIONS = Arrays.asList(new String[]{".html", ".md"}); //valid file extensions to return html

    //response members
    protected String contents;
    protected boolean error;
    protected String filename, directoryname, cwd, extension; //file properties

    /**
     * Creates an instance of a com.cvrskidz.servedown.FileHandler, processing the supplied path. If their is an error
     * reading the file, or the file does not exist the error attribute is set and the response
     * contents should not be read.
     *
     * @param path The path to the requested file
     * @return A com.cvrskidz.servedown.FileHandler containing the contents of the requested.
     */
    public static FileHandler newHandler(String path) {
        path = sanitize(path);
        String extension = path.substring(path.lastIndexOf("."), path.length());

        if (HTML_EXTENSIONS.contains(extension)) {
            return new HTMLHandler(path, Server.compileFlag);
        }
        else {
            return new CacheHandler(path);
        }
    }

    /**
     * Default constructor for all FileHandlers, without specific functionality for reading
     * HTML or cached files.
     *
     * @param path The path of the file to read.
     */
    public FileHandler(String path) {
        path = sanitize(path);
        extractLocations(path);

        if(!checkPath(path)) {
            error = true;
        }
    }

    /**
     * Prepares a path to be used by all com.cvrskidz.servedown.FileHandler instances.
     * <p>
     * All / are replaced with \ and the default file is appended to paths not specifying a file.
     * All files without file extensions are treated as markdown files.
     *
     * @param path The path to sanitize.
     * @return A properly formatted path.
     */
    private static String sanitize(String path) {
        path = path.replace('/', '\\');
        if(path.lastIndexOf("\\") == path.length()-1) { // e.g. example.com\Images\Nature\
            path += Server.defaultFile;
        }
        else if(!path.contains(".")) { //if the type of file is not specified
            path = path + ".md";
        }

        return path;
    }

    /**
     * Reads the various locations of a path.
     * <ul>
     * <li>The directoryname member is assigned the name of the directory containing the requested file.
     * <li>The filename member is assigned the name of the requested file.
     * <li>The extension member is assigned the file type.
     * <li>The cwd member is assigned the path to the servers content root.
     *
     * @param path The path to the file in the required format
     */
    private void extractLocations(String path) {
        int delimiterLocation = path.lastIndexOf('\\');
        directoryname = path.substring(0, delimiterLocation);
        filename = path.substring(delimiterLocation, path.lastIndexOf("."));
        extension = path.substring(path.lastIndexOf("."), path.length());
        cwd = Server.path.isEmpty() ? Path.of(".").toAbsolutePath().toString() : Server.path;
    }

    /**
     * Confirms the supplied file path can be read.
     *
     * @param path The path to the requested file.
     * @return True if the file is able to be read, else False
     */
    private boolean checkPath(String path) {
        if(Files.isRegularFile(Path.of(cwd + path))) {
            return true;
        }
        return false;
    }

    /**
     * Returns the contents of the file the com.cvrskidz.servedown.FileHandler is assigned.
     *
     * @param extension The extension of the file.
     * @return The contents of the assigned file.
     * @throws IOException If there is an error reading the assigned file.
     */
    protected String readFile(String extension) throws IOException {
        System.out.println("Reading: " + filename);
        FileReader file = new FileReader(cwd + directoryname + filename + extension);
        StringBuilder contents = new StringBuilder();

        int cBuffer = 0;
        while((cBuffer = file.read()) != -1) {
            contents.append((char)cBuffer);
        }
        return contents.toString();
    }

    /**
     * A getter method to access the error state of an instance of a  com.cvrskidz.servedown.FileHandler.
     *
     * @return The value of this.error
     * @see FileHandler
     */
    public boolean hadError() {
        return error;
    }

    /**
     * A getter method to access the contents read from the assigned file to this handler.
     *
     * @return The value of this.contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * A getter method to access the HTTP response for the assigned file request to this handler.
     * <p>
     * com.cvrskidz.servedown.FileHandler errors are automatically handled within the method, returning an appropriate response
     * if an invalid request was made (such as 404).
     *
     * @return A com.cvrskidz.servedown.HTTPResponse to be sent to a client
     * @see HTTPResponse
     */
    public String getResponse() {
        if(hadError()) {
            return new HTTPResponse("text/html", "NOT FOUND", 404, HTTPResponse.PROTOCOL.HTTP).toString();
        }

        return HTTPResponse.newResponse(extension, contents, HTTPResponse.PROTOCOL.HTTP).toString();
    }
}
