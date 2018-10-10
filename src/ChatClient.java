import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;


 // JSWING GUI for chat.

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("BEST CHATROOM EVER");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
   // JScrollPane userField = new JScrollPane(); unused. Testing purpose


    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);

        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North"); //north, center, east etc, determines the placement of the jframe UI
       // frame.getContentPane().add(userField, "East");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();


        // Add Listeners
        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText("");
        });
    }


     // Sort of unused function. Only needed if the server is not localhost (which in this case, it is)

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP to chat room (localhost)",
                "Welcome to the best chatroom ever",
                JOptionPane.INFORMATION_MESSAGE);
    }


    // Ask for username, duplicate check is handled serverside

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a username name:",
                "Username selection",
                JOptionPane.PLAIN_MESSAGE);
    }


      // Server connection

    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9002);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        /*
         Handle server messages. These are commands the server listens for so it knows what to do with them.
         CHOOSENAME is what the server asks the client untill unique name is given
         NAMEOK is what is sent to server, when the name is accepted and added to the current list of names and chatters
         BROADCAST infront of a message will broadcast to all current chatters
         */

        while (true) {
            String line = in.readLine();
            if (line.startsWith("CHOOSENAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEOK")) {
                textField.setEditable(true);
                //out.println(getServerAddress());
            } else if (line.startsWith("BROADCAST")) {
                messageArea.append(line.substring(9) + "\n");

            }
        }
    }

  // the main method. Opens the JFRAME
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}