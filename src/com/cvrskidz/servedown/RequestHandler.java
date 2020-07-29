package com.cvrskidz.servedown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler extends Thread{
    private Socket client;
    private boolean block;

    public RequestHandler(Socket client) {
        this.client = client;
    }

    public void run(){
        String path = ""; //requested file

        try {
            BufferedReader req = new BufferedReader(new InputStreamReader(client.getInputStream())); //the client request
            String reqBuffer = req.readLine(); //a buffer to store tokens from the request stream

            System.out.println("\nREQUEST: " + reqBuffer);
            System.out.println("Client: " + client.getRemoteSocketAddress());
            if(Server.isValidRequest(reqBuffer)) {
                path = Server.extractPath(reqBuffer);
            }
            else {
                block = true;
            }
        }
        catch (IOException e) {
            System.out.println("An error occurred reading the request: " + e);
        }

        try {
            PrintWriter res = new PrintWriter(client.getOutputStream()); //open a stream to write a web response to

            if(!block) {
                FileHandler requestedContent = FileHandler.newHandler(path); //read and/or compile requested file
                res.println(requestedContent.getResponse()); //write response
            }
            else {
                res.println(new HTTPResponse("text/html", "NOT FOUND", 404, HTTPResponse.PROTOCOL.HTTP));
                System.out.println("Responded 404");
            }

            //cleanup
            res.flush();
            res.close();
            client.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred sending a response: " + e);
        }
    }
}
