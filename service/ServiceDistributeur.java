import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Scene;

public interface ServiceDistributeur extends Remote {
       void enregistrerClient(ServiceCalcul var1) throws RemoteException, InterruptedException;
       void faireCalcul(ServiceLocal service, Scene scene, int l, int h) throws RemoteException, InterruptedException;
}
