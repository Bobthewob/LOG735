/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.io.Serializable;
import java.util.ArrayList;

public class EtatLocal implements Serializable{

    private int idEtatLocal;
    private int idSuccursale;
    private int idSuccursaleRacine;
    private int nombreEnregistrementTermine = 0;
    private int nombreTotalCanauxAEnregistre;
    private boolean enregistrementTermine = false;
    private int montant;
    public ArrayList<Canal> canaux;

    public boolean obtenirEnregistrementTermine(){
        return enregistrementTermine;
    }

    public void incrementerEnregistrementTermine(){
        ++nombreEnregistrementTermine;
        enregistrementTermine = (nombreTotalCanauxAEnregistre == nombreEnregistrementTermine);
    }

    public int obtenirIdSuccusale(){
        return this.idSuccursale;
    }

    public int obtenirIdEtatGlobal(){
        return idEtatLocal;
    }

    public int obtenirMontant(){
        return montant;
    }

    //Constructeur pour l'état local de la succursale initiatrice
    public EtatLocal(int idEtatLocal, int idSuccursale, int montant, int nombreTotalCanauxAEnregistre, int idSuccursaleRacine){
        canaux = new ArrayList<Canal>();
        this.idEtatLocal = idEtatLocal;
        this.idSuccursale = idSuccursale;
        this.montant = montant;
        this.nombreTotalCanauxAEnregistre = nombreTotalCanauxAEnregistre;
        this.idSuccursaleRacine = idSuccursaleRacine;
        enregistrementTermine = (this.nombreTotalCanauxAEnregistre == 0);
    }

    //Constructeur pour les succursales non initiatrices
    public EtatLocal(int idEtatLocal, int idSuccursale, int montant, int nombreTotalCanauxAEnregistre){
        canaux = new ArrayList<Canal>();
        this.idEtatLocal = idEtatLocal;
        this.idSuccursale = idSuccursale;
        this.montant = montant;
        this.nombreTotalCanauxAEnregistre = nombreTotalCanauxAEnregistre;
        this.idSuccursaleRacine = idSuccursale;
        enregistrementTermine = (this.nombreTotalCanauxAEnregistre == 0);
    }

    public int obtenirSuccursaleRacine(){
        return idSuccursaleRacine;
    }

    //Enregistre un canal à l'état local
    public void enregistrerCanal(int succSource, int montant){
        boolean canalExisteDeja = false;

        for (Canal canal:canaux) {
            if(canal.obtenirSuccursaleSource() == succSource){
                canalExisteDeja = true;
                //Si le canal existe déjà, le montant est additionné
                canal.ajouterMontant(montant);
            }
        }

        //Évite l'enregistrement de doublons
        if(!canalExisteDeja) {
            canaux.add(new Canal(montant, this.idSuccursale, succSource));
        }
    }

}
