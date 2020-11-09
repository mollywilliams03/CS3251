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

    public static HashSet<String> getUsers() {return currentUsers; }

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
                    writer.println("username legal, connection established.");
                    currentUsers.add(line);
                    Thread newThread = new ClientHandler(socket, in, out, line);
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
        // public PrintWriter getWriter() { //get writer
        //     return writer;
        // }
        while (true) {
            try {
                //writer.println("whats up");
                //received = in.readUTF();
                received = reader.readLine();
                System.out.println(received);
                if (received.length() > 7 && received.substring(0,7).equals("tweet \"")) {
                    String remaining = received.substring(7, received.length());
                    int endOfTweet = remaining.indexOf("\"");
                    String theTweet = "";
                    if (endOfTweet != 0) {
                        theTweet = remaining.substring(0, endOfTweet);
                    }
                    int first = remaining.indexOf("#");
                    String hashes = remaining.substring(first, received.length());
                    if (theTweet.length() == 0) {
                        writer.println("message format illegal.");
                    } else if (theTweet.length() > 150) {
                        writer.println("message length illegal, connection refused.");
                    } else {

                        System.out.println("test");
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
                            // ArrayList<ClientHandler> usersToSend = hashtags.get(hashesArr[i]); //gets list of users to send to
                            // if (usersToSend != null) {
                            //     for (int u = 0; u < usersToSend.size(); u++) { //loops through these users and sends to them
                            //         PrintWriter thisWriter = usersToSend.get(u).getWriter(); //gets the user's writer
                            //         thisWriter.println(theTweet);
                            //     }
                            // }
                        }
                    }

                } else if (received.length() > 13 && received.substring(0,13).equals("unsubscribe #")) {
                    //unsubscribe logic
                    //remove this user from the hashmap of hashtags
                    String unhash = received.substring(14,received.length()); //gets the hashtag
                    HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags(); //gets the hashtags
                    if (unhash.equals("ALL")) {
                        for (Map.Entry<String, ArrayList<ClientHandler>> set : hashtags.entrySet()) { //loops through the hashtags
                            ArrayList<ClientHandler> toRemoveFrom = set.getValue(); //gets the value of this particular set
                            toRemoveFrom.remove(this); //removes if its there
                        }
                    } else {
                        ArrayList<ClientHandler> toRemoveFrom = hashtags.get(unhash);
                        toRemoveFrom.remove(this);
                    }
                    ttweetser.setHashtags(hashtags); //sets with the changes made
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
                    writer.println(ttweetser.getUsers());
                } else if (received.length() > 9 && received.substring(0,9).equals("gettweets")) {
                    LinkedList<String>[] messages = ttweetser.getMessages();
                    String user = received.substring(10, received.length()); //username of the user we want the tweets of
                    LinkedList<String> usersTweets = new LinkedList<>();
                    for (int i = 0; i <5; i++) { //goes through the messages array, finds the user's linked list
                        if (messages[i].getFirst().equals(user)) {
                            usersTweets = messages[i];
                        }
                    }
                    if (usersTweets == null) {
                        writer.println("no user " + user + " in the system");
                    } else {
                        writer.println(usersTweets); //sends the linked list to the client
                    }
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

