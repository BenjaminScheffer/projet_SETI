import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceLocal extends Remote{
     void recevoirImage(ImageEnvoyer image)throws RemoteException;
}
