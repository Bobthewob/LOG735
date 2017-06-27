/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Canal implements Serializable {
    private AtomicInteger montant = new AtomicInteger();

    public int obtenirMontant() {
        return montant.get();
    }

    public void ajouterMontant(int montant) {
        this.montant.getAndAdd(montant);
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
        this.montant.set(montant);
        this.succursaleDestination = succursaleDestination;
        this.succursaleSource = succursaleSource;
    }
}
