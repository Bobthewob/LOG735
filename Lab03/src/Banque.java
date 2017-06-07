/**
 * Created by Joey Roger on 2017-06-06.
 */
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Banque implements IBanque {

    private ArrayList<Integer> listeSuccursale = new ArrayList<Integer>();
    private int idCompteur = 0;
    private int montantTotal = 0;

    public Banque() {}

    public Integer connexion(int montant) throws Exception{
        montantTotal += montant;

        ++idCompteur;

        System.out.println("Ajout d'une succursale, montant total est de " + montantTotal);

        return idCompteur;
    }

    public void miseAJourSuccursales() throws Exception{

        Registry registry = LocateRegistry.getRegistry("127.0.0.1" ,10000);
        String[] services = registry.list();

        for (String succ:services) {
            if(succ.indexOf("Succursale") != -1)
            {
                ISuccursale s = (ISuccursale) registry.lookup(succ);
                s.mettreAJourSuccursale();
            }
        }
    }

    public int obtenirMontantTotal() throws RemoteException{
        return montantTotal;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Demarrrage de la banque.");
            Banque b = new Banque();

            Registry registry = LocateRegistry.createRegistry(10000);
            IBanque skeleton = (IBanque) UnicastRemoteObject.exportObject(b, 10000); // Génère un stub vers notre service.
            registry.rebind("Banque", skeleton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}