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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

public class Succursale extends UnicastRemoteObject implements Serializable,ISuccursale {
    private int id;
    private AtomicInteger montant = new AtomicInteger();
    private String adresseIP;
    private boolean print = true;
    public ConcurrentHashMap<Integer,ISuccursale> listeSuccursale = new ConcurrentHashMap<Integer,ISuccursale>();
    private ConcurrentLinkedQueue<EtatLocal> etatsLocaux = new ConcurrentLinkedQueue<EtatLocal>();
    private HashMap<Integer,EtatGlobal> listeGlobale = new HashMap<Integer,EtatGlobal>();

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
                        System.out.println("--> " + Integer.toString(montant) + " : " + Integer.toString(idDest));
                        System.out.println();
                    }

                    diminuerMontant(montant);

                    Thread.sleep(5000);

                    ISuccursale s = listeSuccursale.get(idDest);
                    s.ajouterMontant(montant, obtenirId());

                } catch (Exception e) {
                    e.toString();
                }
            }
        }.start();
    }

    public void ajouterMontant(int montant, int idSource) throws  RemoteException{
        this.montant.addAndGet(montant);
        enregistrerModification(idSource, montant);

        if (print) {
            System.out.println();
            System.out.println("<-- " + Integer.toString(montant) + " : " + Integer.toString(idSource));
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
        System.out.println("Succursale numero " + Integer.toString(this.obtenirId()) + " et elle contenant "
                + Integer.toString(this.obtenirMontant()) + " dollar(s).");
        System.out.println();
    }

    public void afficherSuccursales() throws RemoteException{
        System.out.println();
        System.out.println("Ceci est la succursale numero " + Integer.toString(this.obtenirId()) + " et elle contient un montant de "
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

           EtatGlobalFils etatGlobalFils = new EtatGlobalFils(s.listeSuccursale, s.id);
           etatGlobalFils.start();

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
                    case "P":
                        try {
                            s.print = !s.print;
                        }catch (Exception e){
                            System.out.println(e.toString());
                        }
                        break;
                    case "EG":
                        try {
                            System.out.println("Démarrage d'une capture d'état");
                            s.demarrerEtatGlobal();
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

        } catch (Exception e){
            System.out.println(e.toString());
        }
    }

    //Démarrage de la capture d'un état global
    public void demarrerEtatGlobal() throws  RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry(adresseIP, 10000);
            IBanque b = (IBanque) registry.lookup("Banque");

            int idEtatGlobal = b.obtenirIdEtat();
            listeGlobale.put(idEtatGlobal, new EtatGlobal(b.obtenirMontantTotal(), this.id));

            EtatLocal etatLocal = new EtatLocal(idEtatGlobal, obtenirId(), obtenirMontant(), listeSuccursale.size() - 1);
            etatsLocaux.add(etatLocal);

            Thread.sleep(5000); // on simule la meme attente que le transfert d'argent

            for (Map.Entry<Integer, ISuccursale> succ : listeSuccursale.entrySet()) {
                if (succ.getKey() != obtenirId()) {
                    new Thread() {
                        public void run() {
                            try {
                                succ.getValue().recevoirMarqueur(idEtatGlobal, obtenirId());   //on envoit le marqueur a tous les succursales sauf nous-meme.
                            } catch (Exception e) {
                                e.toString();
                            }
                        }
                    }.start();
                }
            }
        }catch (Exception e){
            e.toString();
        }
    }

    //Reçoit un marqueur, diffuse aux autres.
    public void recevoirMarqueur(int idEtatGlobal, int idSuccRacine) {
        try {
            boolean etatLocalExisteDeja = false;

            for (EtatLocal etatLocal : etatsLocaux) {
                if (etatLocal.obtenirIdEtatGlobal() == idEtatGlobal) {
                    etatLocalExisteDeja = true;
                    etatLocal.incrementerEnregistrementTermine();

                    if(etatLocal.obtenirEnregistrementTermine()) // nous avons termine notre etat local
                        listeSuccursale.get(etatLocal.obtenirSuccursaleRacine()).ajouterEtatLocal(etatLocal);
                }
            }

            if (!etatLocalExisteDeja) { // premier marqueur recu
                EtatLocal etatLocalCourant = new EtatLocal(idEtatGlobal, obtenirId(), obtenirMontant(), listeSuccursale.size() - 2, idSuccRacine); //on enregistre notre etat

                etatsLocaux.add(etatLocalCourant);

                //on broadcast aux autres succursales
                Thread.sleep(5000); // on simule la meme attente que le transfert d'argent

                for (Map.Entry<Integer, ISuccursale> succ: listeSuccursale.entrySet()) {
                    if(succ.getKey() != obtenirId()){
                        new Thread() {
                            public void run() {
                                try {
                                    succ.getValue().recevoirMarqueur(idEtatGlobal, obtenirId());  //on envoit le marqueur a tous les succursale sauf nous.
                                } catch (Exception e) {
                                    e.toString();
                                }
                            }
                        }.start();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Crée un canal afin d'enregistrer le transfert dans l'état local
    public void enregistrerModification(int succSource, int montant){
        for (EtatLocal etatLocal:etatsLocaux) {
            if ((!etatLocal.obtenirEnregistrementTermine()) && (succSource != etatLocal.obtenirSuccursaleRacine())) { // si on enregistre encore les modification
                etatLocal.enregistrerCanal(succSource, montant);
            }
        }
    }

    //Ajoute l'état local à l'état global, si la capture est terminée, print le résultat
    public void ajouterEtatLocal(EtatLocal etatLocal){
        listeGlobale.get(etatLocal.obtenirIdEtatGlobal()).obtenirEtatsLocaux().add(etatLocal);

        if(listeGlobale.get(etatLocal.obtenirIdEtatGlobal()).obtenirEtatsLocaux().size() == listeSuccursale.size()) { //on a recu tous les marqueurs
            System.out.print(listeGlobale.get(etatLocal.obtenirIdEtatGlobal()).toString() );
        }
    }

    //Permet d'informer de l'ajout d'une succursale
    public void pingSuccursale(ISuccursale sourceSucc) throws RemoteException
    {
        listeSuccursale.put(sourceSucc.obtenirId(), sourceSucc);
        try{
            listeSuccursale.get(this.id).afficherSuccursales();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
