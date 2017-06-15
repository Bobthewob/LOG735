/**
 * Created by Joey Roger on 2017-06-06.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISuccursale extends Remote {
    //On met les methodes accessibles a distance ici
    int obtenirId() throws  RemoteException;
    void assignerId(int id) throws  RemoteException;
    int obtenirMontant() throws  RemoteException;
    void transfererMontant(int montant,int idSource) throws RemoteException;
    void ajoutMontant(int montant) throws  RemoteException;
    void diminuerMontant(int montant) throws  RemoteException;
    void afficherSuccursales() throws RemoteException;
    void pingSuccursale(ISuccursale sourceSucc) throws  RemoteException;
}
