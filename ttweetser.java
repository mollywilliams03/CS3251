import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ttweetser {

    static HashSet<String> currentUsers = new HashSet<>();
    static HashMap<String, ArrayList<ClientHandler>> hashtags = new HashMap<String, ArrayList<ClientHandler>>(); //maps hashtags to people subscribed to them
    static ArrayList<ArrayList<String>> messages = new ArrayList<>(Arrays.asList(null, null, null, null, null)); //stores all the messages
    static HashMap<String, ArrayList<String>> usersToSub = new HashMap<>(); //maps users to their subscriptions
    static ArrayList<ArrayList<String>> timelines = new ArrayList<ArrayList<String>>(Arrays.asList(null, null, null, null, null)); //fist element in the arraylist is the user the list belongs to, the next ones are the message

    public static ArrayList<ArrayList<String>> getMessages() {
        return messages;
    }

    public static void setMessages(ArrayList<ArrayList<String>> mess) {
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

    public static ArrayList<ArrayList<String>> getTimelines() {
        return timelines;
    }


    public static void setTimelines(ArrayList<ArrayList<String>> tl) {
        timelines = tl;
    }

        public static void broadcast(ArrayList<ClientHandler> users, String message, String sendingUser) {
        // send message to all connected users
        //ArrayList<ClientHandler> list = hashtags.get(hashtagToSend); //gets the list of users subscribed to that hashtag
        //System.out.println(list);
       // if (list != null) {
            for (ClientHandler c : users) {
                c.sendMessage(message);
                //add to the timeline data structure
                boolean found = false;
                int firstNull = 0;
                boolean set = false;
                if (timelines != null) { //if it has been created
                    for (int d = 0; d < timelines.size(); d++) {
                        if (timelines.get(d) != null) {
                            if (timelines.get(d).get(0).equals(c.username)) { //if it is already in the timeline
                                timelines.get(d).add(message); //add this to the correct arraylist
                                found = true;
                            }
                        }
                        if ((timelines.get(d) == null) && (set == false)) {
                            firstNull = d;
                            set = true;
                        }
                    }
                    if (found == false) { //if it was never found
                        ArrayList<String> toAdd = new ArrayList<String>();
                        toAdd.add(c.username); //add the username first thing
                        String together = message; //makes the string
                        toAdd.add(together); //add the message
                        timelines.set(firstNull, toAdd);
                    }
                } else {
                    ArrayList<String> toAdd = new ArrayList<String>();
                    toAdd.add(c.username); //add the username first thing
                    String together = message; //makes the string
                    toAdd.add(together); //add the message
                    timelines.set(0, toAdd);
                }
            }
        //}
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
    PrintWriter writer;


    public ClientHandler(Socket socket, InputStream in, OutputStream out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
        writer = new PrintWriter(out, true);
    }



    public void sendMessage(String  msg)  {
        writer.println(msg);
    }

    public void run() {
        String received, toReturn;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        //PrintWriter writer = new PrintWriter(out, true);
        // public PrintWriter getWriter() { //get writer
        //     return writer;
        // }
        while (true) {
            try {
                //writer.println("whats up");
                //received = in.readUTF();
                //System.out.println(this.socket);
                received = reader.readLine();
                //System.out.println(received);
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
                    remaining = remaining.substring(endOfTweet, remaining.length());
                    int first = remaining.indexOf("#");
                    String hashes = remaining.substring(first, remaining.length());
                    if (theTweet.length() == 0) {
                        writer.println("message format illegal.");
                    } else if (theTweet.length() > 150) {
                        writer.println("message length illegal, connection refused.");
                    } else {
                        //access hashmap of hashtags, send out to the users somehow
                        ArrayList<ArrayList<String>> messages = ttweetser.getMessages(); //gets the messages
                        //if (messages != null) {
                        boolean found = false; //if user is found already existing
                        int firstNull = -1; //where the first null entry is if the user doesnt exist
                        boolean set = false; //if the firstnull variable has been set yet or not
                        for (int h = 0; h < 5; h++) {
                            if (messages.get(h) != null) {
                                if (messages.get(h).get(0).equals(username)) { //if the username exists
                                    messages.get(h).add(received.substring(6, received.length()));
                                    found = true;
                                    break;
                                }
                            }
                            if ((messages.get(h) == null) && (set == false)) { //gets the first null entry
                                firstNull = h; //where the null entry is
                                set = true; //the firstnull variable has been set
                            }
                        }
                        if ((found == false) && (set == true)) { //if user does not exist, and there is a null spot in the array
                            messages.set(firstNull, new ArrayList<String>()); //creates new arraylist
                            messages.get(firstNull).add(this.username); //adds username first thing
                            messages.get(firstNull).add(received.substring(6, received.length())); ////adds the message to the correct user's
                        }
                        ttweetser.setMessages(messages); //updates the messages
                        HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags();
                        String[] hashesArr = hashes.split("#");
                        ArrayList<ClientHandler> users = new ArrayList<>();
                        String mess = this.username + ": " + received.substring(6, received.length());
                        for (String hash: hashesArr) {
                                //String mess = this.username + ": " + received.substring(6, received.length());

                                ArrayList<ClientHandler> thisHash = hashtags.get(hash);
                                if (thisHash != null) {
                                    for (int i = 0; i < thisHash.size(); i++) {
                                        if (!users.contains(thisHash.get(i))) {
                                            users.add(thisHash.get(i));
                                        }
                                    }
                                }
                                //ttweetser.broadcast(hash, received.substring(6, received.length()), this.username);
                                //ttweetser.broadcast(users, mess, this.username);
                        }
                        ttweetser.broadcast(users, mess, this.username);


                        for (int i = 0; i < hashesArr.length; i++) {
                            hashtags.putIfAbsent(hashesArr[i], new ArrayList<ClientHandler>()); //only inserts new key if it doesnt already exists
                        }
                        ttweetser.setMessages(messages); //updates the messages
                        writer.println("null");
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
                    HashMap<String, ArrayList<String>> usersToSub = ttweetser.getUsersToSub(); //gets the users mapped with their subs
                    ArrayList<String> values = usersToSub.get(username); //current users current subscriptions
                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i).equals(unhash)) {
                            values.remove(i);
                        }
                    }

                    ttweetser.setHashtags(hashtags); //sets with the changes made
                    writer.println("operation success");
                } else if (received.length() > 11 && received.substring(0,11).equals("subscribe #")) {
//                    subscribe logic
//                    add them to the hashmap
//                    check if the hashtag exists, if it doesn't just add an entry to the hashmap, if it does then add to that entry
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
                                //System.out.println(toAddTo);
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
                        //System.out.println(usersToSub);
                        //System.out.println(hashtags);
                        writer.println("operation success");
                    } else {
                        writer.println("sub " + sub + " failed, already exists or exceeds 3 limitation");
                    }
                    ttweetser.setHashtags(hashtags); //sets with the changes made
                } else if (received.equals("timeline")) {
                    //timeline logic
                    //loop through the timelines array
                    ArrayList<ArrayList<String>> timelines = ttweetser.getTimelines();
                    boolean found = false;
                    if (timelines != null) {
                        for (int i = 0; i < timelines.size(); i++) {
                            ArrayList<String> toPrint = timelines.get(i);
                            HashSet<String> usersNow = ttweetser.getUsers();
                            // System.out.println(toPrint);
                            if ((toPrint != null) && (usersNow != null)) { //this entry exists, and users now is not null
                                if (toPrint.get(0).equals(this.username)) { //if it equals the username
                                    String toSend = ""; //creates the string to send
                                    for (int p = 1; p < toPrint.size(); p++) {
                                        //have to check username which is delineated by the :
                                        String[] check = toPrint.get(p).split(":");
                                        if (usersNow.contains(check[0])) {
                                            writer.println(toPrint.get(p)); //adds the arraylist entry to the string
                                        }
                                    }
                                    found = true;
                                }
                            }
                        }
                        if (found == false) {
                            writer.println("You haven't been able to recieve posts yet!");
                        }
                    } else {
                        writer.println("No posts yet!");
                    }
                } else if (received.equals("exit")) {
                    HashMap<String, ArrayList<ClientHandler>> hashtags = ttweetser.getHashtags(); //gets the hashtags
                    HashMap<String, ArrayList<String>> usersToSub = ttweetser.getUsersToSub(); //gets the users mapped with their subs
                    ArrayList<String> values = usersToSub.get(username); //current users current subscriptions
                    for (int i = 0; i < values.size(); i++) { //remove the users from the hashtag subscriptions
                        ArrayList<ClientHandler> hashTagUsers = hashtags.get(values.get(i));
                        hashTagUsers.remove(this);
                        hashtags.replace(values.get(i), hashTagUsers);
                    }
                    ArrayList<ArrayList<String>> messages = ttweetser.getMessages();
                    if (messages != null) {
                        for (int i = 0; i < messages.size(); i++) { //remove user and their messages
                            if ((messages.get(i) != null) && (messages.get(i).get(0).equals(username))) {
                                messages.set(i, null);
                            }
                        }
                    }
                    ttweetser.setMessages(messages); //updates the messages

                    ArrayList<ArrayList<String>> timelines = ttweetser.getTimelines();
                    if (timelines != null) {
                        for (int i = 0; i < timelines.size(); i++) { //remove user and their messages
                            if ((timelines.get(i) != null) && (timelines.get(i).get(0).equals(username))) {
                                timelines.set(i, null);
                            }
                        }
                    }
                    ttweetser.setTimelines(timelines); //updates the messages

                    this.socket.close();
                    ttweetser.removeUser(username);
                    usersToSub.remove(username);
                    ttweetser.setUsersToSub(usersToSub);
                    ttweetser.setHashtags(hashtags);
                    //add logic to have user info removed
                    break;
                } else if (received.equals("getusers")) {
                    HashSet<String> currentUsers = ttweetser.getUsers();
                    for (String temp :currentUsers) {
                        writer.println(temp);
                    }
                    //writer.println(ttweetser.getUsers());
                } else if (received.length() > 9 && received.substring(0,9).equals("gettweets")) {
                    ArrayList<ArrayList<String>> messages = ttweetser.getMessages();
                    String user = received.substring(10, received.length()); //username of the user we want the tweets of
                    ArrayList<String> usersTweets = new ArrayList<>();
                    for (int i = 0; i < 5; i++) { //goes through the messages array, finds the user's linked list
                        if ((messages.get(i)!= null) && messages.get(i).get(0).equals(user)) {
                            usersTweets = messages.get(i);
                        }
                    }
                    HashSet<String> currentUsers = ttweetser.getUsers();
                    if (!currentUsers.contains(user)) {
                        writer.println("no user " + user + " in the system");
                    } else {
                        for (int i = 1; i < usersTweets.size(); i++) {
                            writer.println(user + ": " + usersTweets.get(i));
                        }
                        //writer.println(usersTweets); //sends the linked list to the client
                    }
                } else {
                    writer.println("invalid request");
                }



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

