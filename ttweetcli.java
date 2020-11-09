import java.io.*;
import java.net.*;
import java.util.*;

public class ttweetcli {

    public static void main(String args[]) throws Exception{
        if (args.length != 3) { //invalid number of args
            System.out.println("error: args should contain <ServerIP> <ServerPort> <Username>");
            System.exit(0);
        }

        String serverIP = args[0];
        int serverPort =  Integer.parseInt(args[1]);
        String username = args[2];
        //logic to check if the username is valid
        for (int i = 0; i < username.length(); i++) {
            char curr = username.charAt(i);
            if (!Character.isLetter(curr) && !(username.charAt(i) >= '0' && username.charAt(i) <= '9')) {
                System.out.println("error: username has wrong format, connection refused.");
                System.exit(0);
            }
        }
        try
            {
                Scanner scanner = new Scanner(System.in);
                Socket socket = new Socket(serverIP, serverPort);

                OutputStream out = socket.getOutputStream();

                PrintWriter writer = new PrintWriter(out, true);
                writer.println(username);

                InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line1 = reader.readLine();
                System.out.println(line1);
                if (line1.equals("username illegal, connection refused.")) {
                    //System.out.println("username illegal, connection refused.");
                    System.exit(0);
                }

                while (true) {
                    String tosend = scanner.nextLine(); //gets the input from terminal

                    writer.println(tosend); //sends input to server

                    String line = reader.readLine(); //reads the stuff from the server

                    if (line != null) {
                        if (!line.equals("null")) {
                            System.out.println(line);
                        }
                    }           
                    if (tosend.equals("exit")) {
                        System.out.println("bye bye");
                        socket.close();
                        System.out.println("Connection closed");
                        break;
                    }
                }
                scanner.close();
                //in.close();
                //out.close();
            } catch (UnknownHostException ex) {

                System.out.println("error: server ip invalid, connection refused.");
                System.exit(0);

            } catch (IOException ex) { //check for valid serverport

               System.out.println("Server not found");
                System.exit(0);
           }
    }
}
