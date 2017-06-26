import java.io.Serializable;

/**
 * Created by Joey Roger on 2017-06-26.
 */
public class Canal implements Serializable {

    private int montant;

    public int obtenirMontant() {
        return montant;
    }

    public void assignerMontant(int montant) {
        this.montant = montant;
    }

    private int succursaleDestination;

    public int obtenirSuccursaleDestination() {
        return succursaleDestination;
    }

    private int succursaleSource;

    public int obtenirSuccursaleSource() {
        return succursaleSource;
    }

    public Canal(int montant, int succursaleDestination, int succursaleSource){
        this.montant = montant;
        this.succursaleDestination = succursaleDestination;
        this.succursaleSource = succursaleSource;
    }
}
