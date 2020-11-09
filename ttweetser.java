import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ttweetser {

    static HashSet<String> currentUsers = new HashSet<>();
    static HashMap<String, ArrayList<ClientHandler>> hashtags = new HashMap<String, ArrayList<ClientHandler>>(); //maps hashtags to people subscribed to them
    static LinkedList<String>[] messages = new LinkedList[5]; //stores all the messages
    static HashMap<String, ArrayList<String>> usersToSub = new HashMap<>(); //maps users to their subscriptions

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

    public static HashMap<String, ArrayList<String>> getUsersToSub() {
        return usersToSub;
    }

    public static void setUsersToSub(HashMap<String, ArrayList<String>> subs) { usersToSub = subs; }

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
                HashMap<String, ArrayList<String>> usersMap = ttweetser.getUsersToSub(); //gets the users mapped with their subs
                usersMap.putIfAbsent(username, new ArrayList<String>());
                ttweetser.setUsersToSub(usersMap);
                if (received.length() > 7 && received.substring(0,7).equals("tweet \"")) {
                    String remaining = received.substring(7, received.length());
                    int endOfTweet = remaining.indexOf("\"");
                    String theTweet = "";
                    if (endOfTweet != 0) {
                        theTweet = remaining.substring(0, endOfTweet);
                    }
                    int first = remaining.indexOf("#");
                    String hashes = remaining.substring(first, remaining.length());
                    if (theTweet.length() == 0) {
                        writer.println("message format illegal.");
                    } else if (theTweet.length() > 150) {
                        writer.println("message length illegal, connection refused.");
                    } else {
                        //access hashmap of hashtags, send out to the users somehow
                        LinkedList<String>[] messages = ttweetser.getMessages(); //gets the messages
                        if (messages != null) {
                            boolean found = false;
                            int foundHere = 0;
                            int count = 0;
                            while (messages[count] != null) { //loops through existing entries
                                if (messages[count].peekFirst().equals(username)) { //if the username exists
                                    messages[count].add(received.substring(6, received.length()));
                                    found = true;
                                    foundHere = count; //where it was found
                                    break;
                                }
                                count++;
                            }
                            if (found == false) { //if user does not exist
                                if (foundHere < 4) {
                                    messages[foundHere + 1] = new LinkedList<String>(); //creates new linkedlist
                                    messages[foundHere + 1].add(this.username); //adds username first thing
                                    messages[foundHere + 1].add(received.substring(6, received.length())); ////adds the message to the correct user's linked list
                                }
                            }
                        } else { //if no message array has been created
                            messages[0] = new LinkedList<String>(); //creates new linkedlist
                            messages[0].add(this.username); //adds username first thing
                            messages[0].add(received.substring(6, received.length())); //adds the linkedlist to the first entry in the array
                        }
                        ttweetser.setMessages(messages); //updates the messages
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
                    String unhash = received.substring(13,received.length()); //gets the hashtag
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
                    ArrayList<String> values = usersToSub.get(username); //current users current subscriptions
                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i).equals(unhash)) {
                            values.remove(i);
                        }
                    }

                    ttweetser.setHashtags(hashtags); //sets with the changes made
                } else if (received.length() > 11 && received.substring(0,11).equals("subscribe #")) {
                    //subscribe logic
                    //add them to the hashmap
                    //check if the hashtag exists, if it doesn't just add an entry to the hashmap, if it does then add to that entry
                    String sub = received.substring(11,received.length()); //gets the hashtag
                    HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags(); //gets the hashtags
                    HashMap<String, ArrayList<String>> usersToSub = ttweetser.getUsersToSub(); //gets the users mapped with their subs
                    ArrayList<String> values = usersToSub.get(username); //current users current subscriptions
                    if (values.size() < 3) {
                        if (sub.equals("ALL")) {
                            //subscribe to all
                            for (Map.Entry<String, ArrayList<ClientHandler>> set : hashtags.entrySet()) { //loops through the hashtags
                                ArrayList<ClientHandler> toAddTo = set.getValue(); //gets the value of this particular set
                                toAddTo.add(this); //add if its there
                            }
                        } else {
                            if (hashtags.containsKey(sub)) { //if its already in there
                                ArrayList<ClientHandler> toAddTo = hashtags.get(sub); //gets hashtag's arraylist
                                toAddTo.add(this); //adds the user to the arraylist
                            } else {
                                ArrayList<ClientHandler> newHash = new ArrayList<ClientHandler>(); //creates new arraylist
                                newHash.add(this); //adds this user to the arraylist
                                hashtags.put(sub, newHash); //adds the hashtag with this user subscribed to it
                            }
                        }
                        int check = 0; //variable to check if already subscribed
                        for (int i = 0; i < values.size(); i++) {
                            if (values.get(i).equals(sub)) {
                                check = 1;
                            }
                        }
                        if (check == 0) { //not subscribed already, need to add to values
                            values.add(sub);
                            usersToSub.replace(username, values);
                        }
                        check = 0; //reset check
                        ttweetser.setUsersToSub(usersToSub);
                    } else {
                        writer.println("sub " + sub + " failed, already exists or exceeds 3 limitation");
                    }
                    ttweetser.setHashtags(hashtags); //sets with the changes made
                } else if (received.equals("timeline")) {
                    //timeline logic
                } else if (received.equals("exit")) {
                    HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags(); //gets the hashtags
                    HashMap<String, ArrayList<String>> usersToSub = ttweetser.getUsersToSub(); //gets the users mapped with their subs
                    ArrayList<String> values = usersToSub.get(username); //current users current subscriptions
                    for (int i = 0; i < values.size(); i++) { //remove the users from the hashtag subscriptions
                        ArrayList<ClientHandler> hashTagUsers = hashtags.get(values.get(i));
                        hashTagUsers.remove(this);
                        hashtags.replace(values.get(i), hashTagUsers);
                    }

                    this.socket.close();
                    ttweetser.removeUser(username);
                    usersToSub.remove(username);
                    ttweetser.setUsersToSub(usersToSub);
                    ttweetser.setHashtags(hashtags);
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

