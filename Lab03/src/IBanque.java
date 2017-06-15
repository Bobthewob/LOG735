import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by Joey Roger on 2017-06-06.
 */
public interface IBanque extends Remote {
    HashMap<Integer,ISuccursale> connexion(ISuccursale nouvelleSuccursale) throws Exception;
    int obtenirMontantTotal() throws RemoteException;
}
