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
import java.net.Socket;

public class Client {
    private final static String DEFAULT_SERVER_HOSTNAME = "127.0.0.1";
    private final static int SERVER_PORT = 10118;

    public static void main(String[] args) throws IOException {
        String serverHostname = DEFAULT_SERVER_HOSTNAME;

        if (args.length > 0) {
            serverHostname = args[0];
        }
        printfln("Essai de se connecter \u00e0 l'h\u00f4te %s au port %d...", serverHostname, SERVER_PORT);

        Socket echoSocket;
        PrintWriter out;
        BufferedReader in;

        echoSocket = new Socket(serverHostname, SERVER_PORT);
        out = new PrintWriter(echoSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        printf("Entr\u00e9e: ");
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            String serverResponse = in.readLine();
            if (serverResponse == null) {
                printfln("Le serveur n'est plus connect\u00e9.");
                break;
            }
            printfln("echo: %s", serverResponse);
            printf("Entr\u00e9e: ");
        }

        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void printfln(String format, Object... args) {
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        printf(format + LINE_SEPARATOR, args);
    }
}
