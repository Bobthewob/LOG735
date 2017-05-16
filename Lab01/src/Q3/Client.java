package Q3;

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
import java.net.SocketException;
import java.sql.Time;
import java.util.concurrent.TimeoutException;

public class Client {
    private final static String DEFAULT_SERVER_HOSTNAME = "127.0.0.1";
    private final static String SECOND_SERVER_HOSTNAME = "10.196.115.189";
    private final static int SERVER_PORT = 10118;
    private static String currentMessage = "";

    public static void main(String[] args) throws Exception {
        String serverHostname = DEFAULT_SERVER_HOSTNAME;
        String secondServerHostname = SECOND_SERVER_HOSTNAME;

        if (args.length > 0) {
            serverHostname = args[0];
        }
        try {
          connectToServer(serverHostname, "");
        }
        catch(TimeoutException|SocketException se ){
            printf(se.getMessage());
            printfln("RIPPPPPPPPPPPP");
            connectToServer(secondServerHostname, currentMessage);
        }
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void printfln(String format, Object... args) {
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        printf(format + LINE_SEPARATOR, args);
    }
    private static void connectToServer(String ipAdress, String message) throws Exception {
        printfln("Essai de se connecter \u00e0 l'h\u00f4te %s au port %d...", ipAdress, SERVER_PORT);

        Socket echoSocket;
        PrintWriter out;
        BufferedReader in;

        echoSocket = new Socket(ipAdress, SERVER_PORT);
        out = new PrintWriter(echoSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        Boolean notResponding = true;
        long beginTime = System.currentTimeMillis();
        if (message != "") {
            //printf("Entr\u00e9e: ");
            out.println(message);
            String serverResponse = in.readLine();
            if (serverResponse == null) {
                printfln("Le serveur n'est plus connect\u00e9.");
            }
            printfln("echo: %s", serverResponse);
       }
        printf("Entr\u00e9e: ");

        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            currentMessage = userInput;
            while (((System.currentTimeMillis() - beginTime) < 10000)) {
                if (in.ready()) {
                    notResponding = false;
                    break;
                }
            }
            if (notResponding) {
                throw new TimeoutException("Le serveur ne repond plus rip :(");
            }
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
}
