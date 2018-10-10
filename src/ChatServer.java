import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.*;
import java.util.HashSet;


public class ChatServer {


    //port number 9002
    private static final int PORT = 9002;
    private static final String ip = "localhost";

   //set of usernames so we can check duplicates and post current chatters
    private static HashSet<String> names = new HashSet<>();

    //holds messages so they can be sent to all current chatters
    private static HashSet<PrintWriter> writers = new HashSet<>();


    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
                for (PrintWriter writer : writers) {
                    writer.println("BROADCAST New user connected from port " +PORT+ " and IP " +ip);
                  //  writer.println("BROADCAST [SERVER MESSAGE] - Current users online: "+ names); //broadcast current users whenever a new one connects
                }
            }
        } finally {
            listener.close();
        }
    }


    private static class Handler extends Thread {
        private String name;
      //  private String command;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;


        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                socket.setKeepAlive(true); //heartbeat. Sends ping every 2 hours

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.
                while (true) {
                    out.println("CHOOSENAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                           out.println(names);
                          // System.out.println(names);
                            break;

                        }
                    }
                }



                // use of NAMEOK. The name is ok and is added to the pool of chatters and is available for broadcasting
                out.println("NAMEOK");
             //   System.out.println(names);
                writers.add(out);
                System.out.println("Currently online on chat: "+names); //display names to console





                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null ) {
                       // System.out.println("user DC");
                        break;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("BROADCAST [" + name.toUpperCase() + "]: " + input);

                        if(input.equals("!bye")){ //handles quit message
                            writer.println("BROADCAST " +name.toUpperCase() + " Disconnected");
                            System.out.println("user DC");

                        }else if (input.equals("!users")){ //handles current user lookup
                            writer.println("BROADCAST Current online users: "+names);
                        }



                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {

                //remove client name and close socket

                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);

                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}