package Q2;

/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #1
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final static int SERVER_PORT = 10118;

    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(SERVER_PORT);

        printfln("Le server est en marche et en attente de la connexion au port %d ...", SERVER_PORT);

        String inputLine;

        try {

            //Mettre le socket à l’écoute de demande de connexion avec la méthode bloquante accept():
            Socket clientSocket;
            while ((clientSocket = server.accept()) != null) {
                printfln("Connexion r\u00E9ussie.");
                printfln("Attente de l'entr\u00e9e...");
                ServerThread serverThread = new ServerThread(clientSocket);
                serverThread.start();
            }
        } catch (Exception e) { System.out.println(e); }
        finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //clientSocket.close();
        server.close();
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void printfln(String format, Object... args) {
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        printf(format + LINE_SEPARATOR, args);
    }

}
