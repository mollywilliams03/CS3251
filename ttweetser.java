import java.io.*;
import java.net.*;

public class ttweetser {

    public static void main(String args[]) throws Exception{
        if (args.length != 1) {
            System.out.println("error: args should contain <ServerPort>");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port); //create server socket

        ArrayList<String> messages = new ArrayList<>();
    }