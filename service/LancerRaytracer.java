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

    private List<ServiceCalcul> machines = new ArrayList<>();

    public LancerRaytracer() {
    }

    @Override
    public void enregistrerClient(ServiceCalcul service) throws RemoteException, InterruptedException {
        machines.add(service);
        System.out.println("Nouveau service calcul ajouté, Total: " + machines.size());
    }

    @Override
    public void faireCalcul(ServiceLocal clientDisplay, Scene scene, int l, int h)
            throws RemoteException, InterruptedException {
        List<Calcul> calculs = new ArrayList<>();
        List<Calcul> calculsEnCours = new ArrayList<>(); // Nouvelle liste pour les calculs en cours
        int x0 = 0, y0 = 0;
        Instant debut = Instant.now();
        System.out.println("Calcul de l'image :\n - Coordonnées : " + x0 + "," + y0
                + "\n - Taille " + l + "x" + h);

        int indexMachine = 0;

        for (int i = 0; i < l; i = i + 50) {
            for (int j = 0; j < h; j = j + 50) {
                calculs.add(new Calcul(i, j));
            }
        }
        AtomicBoolean clientActif = new AtomicBoolean(true);
        // Modifier la condition pour continuer même sans machines temporairement
        while (!calculs.isEmpty() || !calculsEnCours.isEmpty() && clientActif.get()) {
            // Attendre qu'au moins une machine soit disponible
            while (machines.size() == 0 && (!calculs.isEmpty() || !calculsEnCours.isEmpty())) {
                System.out.println("Aucune machine disponible, attente de reconnexion...");
                Thread.sleep(1000);
            }

            // Traiter seulement les calculs qui ne sont pas encore en cours
            while (!calculs.isEmpty() && machines.size() > 0) {
                synchronized (machines) {
                    indexMachine = (indexMachine + 1) % machines.size();
                }

                Calcul c;
                synchronized (calculs) {
                    if (calculs.isEmpty()) break;
                    c = calculs.remove(0); // Retirer immédiatement de calculs
                }

                synchronized (calculsEnCours) {
                    calculsEnCours.add(c); // Ajouter aux calculs en cours
                }

                ServiceCalcul serviceCalcul = machines.get(indexMachine);
                Thread t = new ThreadCalcul(c, serviceCalcul) {
                    public void run() {
                        try {
                            if (!clientActif.get()) return;
                            // Appel du service de calcul pour obtenir l'image
                            int indice;
                            synchronized (calculsEnCours) {
                                indice = calculsEnCours.size();
                            }
                            Image image = this.service.calculerImage(scene, this.c.x, this.c.y, 50, 50, indice);
                            ImageEnvoyer imageEnvoyer = new ImageEnvoyer(image, this.c.x, this.c.y);

                            try {
                                clientDisplay.recevoirImage(imageEnvoyer);
                                synchronized (calculsEnCours) {
                                    calculsEnCours.remove(this.c);
                                    System.out.println("Calcul terminé. Calculs en attente: " + calculs.size()
                                            + ", en cours: " + calculsEnCours.size());
                                }
                            } catch (Exception e) {
                                clientActif.set(false); 
                                System.out.println("Erreur pendant l'envoi au client: " + e.getMessage());
                                // Remettre le calcul dans la liste en cas d'erreur
                                synchronized (calculs) {
                                    calculs.add(this.c);
                                }
                                synchronized (calculsEnCours) {
                                    calculsEnCours.remove(this.c);
                                }
                            }
                        } catch (ConnectException e) {
                            synchronized (machines) {
                                try {
                                    machines.remove(serviceCalcul);
                                    System.out.println("Service de calcul retiré, Total: " + machines.size());
                                    e.printStackTrace();
                                } catch (IndexOutOfBoundsException ex) {
                                    // Index plus valide, un autre thread a déjà supprimé cette machine
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
                            System.out.println("Erreur de communication avec le service: " + remote.getMessage());
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