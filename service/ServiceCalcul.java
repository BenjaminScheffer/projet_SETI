import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceCalcul extends Remote{
    public void calcul()throws RemoteException;
}
