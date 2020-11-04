import java.io.*;
import java.net.*;

public class ttweetcli {

    public static void main(String args[]) throws Exception{
        if (args.length != 3) { //invalid number of args
            System.out.println("error: args should contain <ServerIP> <ServerPort> <Username>");
            System.exit(0);
        }

        String serverIP = args[0];
        int serverPort =  Integer.parseInt(args[1]);
        String username = args[2];
        try (Socket socket = new Socket(serverAddr, serverPort)) {

                socket.close();
            } catch (UnknownHostException ex) {

                System.out.println("error: server ip invalid, connection refused.");
                System.exit(0);

            } //catch (IOException ex) { check for valid serverport

               // System.out.println("Server not found");
                //System.exit(0);
           // }