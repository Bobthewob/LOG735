/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISuccursale extends Remote {
    //On met les methodes accessibles a distance ici
    int obtenirId() throws  RemoteException;
    void assignerId(int id) throws  RemoteException;
    int obtenirMontant() throws  RemoteException;
    void transfererMontant(int montant,int idSource) throws RemoteException;
    void ajoutMontant(int montant, int idSource) throws  RemoteException;
    void diminuerMontant(int montant) throws  RemoteException;
    void afficherSuccursales() throws RemoteException;
    void pingSuccursale(ISuccursale sourceSucc) throws  RemoteException;
}
