package Q6;

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

public class ServerCountThread extends Thread {
    //Code des notes de cours
    ServerSocket server;
    Socket client;
    AtomicInteger atmIn;
    PrintWriter outServer;

    public ServerCountThread(Socket _client, AtomicInteger _atmInt)
    {
        client = _client;
        atmIn = _atmInt;
    }

    public void run() {
        //Créer le socket du server lié à un port donné:
        try {
            //Échanger des données avec le client à travers le socket	connexionSocket:
            //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String returnText;
            while ((returnText = in.readLine()) != null) {

                System.out.println("requete numero : " + Integer.parseInt(returnText));
                atmIn.set(Integer.parseInt(returnText));
            }
            //Fermer la connexion
            client.close();

        } catch (Exception e) { System.out.println(e); }
    }
}
