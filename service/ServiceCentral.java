import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;

public class ServiceCentral implements ServiceDistributeur{
    
    public List<ServiceCalcul> services = new ArrayList<>();
   
    public void enregistrerClient(ServiceCalcul var1) throws RemoteException,InterruptedException{
        synchronized(services){
            this.services.add(var1);
        }

        String host = "";
        try{
            host = RemoteServer.getClientHost();
            System.out.println("Nouveau Service : " + host +" "+ services.size() + " tableaux connectés");
        }catch(ServerNotActiveException e){
     
        }
    }
}
