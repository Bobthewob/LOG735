/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #3
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface IBanque extends Remote {
    HashMap<Integer,ISuccursale> connexion(ISuccursale nouvelleSuccursale) throws Exception;
    int obtenirMontantTotal() throws RemoteException;
}
