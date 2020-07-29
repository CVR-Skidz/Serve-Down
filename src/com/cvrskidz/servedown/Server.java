package com.cvrskidz.servedown;

import java.net.*;
import java.io.*;
import java.time.*;

/**
 * The entry point for the application responsible for listening to traffic on a
 * local port and creating a file handler for the request. This class is also
 * responsible for sending back a response to he request based on it's result.
 */
public class Server {
    // CLI argument storage
    public static boolean compileFlag = false; //set to true upon --compile being sent
    public static String path = ""; //set to the content path supplied to the program if any

    // Program loop variables
    public static boolean listening;
    public static final String defaultFile = "README.md"; //default file to read if none specified

    //com.cvrskidz.servedown.Server status
    private ServerSocket socket; //open socket
    private String error; //any error messages to display
    private int port; //port to listen on
    private Thread inputThread; //Thread to handle user input whilst running

    public static final String REQUEST_METHOD = "GET"; //supported methods
    public static final String[] REQUEST_PROTOCOLS = new String[] {"HTTP", "HTTPS"}; //supported protocols

    /**
     * Returns a new instance of a file server, ready to listen on the supplied port
     *
     * @param port The port number on the local host to open.
     */
    public Server(int port) {
        this.port = port;
        inputThread = new Thread(new ConsoleInput()); //assign input controller to a new thread
    }

    /**
     * Starts the server object, continuously polling the designated port and handling all
     * requests it receives until interrupted
     *
     * @throws java.net.UnknownHostException If a socket cannot be created on the local machine
     * @throws java.io.IOException If an error occurs sending a response to a client or the open socket
     */
    public void listen() throws java.net.UnknownHostException, java.io.IOException {
        System.out.println("Connecting to socket at: " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        socket = new ServerSocket(port, 0, InetAddress.getLocalHost()); //create socket to receive requests
        listening = true; //whilst true the server will listen to traffic on the instance port

        inputThread.start();

        // Accept requests until stopped
        while(listening) {
            new RequestHandler(socket.accept()).start();
        }
    }

    /**
     * Confirms the request conforms to the supported protocols.
     *
     * @param str The client request
     * @return True if the request can be processed, else False
     */
    public static boolean isValidRequest(String str) {
        if (str.contains(REQUEST_METHOD)) {
            for(String protocol : REQUEST_PROTOCOLS) {
                if(str.contains(protocol)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extracts the file path from a HTTP(s) request
     *
     * @param str The HTTP(s) request
     * @return The file path relative to the severs content path
     */
    public static String extractPath(String str) {
        int end = 0;
        int start = str.indexOf(REQUEST_METHOD) + REQUEST_METHOD.length();

        for(String p : REQUEST_PROTOCOLS) {
            if(str.contains(p)) {
                end = str.indexOf(p);
            }
        }

        return str.substring(start, end).strip();
    }

    /**
     * Runs a single server on port 80 with the supplied server arguments until stopped.
     *
     * @param args The program arguments.
     * @throws Exception Dump all unhandled errors.
     */
    public static void main(String args[]) throws Exception {
        for (int i = 0; i < args.length; ++i) {
            if(args[i].equals("--compile")) {
                Server.compileFlag = true;
            }
            if(args[i].equals("--path")) {
                File pathBuffer = new File(args[++i]);
                if(pathBuffer.isDirectory()) {
                    Server.path = pathBuffer.getCanonicalPath();
                    System.out.println("Path set to " + Server.path);
                }
                System.out.println("Path arg was " + pathBuffer.getPath());
            }
        }

        Server server = new Server(80);

        try {
            server.listen();
        }
        catch (Exception e) {
            System.err.println("An error occurred: " + LocalDateTime.now().toString());
            System.err.println(e);
        }
    }
}
