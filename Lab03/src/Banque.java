/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Banque implements IBanque {
    private ConcurrentHashMap<Integer,ISuccursale> listeSuccursale = new ConcurrentHashMap<Integer,ISuccursale>();
    private AtomicInteger idCompteur = new AtomicInteger();
    private AtomicInteger montantTotal = new AtomicInteger();
    private AtomicInteger idEtat = new AtomicInteger();

    public Banque() {
        idCompteur.set(0);
        montantTotal.set(0);
        idEtat.set(0);
    }

    //Méthode permettant d'accepter les connexionx de succursales à la banque
    public ConcurrentHashMap<Integer,ISuccursale> connexion(ISuccursale nouvelleSuccursale) throws Exception{
        montantTotal.addAndGet(nouvelleSuccursale.obtenirMontant());

        System.out.println("Ajout d'une succursale ayant le montant "+ nouvelleSuccursale.obtenirMontant() +", montant total est de " + montantTotal);

        nouvelleSuccursale.assignerId(idCompteur.incrementAndGet()); //Attribution de l'identifiant unique
        listeSuccursale.put(nouvelleSuccursale.obtenirId(), nouvelleSuccursale);

        return listeSuccursale;
    }

    public int obtenirMontantTotal() throws RemoteException{
        return montantTotal.get();
    }

    public int obtenirIdEtat() throws RemoteException{
        return idEtat.getAndIncrement();
    }

    //Démarrage de la banque
    public static void main(String[] args) {
        try {
            System.out.println("Demarrage de la banque.");
            Banque b = new Banque();

            Registry registry = LocateRegistry.createRegistry(10000);
            IBanque skeleton = (IBanque) UnicastRemoteObject.exportObject(b, 10000); // Génère un stub vers notre service.
            registry.rebind("Banque", skeleton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}