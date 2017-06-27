/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.util.HashMap;
import java.util.Random;

public class EtatGlobalFils extends Thread {
    private HashMap<Integer,ISuccursale> listeSuccursale;
    private int idSource;
    private final Random seed = new Random();

    public EtatGlobalFils(HashMap<Integer,ISuccursale> listeSuccursale, int idSource){
        this.listeSuccursale = listeSuccursale;
        this.idSource = idSource;
    }

    //Effectue un transfert automatique selon un délai aléatoire variant de 10 à 30 secondes
    public void run(){
        ISuccursale succSource = this.listeSuccursale.get(this.idSource);
        try{
            while (true){
                int waitTime = (seed.nextInt(31) + 10) * 1000;
                Thread.sleep(waitTime);
                succSource.demarrerEtatGlobal();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
