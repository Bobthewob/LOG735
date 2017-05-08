package Q1;

/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #1
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final static int SERVER_PORT = 10118;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        printfln("Le serveur est en marche et en attente de la connexion au port %d ...", SERVER_PORT);
        Socket clientSocket = serverSocket.accept();

        printfln("Connexion r\u00E9ussie.");
        printfln("Attente de l'entr\u00e9e...");

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            printfln("Serveur: %s", inputLine);
            inputLine = inputLine.toUpperCase();
            out.println(inputLine);
            if (inputLine.equals("Bye.")) {
                break;
            }
        }

        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void printfln(String format, Object... args) {
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        printf(format + LINE_SEPARATOR, args);
    }

}
