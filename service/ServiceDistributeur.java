import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceDistributeur extends Remote{
       void enregistrerClient(ServiceCalcul var1) throws RemoteException,InterruptedException;
}
