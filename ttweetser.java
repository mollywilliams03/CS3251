import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ttweetser {

    static HashSet<String> currentUsers = new HashSet<>();

    public static HashSet<String> getUsers() {
        return currentUsers;
    }

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

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                PrintWriter writer = new PrintWriter(out, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //pull information from socket
                String line = reader.readLine();

                //logic to quit if user is already logged in
                if (currentUsers.contains(line)) {
                    writer.println("username illegal, connection refused.");
                } else {
                    currentUsers.add(line);
                    Thread newThread = new ClientHandler(socket, in, out);
                    newThread.start();
                }

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
    //final DataInputStream in;
    //final DataOutputStream out;
    final InputStream in;
    final OutputStream out;
    final Socket socket;

    public ClientHandler(Socket socket, InputStream in, OutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void run() {
        String received, toReturn;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);
        while (true) {
            try {
                writer.println("whats up");
                received = reader.readLine();
                System.out.println("check Server");
                if (received.length() > 7 && received.substring(0,7).equals("tweet \"")) {
                    String remaining = received.substring(7, received.length());
                    int endOfTweet = remaining.indexOf('"');
                    String theTweet = remaining.substring(0,endOfTweet);
                    if (theTweet.length() == 0) {
                        writer.println("message format illegal.");
                    } else if (theTweet.length() > 150) {
                        writer.println("message length illegal, connection refused.");
                    } else {
                        System.out.println(theTweet);
                    }

                } else if (received.length() > 13 && received.substring(0,13).equals("unsubscribe #")) {
                    //unsubscribe logic
                } else if (received.length() > 11 && received.substring(0,11).equals("subscribe #")) {
                    //subscribe logic
                } else if (received.equals("timeline")) {
                    //timeline logic
                } else if (received.equals("exit")) {
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    //add logic to have user info removed
                    break;
                } else if (received.equals("getusers")) {
                    HashSet<String> currUsers = ttweetser.getUsers();
                    writer.println(currUsers);
                } else if (received.equals("gettweets")) {
                    //gettweets logic
                } else {
                    //invalid request logic
                }

                break;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

