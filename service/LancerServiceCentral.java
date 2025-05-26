import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LancerServiceCentral {
    public static void main(String args[]) throws RemoteException {
        try {
            LancerRaytracer d = new LancerRaytracer();
            ServiceDistributeur s = (ServiceDistributeur) UnicastRemoteObject.exportObject(d, 0);
            
            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("calculerImage", s);
            
            System.out.println("Service central en cours d'exécution...");
        } catch (Exception e) {
            System.out.println("Erreur dans le service central : ");
            e.printStackTrace();
        }
    }
}