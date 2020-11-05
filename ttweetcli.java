import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ttweetcli {

    public static void main(String args[]) throws Exception{
        if (args.length != 3) { //invalid number of args
            System.out.println("error: args should contain <ServerIP> <ServerPort> <Username>");
            System.exit(0);
        }

        String serverIP = args[0];
        int serverPort =  Integer.parseInt(args[1]);
        String username = args[2];
        try //(Socket socket = new Socket(serverAddr, serverPort)) {
            {
                Scanner scanner = new Scanner(System.in);
                Socket socket = new Socket(serverIP, serverPort);

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    System.out.println(in.readUTF());
                    String tosend = scanner.nextLine();
                    out.writeUTF(tosend);
                    System.out.println("check Client");
                    //socket.close();
                    if (tosend.equals("exit")) {
                        System.out.println("Closing this connection : " + socket);
                        socket.close();
                        System.out.println("Connection closed");
                        break;
                    }
                    //break;


                }
                scanner.close();
                in.close();
                out.close();
            } catch (UnknownHostException ex) {

                System.out.println("error: server ip invalid, connection refused.");
                System.exit(0);

            } catch (IOException ex) { //check for valid serverport

               System.out.println("Server not found");
                System.exit(0);
           }
    }
}
