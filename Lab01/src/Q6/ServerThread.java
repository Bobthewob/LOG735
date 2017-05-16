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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThread extends Thread {
    //Code des notes de cours
    ServerSocket server;
    Socket client;
    AtomicInteger atmIn;
    AtomicBoolean noClient;

    public ServerThread(Socket _client, AtomicInteger _atmInt, AtomicBoolean _noClient)
    {
        client = _client;
        atmIn = _atmInt;
        noClient = _noClient;
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

                if(noClient.get()){
                    int number = Integer.parseInt(returnText.split(" - ")[0].substring(1));
                    atmIn.set(number);
                    noClient.set(false);
                }

                System.out.println("Entrée: " + "#" + atmIn.incrementAndGet() + " - " + returnText.split(" - ")[1]);
                //in.println("Entrée: " + returnText);
                out.println("#" + atmIn.get() + " - " + returnText.split(" - ")[1].toUpperCase());
                out.flush();
                //System.out.println("on a flush");
            }
            //Fermer la connexion
            client.close();

        } catch (Exception e) { System.out.println(e); }
    }
}
