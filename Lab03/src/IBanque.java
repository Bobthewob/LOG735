import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by Joey Roger on 2017-06-06.
 */
public interface IBanque extends Remote {
    Integer connexion(int montant) throws Exception;
    void miseAJourSuccursales() throws Exception;
    int obtenirMontantTotal() throws RemoteException;
}
