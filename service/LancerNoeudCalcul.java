import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LancerNoeudCalcul {    
    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.getRegistry("10.13.23.250", 1099);
            ServiceDistributeur distributeur = (ServiceDistributeur) reg.lookup("calculerImage");
            
            ServiceCalculImplementation calculator = new ServiceCalculImplementation();
            ServiceCalcul serviceCalcul = (ServiceCalcul) UnicastRemoteObject.exportObject(calculator, 0);
            distributeur.enregistrerClient(serviceCalcul);
            
            System.out.println("Enregistré dans service distributeur.");
            
            Thread.sleep(Long.MAX_VALUE);
        } catch(Exception e) {
            System.out.println("Erreur d'enregistrement ");
        }
    }
}