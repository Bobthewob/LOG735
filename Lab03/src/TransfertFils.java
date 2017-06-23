import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Joey Roger on 2017-06-08.
 */
public class TransfertFils extends Thread {
    private HashMap<Integer,ISuccursale> listeSuccursale;
    private int idSource;
    private final Random seed = new Random();

    public TransfertFils(HashMap<Integer,ISuccursale> listeSuccursale, int idSource){
        this.listeSuccursale = listeSuccursale;
        this.idSource = idSource;
    }

    public void run(){

        try{
            while (true){

                int waitTime = (seed.nextInt(6) + 5) * 1000;

                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() < (startTime + waitTime)){
                    Thread.sleep(100);
                }

                if(this.listeSuccursale.size() == 1){
                    System.out.println("Aucune succursale valide pour le transfert.");
                    System.out.flush();
                }
                else {
                    ISuccursale succDest;
                    Object[] tableauSucc = this.listeSuccursale.values().toArray();

                    do {
                        succDest = (ISuccursale) tableauSucc[seed.nextInt(tableauSucc.length)];
                    } while (succDest.obtenirId() == this.idSource);

                    ISuccursale succSource = this.listeSuccursale.get(this.idSource);
                    int montantSucc = succSource.obtenirMontant();

                    int montantTransfert = (int) Math.ceil(montantSucc / (seed.nextInt(10) + 1));

                    //System.out.println("Transfert automatique de " + Integer.toString(montantTransfert) + " dollars a la succursale " + succDest.obtenirId());

                    succSource.transfererMontant(montantTransfert, succDest.obtenirId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
