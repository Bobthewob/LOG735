import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Joey Roger on 2017-06-08.
 */
public class TransfertFils extends Thread {
    private int id;
    private final Random seed = new Random();

    public TransfertFils(int id){
        this.id = id;
    }

    public void run(){

        try{
            while (true){

                int waitTime = (seed.nextInt(6) + 5) * 1000;

                //System.out.println("Attente de " + Integer.toString(waitTime / 1000) + " secondes avant transfert automatique.");
                //System.out.flush();

                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() < (startTime + waitTime)){
                    Thread.sleep(100);
                }

                Registry registry = LocateRegistry.getRegistry(10000);
                String[] Stubs = registry.list();
                ArrayList<String> validDestinationStub = new ArrayList<String>();

                for (String stub:Stubs) {
                    if((stub.indexOf("Succursale") != -1) && (id !=  Integer.parseInt(stub.substring(10)))){
                        validDestinationStub.add(stub);
                    }
                }

                if(validDestinationStub.size() == 0){
                    System.out.println("Aucune succursale valide pour le transfert.");
                    System.out.flush();
                }else{
                    String destinationStub = validDestinationStub.get(seed.nextInt(validDestinationStub.size()));

                    ISuccursale s = (ISuccursale) registry.lookup("Succursale" + id);
                    int montantSucc = s.obtenirMontant();
                    int montantTransfert = (int) Math.ceil ((1 / (seed.nextInt(11) + 0.0000001)) * montantSucc);
                    int succDestId = Integer.parseInt(destinationStub.substring(10));

                    System.out.println("Transfert de " + montantSucc + "dollars a la succursale " + succDestId);

                    s.transfererMontant(montantTransfert, succDestId);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
