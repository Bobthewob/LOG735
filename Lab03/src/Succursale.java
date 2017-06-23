/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static java.lang.System.exit;

public class Succursale extends UnicastRemoteObject implements Serializable,ISuccursale {

    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private int id;
    private AtomicInteger montant = new AtomicInteger();
    private String adresseIP;
    private boolean print = true;
    public HashMap<Integer,ISuccursale> listeSuccursale = new HashMap<Integer,ISuccursale>();

    public Succursale(int montant,String adresseIP) throws RemoteException{
        super(0);
        this.montant.set(montant);
        this.adresseIP = adresseIP;
    }

    public void transfererMontant(int montant, int idDest) throws RemoteException {
        new Thread() {
            public void run() {
                try {

                    if (obtenirMontant() - montant < 0) {
                        throw new Exception("Nous ne pouvons faire le transfert car le montant de cette succursale ne peut etre negatif.");
                    }

                    if (print) {
                        System.out.println();
                        System.out.println(ANSI_RED_BACKGROUND + "--> " + Integer.toString(montant) + " : " + Integer.toString(idDest));
                        System.out.println();
                    }

                    diminuerMontant(montant);

                    Thread.sleep(5000);

                    ISuccursale s = listeSuccursale.get(idDest);

                    s.ajoutMontant(montant, obtenirId());

                } catch (Exception e) {
                    e.toString();
                }
            }
        }.start();
    }

    public void ajoutMontant(int montant, int idSource) throws  RemoteException{
        this.montant.addAndGet(montant);

        if (print) {
            System.out.println();
            System.out.println(ANSI_RED_BACKGROUND + "<-- " + Integer.toString(montant) + " : " + Integer.toString(idSource));
            System.out.println();
        }
    }

    public int obtenirMontant() throws RemoteException{
        return montant.get();
    }

    public int obtenirId() throws RemoteException{
        return id;
    }

    public void assignerId(int id) throws RemoteException{
        this.id = id;
    }

    private void afficherSuccursale() throws  RemoteException{
        System.out.println();
        System.out.println(ANSI_RED_BACKGROUND + "Succursale numero " + Integer.toString(this.obtenirId()) + " et elle contenant "
                + Integer.toString(this.obtenirMontant()) + " dollar(s).");
        System.out.println();
    }

    public void afficherSuccursales() throws RemoteException{
        System.out.println();
        System.out.println(ANSI_RED_BACKGROUND + "Ceci est la succursale numero " + Integer.toString(this.obtenirId()) + " et elle contient un montant de "
        + Integer.toString(this.obtenirMontant()) + " dollar(s).");
        System.out.println();
        System.out.println("Succursale(s) de la banque :");
        System.out.println();

        for (Map.Entry<Integer, ISuccursale> succ: this.listeSuccursale.entrySet()) {
            ISuccursale s = succ.getValue();
            System.out.println(Integer.toString(s.obtenirId()) + ", montant : " + Integer.toString(s.obtenirMontant())+ " dollar(s).");
        }

        System.out.println();
    }

    public static void main(String[] args) {
        try {

            Integer argent = Integer.parseInt(args[0]);

            if (argent < 0) {
                exit(0);
            }

            String adresseIP = args[1];

            Registry registry = LocateRegistry.getRegistry(adresseIP,10000 );
            IBanque b = (IBanque) registry.lookup("Banque");


            Succursale s = new Succursale(argent, adresseIP);

            s.listeSuccursale = b.connexion(s);

            for (Map.Entry<Integer, ISuccursale> succ: s.listeSuccursale.entrySet()) {
                if(succ.getKey() != s.id){
                    succ.getValue().pingSuccursale(s.listeSuccursale.get(s.id));
                }
            }

            s.afficherSuccursales();

            TransfertFils transfertFils = new TransfertFils(s.listeSuccursale,  s.id);
            transfertFils.start();

            while(true){
                String input = System.console().readLine();

                switch (input.split(" ")[0]){
                    case "Transfert":
                        try {
                            ISuccursale destSucc = s.listeSuccursale.get(Integer.parseInt(input.split(" ")[2]));

                            if(destSucc == null){
                                System.out.println("La succursale visee par le transfert n'est pas valide");
                            }
                            else {
                                s.transfererMontant(Integer.parseInt(input.split(" ")[1]), Integer.parseInt(input.split(" ")[2]));
                            }

                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                    case "Erreur":
                        try {
                            s.diminuerMontant(Integer.parseInt(input.split(" ")[1]));
                            s.afficherSuccursales();
                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                    case "AfficherSuccursale":
                        try {
                            s.afficherSuccursale();
                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                    case "p":
                        try {
                            s.print = !s.print;
                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void diminuerMontant(int montant) throws  RemoteException{
        try {
            if (this.montant.get() - montant < 0) {
                throw new Exception("Nous ne pouvons faire le transfert car le montant de cette succursale ne peut etre negatif.");
            }

            this.montant.addAndGet(-montant);
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public void pingSuccursale(ISuccursale sourceSucc) throws  RemoteException
    {
        System.out.println("succ source : " + Integer.toString(sourceSucc.obtenirId()));
        System.out.println("this succ : " + Integer.toString(this.id));

       listeSuccursale.put(sourceSucc.obtenirId(), sourceSucc);
        try{
            listeSuccursale.get(this.id).afficherSuccursales();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
