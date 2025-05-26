import java.rmi.RemoteException;
import raytracer.Scene;
import raytracer.Image;

public class ServiceCalculImplementation implements ServiceCalcul {
    
    @Override
    public Image calculerImage(Scene s, int x, int y, int width, int height) throws RemoteException {
        System.out.println("Calcul de l'image pour les coordonnées (" + x + ", " + y + ") avec une taille de " + width + "x" + height);
        return s.compute(x, y, width, height);
    }
}