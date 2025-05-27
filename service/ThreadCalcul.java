public class ThreadCalcul extends Thread{

    Calcul c;
    ServiceCalcul service;

    public ThreadCalcul(Calcul c,ServiceCalcul serviceCalcul){
        this.c=c;
        this.service = serviceCalcul;
    }
    
    
}
