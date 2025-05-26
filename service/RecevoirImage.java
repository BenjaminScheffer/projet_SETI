import java.rmi.RemoteException;
import raytracer.Disp;

public class RecevoirImage implements ServiceLocal{
    public Disp disp;
    
    public RecevoirImage(Disp d){
        this.disp = d;
    }

    
    public void recevoirImage(ImageEnvoyer image) throws RemoteException {
        try{
            disp.setImage(image.image,image.x,image.y);
        }catch(Exception e){
            System.out.println("Erreur jsp");
        } 
    }
    
}
