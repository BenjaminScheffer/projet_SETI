import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Scene;
import raytracer.Image;

public interface ServiceCalcul extends Remote{
    public Image calculerImage(Scene s , int indexLongueur,int indexHauteur,int l,int h,int indice)throws RemoteException;
}
