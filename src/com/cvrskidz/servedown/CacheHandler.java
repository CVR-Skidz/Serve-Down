package com.cvrskidz.servedown;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * The com.cvrskidz.servedown.CacheHandler class controls reading files on disk. Creating an instance of a com.cvrskidz.servedown.CacheHandler will
 * read a file from disk. This operation could fail, causing the contents of the com.cvrskidz.servedown.CacheHandler to be empty.
 *<p><br>
 * The com.cvrskidz.servedown.CacheHandler also contains methods to read a file as bytes, rather than text. Which has less overhead.
 * This is encoded as base64.
 */
public class CacheHandler extends FileHandler{
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(new String[]{
            ".ico", ".png", ".jpeg", ".jpg", ".gif"});

    public CacheHandler(String path){
        super(path);

        if(!hadError() && checkCache()) {
            try{
                    contents = readFile(extension);
            }
            catch (IOException e) {
                System.err.println("--LOG--\nError reading file\n" + e.toString() + "\n--END LOG--");
                error = true;
            }
        }
    }

    /**
     * Checks whether the given extension is listed as a supported image file type.
     *
     * @param extension The extension to check.
     * @return True if the extension is listed, False otherwise.
     */
    public static boolean isImage(String extension) {
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Reads the contents of a file as bytes, and returns the result encoded in base64.
     *
     * @param path The path of the file to read.
     * @return The contents of a file encoded in base64.
     * @throws IOException If there is an error reading the file.
     */
    public static String readFileBytes(String path) throws IOException {
        String root = Server.path.isEmpty() ? Path.of(".").toAbsolutePath().toString() : Server.path; //path to servers content root
        path = root + path; //full path to image
        System.out.println("Reading " + path);
        FileInputStream file = new FileInputStream(new File(path)); //open file stream

        byte bytes[] = Base64.getEncoder().encode(file.readAllBytes()); //encodes bytes as base64

        file.close();
        return new String(bytes); //convert array of bytes to a string.
    }

    /**
     * Checks if the assigned file to this instance is stored on disk.
     *
     * @return True if the file s on disk, false otherwise.
     */
    protected boolean checkCache() {
        if(Files.exists(Path.of(cwd + directoryname + filename + extension))) {
            return true;
        }
        else {
            return false;
        }
    }
}
