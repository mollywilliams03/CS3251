import java.io.*;
import java.net.*;

public class ttweetcl {
    /*
    Lines 27-29, 33-34, 76-79, 80-81 were based off the website https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip
    The 2 catch exception blocks were also based off the same website.
     */
    public static void main(String args[]) throws Exception{
        if (args.length < 3 || args.length > 4) { //invalid number of args
            System.out.println("Wrong number of arguments");
            System.exit(0);
        }
        String mode = args[0];
        if (!mode.equals("-d") && !mode.equals("-u")) { // if they try and enter something other than upload or download
            System.out.println("Please specify upload or download mode");
            System.exit(0);
        }
        String serverAddr = args[1];
        int serverPort = Integer.parseInt(args[2]);

        //Download mode
        if (mode.equals("-d")) {
            if (args.length == 4) { //upload mode number of args
                System.out.println("You should only have 3 arguments for download mode");
                System.exit(0);
            }
            try (Socket socket = new Socket(serverAddr, serverPort)) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(mode); //send information to the server


                InputStream input = socket.getInputStream(); //get information back from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(input)); //get information from the server
                String line;
                line = reader.readLine(); //readable information
                //InputStreamReader reader = new InputStreamReader(input);
                /*int character;
                StringBuilder data = new StringBuilder();

                while ((character = reader.read()) != -1) {
                    data.append((char) character);
                }*/
                System.out.println(line); //print information retrieved from the server
                socket.close();
            } catch (UnknownHostException ex) {

                System.out.println("Server not found");
                System.exit(0);

            } catch (IOException ex) {

                System.out.println("Server not found");
                System.exit(0);
            }

        }




        //Upload Mode
        if (mode.equals("-u")) {
            if (args.length == 3) { //if given download number of args
                System.out.println("You should have 4 arguments for upload mode");
                System.exit(0);
            }
            String message = args[3];
            if (message.getBytes().length > 150) { //exit client if the message is over 150 characters
                System.out.println("Max number of characters is 150. You had " + message.getBytes().length);
                System.exit(0);
            }
            //send empty message


            try (Socket socket = new Socket(serverAddr, serverPort)) { //create new socket
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(message); //send message to the server
                InputStream input = socket.getInputStream(); //get information back from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                System.out.println(reader.readLine()); //print message sent from server
                socket.close();
            } catch (UnknownHostException ex) {

                System.out.println("Server not found");
                System.exit(0);

            } catch (IOException ex) {

                System.out.println("Server not found");
                System.exit(0);
            }

        }

    }
}
