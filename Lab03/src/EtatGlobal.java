/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EtatGlobal {
    public ConcurrentLinkedQueue<EtatLocal> etatsLocaux;
    private int montantBanque;
    private int idSuccursaleRacine;

    public EtatGlobal(int montantBanque, int idSuccursaleRacine){
        etatsLocaux =  new ConcurrentLinkedQueue<EtatLocal>();
        this.montantBanque = montantBanque;
        this.idSuccursaleRacine = idSuccursaleRacine;
    }

    public ConcurrentLinkedQueue<EtatLocal> obtenirEtatsLocaux() {
        return this.etatsLocaux;
    }

    //Override du toString permettant d'afficher toutes les informations de la capture de l'état global
    public String toString(){
        String chaineFinale = "";
        int montantDetecte = 0;
        ArrayList<Canal> canauxFinal = new ArrayList<Canal>();

        chaineFinale += "Succursale d'origine de la capture : #" + this.idSuccursaleRacine + "\r\n";

        for (EtatLocal etatLocal: etatsLocaux){
            chaineFinale += "Succursale #" + etatLocal.obtenirIdSuccusale() + " : " + etatLocal.obtenirMontant() + "$\r\n";
            montantDetecte += etatLocal.obtenirMontant();

            //Vérification des doublons pour ne pas les afficher
            for (Canal canal1:etatLocal.canaux) {
                boolean onAjoute = true;

                for (Canal canal2 : canauxFinal) {
                    if(canal1.obtenirSuccursaleSource() == canal2.obtenirSuccursaleDestination() && canal1.obtenirSuccursaleDestination() == canal2.obtenirSuccursaleSource()) {
                        canal2.ajouterMontant(canal1.obtenirMontant());
                        onAjoute = false;
                    }
                }

                if(onAjoute)
                    canauxFinal.add(canal1);
            }
        }

        for (Canal canal : canauxFinal) {
            chaineFinale += "Canal S" + canal.obtenirSuccursaleSource() + "-S" + canal.obtenirSuccursaleDestination() + " : " + canal.obtenirMontant() + "$\r\n";
            montantDetecte += canal.obtenirMontant();
        }

        chaineFinale += "Somme connue par la banque : " + montantBanque + "$\r\n";
        chaineFinale += "Somme detectee par la par la capture : " + montantDetecte + "$\r\n";

        if(montantDetecte == montantBanque)
            chaineFinale += "ETAT GLOBAL COHERENT !!!\r\n\r\n";
        else
            chaineFinale += "ETAT GLOBAL INCOHERENT !!!\r\n\r\n";

        return chaineFinale;
    }
}