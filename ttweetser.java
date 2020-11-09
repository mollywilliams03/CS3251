import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ttweetser {

    static HashSet<String> currentUsers = new HashSet<>();
    static HashMap<String, ArrayList<ClientHandler>> hashtags = new HashMap<String, ArrayList<ClientHandler>>();
    static LinkedList<String>[] messages = new LinkedList[5]; //stores all the messages
    static HashSet<ClientHandler> userObjects = new HashSet<>();

    public static LinkedList<String>[] getMessages() {
        return messages;
    }

    public static void setMessages(LinkedList<String>[] mess) {
        messages = mess;
    }

    public static HashMap<String, ArrayList<ClientHandler>> getHashtags() {
        return hashtags;
    }

    public static void setHashtags(HashMap<String, ArrayList<ClientHandler>> hash) {
        hashtags = hash;
    }

    public static void removeUser(String username) {
        currentUsers.remove(username);
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
                    Thread newThread = new ClientHandler(socket, in, out, line);
                    newThread.start();
                }
                System.out.println(currentUsers);

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
    final String username;


    public ClientHandler(Socket socket, InputStream in, OutputStream out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
    }

    public void run() {
        String received, toReturn;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);
        while (true) {
            try {
                writer.println("whats up");
                //received = in.readUTF();
                received = reader.readLine();
                System.out.println("check Server");
                if (received.length() > 7 && received.substring(0,7).equals("tweet \"")) {
                    String remaining = received.substring(7, received.length());
                    int endOfTweet = remaining.indexOf('"');
                    String theTweet = remaining.substring(0,endOfTweet);
                    int first = remaining.indexOf("#");
                    String hashes = remaining.substring(first,received.length());
                    if (theTweet.length() == 0) {
                        writer.println("message format illegal.");
                    } else if (theTweet.length() > 150) {
                        writer.println("message length illegal, connection refused.");
                    } else {
                        //access hashmap of hashtags, send out to the users somehow
                        // System.out.println(theTweet);
                        LinkedList<String>[] messages = ttweetser.getMessages();
                        for (int i = 0; i <5; i++) { //goes through the messages array, finds the user's linked list and adds to it
                            if (messages[i].equals(username)) {
                                messages[i].add(theTweet);
                                break;
                            }
                        }
                        ttweetser.setMessages(messages);
                        HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags();
                        String[] hashesArr = hashes.split("#");
                        for (int i = 0; i < hashesArr.length; i++) {
                            hashtags.putIfAbsent(hashesArr[i], null); //only inserts new key if it doesnt already exist
                            
                        }
                    }

                } else if (received.length() > 13 && received.substring(0,13).equals("unsubscribe #")) {
                    //unsubscribe logic
                    //remove this user from the hashmap of hashtags
                } else if (received.length() > 11 && received.substring(0,11).equals("subscribe #")) {
                    //subscribe logic
                    //add them to the hashmap
                    //check if the hashtag exists, if it doesn't just add an entry to the hashmap, if it does then add to that entry
                } else if (received.equals("timeline")) {
                    //timeline logic
                } else if (received.equals("exit")) {
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    ttweetser.removeUser(username);
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

            //try {
            //    this.in.close();
            //    this.out.close();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}
        }
    }
}

