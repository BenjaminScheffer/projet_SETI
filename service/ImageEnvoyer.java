import java.io.Serializable;
import raytracer.Image;

public class ImageEnvoyer implements Serializable{
    public Image image;
    public int x,y;

    public ImageEnvoyer(Image i,int x , int y){
        this.image = i;
        this.x = x;
        this.y = y;
    }
}