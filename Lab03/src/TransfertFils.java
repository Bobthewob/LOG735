/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.util.HashMap;
import java.util.Random;

public class TransfertFils extends Thread {
    private HashMap<Integer,ISuccursale> listeSuccursale;
    private int idSource;
    private final Random seed = new Random();

    public TransfertFils(HashMap<Integer,ISuccursale> listeSuccursale, int idSource){
        this.listeSuccursale = listeSuccursale;
        this.idSource = idSource;
    }

    //Déclenche des transferts automatiques à une succursale aléatoire à toutes les 5 secondes
    public void run(){
        try{
            while (true){
                int waitTime = (seed.nextInt(6) + 5) * 1000;
                Thread.sleep(waitTime);

                if(this.listeSuccursale.size() == 1){
                    System.out.println("Aucune succursale valide pour le transfert.");
                    System.out.flush();
                }
                else {
                    ISuccursale succDest;
                    Object[] tableauSucc = this.listeSuccursale.values().toArray();

                    //Sélection de la succursale de destination
                    do {
                        succDest = (ISuccursale) tableauSucc[seed.nextInt(tableauSucc.length)];
                    } while (succDest.obtenirId() == this.idSource);

                    ISuccursale succSource = this.listeSuccursale.get(this.idSource);
                    int montantSucc = succSource.obtenirMontant();

                    int montantTransfert = (int) Math.ceil(montantSucc / (seed.nextInt(10) + 1));

                    succSource.transfererMontant(montantTransfert, succDest.obtenirId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
