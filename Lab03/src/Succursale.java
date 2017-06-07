import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Created by Joey Roger on 2017-06-06.
 */
public class Succursale implements Serializable,ISuccursale {

    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private int id;
    private int montant;
    private String adresseIP;
    //public ArrayList<Integer> listeSuccursale = new ArrayList<Integer>();

    public Succursale(int montant, int id,String adresseIP){
        this.montant = montant;
        this.id = id;
        this.adresseIP = adresseIP;
    }

    public void transfererMontant(int montant, int idSource) throws RemoteException {

        try {
            if(this.montant - montant < 0){
                throw  new Exception("Nous ne pouvons faire le transfert car le montant de cette succursale ne peut etre negatif.");
            }
            this.montant -= montant;

            Thread.sleep(5000);

            Registry registry = LocateRegistry.getRegistry(10000);
            ISuccursale s = (ISuccursale) registry.lookup("Succursale" + idSource);

            s.ajoutMontant(montant);

            IBanque b = (IBanque) registry.lookup("Banque");
            b.miseAJourSuccursales();

        }catch (Exception e){
            e.toString();
        }
    }

    public void ajoutMontant(int montant) throws  RemoteException{
        this.montant += montant;
    }

    public void mettreAJourSuccursale() throws RemoteException{
        try {
            afficherSuccursales();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int obtenirMontant() throws RemoteException{
        return montant;
    }

    public int obtenirId() throws RemoteException{
        return id;
    }

    private void afficherSuccursales() throws Exception{

        //System.out.print("\033[H\033[2J");
        //System.out.flush();
        System.out.println();
        System.out.println(ANSI_RED_BACKGROUND + "Ceci est la succursale numero " + Integer.toString(this.obtenirId()) + " et elle contient un montant de "
        + Integer.toString(this.obtenirMontant()) + " dollar(s).");
        System.out.println();
        System.out.println("Succursale(s) de la banque :");
        System.out.println();

        Registry registry = LocateRegistry.getRegistry(adresseIP ,10000);
        String[] services = registry.list();

        for (String succ:services) {
            if(succ.indexOf("Succursale") != -1) {
                ISuccursale s = (ISuccursale) registry.lookup(succ);
                System.out.println(Integer.toString(s.obtenirId()) + ", montant : " + Integer.toString(s.obtenirMontant())+ " dollar(s).");
            }
        }

        System.out.println();
        //System.out.print(ANSI_GREEN_BACKGROUND+"faire un transfert (montant id_succursale_destination) : ");
    }

    public static void main(String[] args) {
        try {

            Integer argent = Integer.parseInt(args[0]);
            String adresseIP = args[1];

            Registry registry = LocateRegistry.getRegistry(adresseIP,10000 );
            IBanque b = (IBanque) registry.lookup("Banque");

            Integer id = b.connexion(argent);
            Succursale s = new Succursale(argent, id, adresseIP);

            ISuccursale skeleton = (ISuccursale) UnicastRemoteObject.exportObject(s, 10000 + s.id); // Génère un stub vers notre service.
            registry.bind("Succursale" + Integer.toString(s.id), skeleton);

            b.miseAJourSuccursales();

            while(true){
                String input = System.console().readLine();

                switch (input.split(" ")[0]){
                    case "Transfert":
                        try {

                            String[] array = registry.list();
                            boolean succExist = false;

                            for (String succ:array) {
                                if(succ.indexOf("Succursale") != -1) {
                                    if(Integer.parseInt(succ.substring(10)) == Integer.parseInt(input.split(" ")[2])){
                                        succExist = true;
                                        break;
                                    }
                                }
                            }

                            if(!succExist){
                                throw  new Exception("La succursale destination nest pas valide.");
                            }

                            ISuccursale s1 = (ISuccursale) registry.lookup("Succursale" + id);
                            int montantSucc = s1.obtenirMontant();

                            if(Integer.parseInt(input.split(" ")[1]) > montantSucc){
                                throw  new Exception("Le montant dois etre positif.");
                            }

                            s1.transfererMontant(Integer.parseInt(input.split(" ")[1]), Integer.parseInt(input.split(" ")[2]));

                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                    case "Erreur":
                        try {
                            ISuccursale s1 = (ISuccursale) registry.lookup("Succursale" + id);
                            s1.diminuerMontant(Integer.parseInt(input.split(" ")[1]));
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
            if (this.montant - montant < 0) {
                throw new Exception("Nous ne pouvons faire le transfert car le montant de cette succursale ne peut etre negatif.");
            }

            this.montant -= montant;

            Registry registry = LocateRegistry.getRegistry(adresseIP,10000 );
            IBanque b = (IBanque) registry.lookup("Banque");
            b.miseAJourSuccursales();
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
