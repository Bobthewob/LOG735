import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joey Roger on 2017-06-26.
 */
public class EtatGlobal {

    public ArrayList<EtatLocal> etatLocauxlocasse;
    public int montantBanque;

    public EtatGlobal(int montantBanque){
        etatLocauxlocasse =  new ArrayList<EtatLocal>();
        this.montantBanque = montantBanque;
    }

    public String toString(){
        String chaineFinale = "";
        int montantDetecte = 0;
        ArrayList<Canal> canauxFinal = new ArrayList<Canal>();

        chaineFinale += "Succursale d'origine de la capture : #" + etatLocauxlocasse.get(0).obtenirSuccursaleRacine() + "\r\n";

        for (EtatLocal etatLocal:etatLocauxlocasse){

            chaineFinale += "Succursale #" + etatLocal.obtenirIdSuccusale() + " : " + etatLocal.obtenirMontant() + "$\r\n";
            montantDetecte += etatLocal.obtenirMontant();

            for (Canal canal1:etatLocal.canaux) {

                boolean onAjoute = true;

                for (Canal canal2 : canauxFinal) {
                    if(canal1.obtenirSuccursaleSource() == canal2.obtenirSuccursaleDestination() && canal1.obtenirSuccursaleDestination() == canal2.obtenirSuccursaleSource())
                    {
                        canal2.assignerMontant(Math.abs(canal1.obtenirMontant() - canal2.obtenirMontant()));
                        onAjoute = false;
                    }
                }

                if(onAjoute)
                    canauxFinal.add(canal1);
            }
        }

        for (Canal canal : canauxFinal) {
            chaineFinale += "Canal S" + canal.obtenirSuccursaleSource() + "-S" + canal.obtenirSuccursaleDestination() + " : " + canal.obtenirMontant() + "$\r\n";
            montantDetecte += canal.obtenirMontant();
        }

        chaineFinale += "Somme connue par la banque : " + montantBanque + "$\r\n";
        chaineFinale += "Somme detectee par la par la capture : " + montantDetecte + "$\r\n";

        if(montantDetecte == montantBanque)
            chaineFinale += "ETAT GLOBAL COHERENT !!!";
        else
            chaineFinale += "ETAT GLOBAL INCOHERENT !!!";

        return chaineFinale;
    }
}