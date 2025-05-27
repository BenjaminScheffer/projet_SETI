import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import raytracer.Disp;
import raytracer.Scene; 

public class LancerClient {    
    public static void main (String[] args) {
        try {
            int width = 1000;
            int height = 1000;
            Disp disp = new Disp("Client", width, height);
            Scene scene = new Scene("simple.txt", width, height);
            
            Registry reg = LocateRegistry.getRegistry("10.13.23.250", 1099);
            ServiceDistributeur distributeur = (ServiceDistributeur) reg.lookup("calculerImage");
            
            RecevoirImage receveurImage = new RecevoirImage(disp);
            ServiceLocal receveurImageService = (ServiceLocal) UnicastRemoteObject.exportObject(receveurImage, 0);
            
            System.out.println("Lancement d'une demande de calcul...");
            distributeur.faireCalcul(receveurImageService, scene, width, height);
            
        } catch(Exception e) {
            System.out.println("Erreur client: ");
            e.printStackTrace();
        }
    }
}