import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joey Roger on 2017-06-26.
 */
public class EtatLocal implements Serializable{

    private int idEtatLocal;
    private int idSuccursale;
    private int idSuccursaleRacine;
    private int nombreEnregistrementTermine = 0;
    private int nombreTotalCanauxAEnregistre;
    private boolean enregistrementTermine = false;
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

    private int montant;

    public int obtenirMontant(){
        return montant;
    }

    public EtatLocal(int idEtatLocal, int idSuccursale, int montant, int nombreTotalCanauxAEnregistre, int idSuccursaleRacine){ //premier snapshot fait lorsqu'on recoit le premier marqueur
        canaux = new ArrayList<Canal>();
        this.idEtatLocal = idEtatLocal;
        this.idSuccursale = idSuccursale;
        this.montant = montant;
        this.nombreTotalCanauxAEnregistre = nombreTotalCanauxAEnregistre;
        this.idSuccursaleRacine = idSuccursaleRacine;

        enregistrementTermine = (this.nombreTotalCanauxAEnregistre == 0);
    }

    public EtatLocal(int idEtatLocal, int idSuccursale, int montant, int nombreTotalCanauxAEnregistre){ //premier snapshot fait lorsqu'on recoit le premier marqueur
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

    public void enregistrerCanal(int succSource, int montant){
        boolean canalExisteDeja = false;

        for (Canal canal:canaux) {
            if(canal.obtenirSuccursaleSource() == succSource){
                canalExisteDeja = true;
                canal.assignerMontant(canal.obtenirMontant() + montant);
            }
        }

        if(!canalExisteDeja) { //si le canal n'existe pas nous en creeons un nouveau
            canaux.add(new Canal(montant, this.idSuccursale, succSource));
        }
    }

}
