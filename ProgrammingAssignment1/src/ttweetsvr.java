import java.io.*;
import java.net.*;

public class ttweetsvr {
    /*
    Lines 14, 20-24 were based off the website https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip
     */
    public static void main(String args[]) throws Exception{
        if (args.length != 1) {
            System.out.println("Wrong number of arguments");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port); //create server socket

        String store = "Empty Message"; //before a message has been saved
        String[] storage = new String[150];

        while (true) {
            Socket connectionSocket = serverSocket.accept(); //create socket
            InputStream in = connectionSocket.getInputStream();  //how the information comes in from the client
            BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //pull information from socket
            OutputStream output = connectionSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            String line;
            line = reader.readLine(); //see what information was pulled
            if (line.equals("-d")) { //download
                writer.println(store); //message to be returned to the client
            } else { //upload
                store = line; //update the current saved message to the new one.
                writer.println("message upload successful"); //message to be returned to the client
            }

            connectionSocket.close();
        }
    }
}