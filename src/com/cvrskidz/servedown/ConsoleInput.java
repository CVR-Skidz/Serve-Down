package com.cvrskidz.servedown;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The com.cvrskidz.servedown.ConsoleInput Class reacts to the available user commands during runtime. Executable on a new thread.
 * <p>
 * Provided commands:
 * <ul>
 * <li> "stop" - Stops all com.cvrskidz.servedown.Server instances listening on the open port by setting com.cvrskidz.servedown.Server.listening to false.
 * <p>
 * The com.cvrskidz.servedown.ConsoleInput class does not react to unspecified input.
 */
public class ConsoleInput implements Runnable {
    @Override
    public void run(){
        Scanner in = new Scanner(System.in);
        while(true) {
            String command = ""; //a buffer to store the user input
            try {
                command = in.nextLine();
            }
            catch (NoSuchElementException e) {
                System.err.println(e); //print an error if no input can be detected on enter
            }

            if (command.toString().equals("stop")) {
                in.close();
                Server.listening = false; //stop Servers
                System.exit(0); //exi program with no errors
                break;
            }
        }
    }
}
