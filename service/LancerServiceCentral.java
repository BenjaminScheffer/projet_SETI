import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LancerServiceCentral {
    public static void main(String args[])throws RemoteException{
	ServiceCentral d = new ServiceCentral();
	//Cast l'object pour lui attribuer un port 
	ServiceDistributeur s = (ServiceDistributeur) UnicastRemoteObject.exportObject(d,0);//0 pour que le system lui attribut tout seul
	//Creation de l'annuaire  
	Registry reg = LocateRegistry.createRegistry(1099);
	//Enregistrement dans l'annuaire de l'objet
	reg.rebind("distributeur",s);
    }
}
