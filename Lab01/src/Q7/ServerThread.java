package Q7;

/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #1
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThread extends Thread {
    //Code des notes de cours
    ServerSocket server;
    Socket client;
    AtomicInteger atmIn;
    PrintWriter outServer;

    public ServerThread(Socket _client, AtomicInteger _atmInt)
    {
        client = _client;
        atmIn = _atmInt;
    }

    public ServerThread(Socket _client, AtomicInteger _atmInt, PrintWriter _outServer)
    {
        client = _client;
        atmIn = _atmInt;
        outServer = _outServer;
    }

    public void run() {
        //Créer le socket du server lié à un port donné:
        try {
            //Échanger des données avec le client à travers le socket	connexionSocket:
            //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String returnText;
            while ((returnText = in.readLine()) != null) {

                //in.println("Entrée: " + returnText);
                out.println("#" + atmIn.incrementAndGet() + " - " + returnText.split(" - ")[1].toUpperCase());
                System.out.println("Entrée: " + "#" + atmIn.get() + " - " + returnText.split(" - ")[1]);
                out.flush();

                if(outServer != null){
                    outServer.println(atmIn.get());
                }
            }
            //Fermer la connexion
            client.close();

        } catch (Exception e) { System.out.println(e); }
    }
}
