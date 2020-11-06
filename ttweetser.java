import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ttweetser {

    public static void main(String args[]) throws Exception {
        if (args.length != 1) {
            System.out.println("error: args should contain <ServerPort>");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port); //create server socket

        while (true) {
            Socket socket = null;
            try
            {
                socket = serverSocket.accept();

                //in and out streams
                DataInputStream in = new DataInputStream(socket.getInputStream());
                //InputStream in = socket.getInputStream();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                //BufferedReader d = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //pull information from socket
                //String line = reader.readLine();
                System.out.println(in);
                //System.out.println(d.readLine());


                Thread newThread = new ClientHandler(socket, in, out);

                newThread.start();
            }
            catch (Exception e)
            {
                socket.close();
                System.exit(0);
            }


        //ArrayList<String> messages = new ArrayList<>();
        }
    }
}

class ClientHandler extends Thread {
    final DataInputStream in;
    final DataOutputStream out;
    final Socket socket;

    public ClientHandler(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void run() {
        String received, toReturn;
        while (true) {
            try {
                out.writeUTF("Whats up");
                received = in.readUTF();
                System.out.println("check Server");
                if (received.length() > 7 && received.substring(0,7).equals("tweet \"")) {
                    String theTweet = received.substring(7, received.length());
                    //tweet logic
                } else if (received.length() > 13 && received.substring(0,13).equals("unsubscribe #")) {
                    //unsubscribe logic
                } else if (received.length() > 11 && received.substring(0,11).equals("subscribe #")) {
                    //subscribe logic
                } else if (received.equals("timeline")) {
                    //timeline logic
                } else if (received.equals("exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    //add logic to have user info removed
                    break;
                } else if (received.equals("getusers")) {
                    //getusers logic
                } else if (received.equals("gettweets")) {
                    //gettweets logic
                } else {
                    //invalid request logic
                }

                break;

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                this.in.close();
                this.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

