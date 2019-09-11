/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detection;

/**
 *
 * @author mirismr
 */
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

public class TCPClient {

    private Socket clientSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

    public TCPClient(String server, int port) {
        try {
            this.clientSocket = new Socket(server, port);

            this.outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.inFromServer = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeConnection() {
        try {
            JSONObject object = new JSONObject();
            object.put("option", "C");
            String info = this.sendPetition(object.toJSONString() + "\n");
            this.clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String sendPetition(String data) {
        
        String toServer = data;
        outToServer.println(toServer);
        outToServer.flush();

        String fromServer=null;
        try {
            fromServer = inFromServer.readLine();
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fromServer;
    }

}
