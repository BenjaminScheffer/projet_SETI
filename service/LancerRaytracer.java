import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.time.Duration;
import raytracer.Scene;
import raytracer.Image;

public class LancerRaytracer implements ServiceDistributeur {

    private List<ServiceCalcul> noeudCalculs = new ArrayList<>();

    public LancerRaytracer() {
    }

    @Override
    public void enregistrerClient(ServiceCalcul service) throws RemoteException, InterruptedException {
        synchronized (noeudCalculs) {
            noeudCalculs.add(service);
        }
        System.out.println("Nouveau service calcul ajouté, Total: " + noeudCalculs.size());
    }

    @Override
    public void faireCalcul(ServiceLocal serviceClient, Scene scene, int l, int h)
            throws RemoteException, InterruptedException {
        List<Calcul> calculs = new ArrayList<>();
        List<Calcul> calculsEnCours = new ArrayList<>();
        int x0 = 0, y0 = 0;
        Instant debut = Instant.now();
        System.out.println("Calcul de l'image :\n - Coordonnées : " + x0 + "," + y0 + "\n - Taille " + l + "x" + h);

        int indexNoeudCalcul = 0;

        for (int i = 0; i < l; i = i + 50) {
            for (int j = 0; j < h; j = j + 50) {
                calculs.add(new Calcul(i, j));
            }
        }

        AtomicBoolean clientActif = new AtomicBoolean(true);

        while (!calculs.isEmpty() || !calculsEnCours.isEmpty() && clientActif.get()) {

            while (noeudCalculs.size() == 0 && (!calculs.isEmpty() || !calculsEnCours.isEmpty())) {
                System.out.println("Aucune machine disponible, attente de reconnexion...");
                Thread.sleep(1000);
            }

            while (!calculs.isEmpty() && noeudCalculs.size() > 0) {
                synchronized (noeudCalculs) {
                    indexNoeudCalcul = (indexNoeudCalcul + 1) % noeudCalculs.size();
                }

                Calcul c;
                synchronized (calculs) {
                    if (calculs.isEmpty())
                        break;
                    c = calculs.remove(0);
                }

                synchronized (calculsEnCours) {
                    calculsEnCours.add(c);
                }

                ServiceCalcul serviceCalcul = noeudCalculs.get(indexNoeudCalcul);
                Thread t = new ThreadCalcul(c, serviceCalcul) {
                    public void run() {
                        try {
                            if (!clientActif.get())
                                return;

                            Image image = this.service.calculerImage(scene, this.c.x, this.c.y, 50, 50);
                            ImageEnvoyer imageEnvoyer = new ImageEnvoyer(image, this.c.x, this.c.y);

                            try {
                                serviceClient.recevoirImage(imageEnvoyer);
                                synchronized (calculsEnCours) {
                                    calculsEnCours.remove(this.c);
                                    System.out.println("Calcul terminé. Calculs en attente: " + calculs.size()
                                            + ", en cours: " + calculsEnCours.size());
                                }
                            } catch (RemoteException e) {
                                clientActif.set(false);
                                System.out.println("Erreur pendant l'envoi au client");

                                synchronized (calculs) {
                                    calculs.add(this.c);
                                }
                                synchronized (calculsEnCours) {
                                    calculsEnCours.remove(this.c);
                                }
                            }
                        } catch (ConnectException e) {
                            synchronized (noeudCalculs) {
                                try {
                                    noeudCalculs.remove(serviceCalcul);
                                    System.out.println("Service de calcul retiré, Total: " + noeudCalculs.size());
                                } catch (IndexOutOfBoundsException ex) {

                                }
                            }
                            // Remettre le calcul dans la liste pour retry
                            synchronized (calculs) {
                                calculs.add(this.c);
                            }
                            synchronized (calculsEnCours) {
                                calculsEnCours.remove(this.c);
                            }

                        } catch (RemoteException remote) {
                            System.out.println("Erreur de communication avec le service" );
                            // Remettre le calcul dans la liste pour retry
                            synchronized (calculs) {
                                calculs.add(this.c);
                            }
                            synchronized (calculsEnCours) {
                                calculsEnCours.remove(this.c);
                            }
                        }
                    }
                };
                t.start();
            }

            // Petite pause pour éviter une boucle trop intensive
            Thread.sleep(100);
        }

        Instant fin = Instant.now();
        long duree = Duration.between(debut, fin).toMillis();
        System.out.println("Image calculée en :" + duree + " ms");
    }
}