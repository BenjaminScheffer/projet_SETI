import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.rmi.RemoteException;
import java.time.Duration;
import raytracer.Scene;
import raytracer.Image;
import raytracer.Disp;

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

        int x0 = 0, y0 = 0;
        Instant debut = Instant.now();
        System.out.println("Calcul de l'image :\n - Coordonnées : " + x0 + "," + y0
                + "\n - Taille " + l + "x" + h);

        if (machines.size() == 0) {
            throw new RemoteException("Aucun service de calcul disponible pour effectuer le calcul.");
        }

        int indexMachine = 0;

        for (int i = 0; i < l; i = i + 50) {
            final int indexi = i;
            for (int j = 0; j < h; j = j + 50) {
                final int indexj = j;
                final int indexMachineFinal = indexMachine;
                indexMachine = (indexMachine + 1) % machines.size(); // Tourne sur les machines disponibles

                Thread t = new Thread() {
                    public void run() {
                        try {
                            ServiceCalcul serviceCalcul;
                            // Synchronisation pour éviter d'accéder à la liste des machines en même temps
                            synchronized (machines) {
                                if (indexMachineFinal >= machines.size()) {
                                    // Calcul localement si l'index n'est plus valide
                                    throw new IndexOutOfBoundsException("Service de calcul non disponible");
                                }
                                serviceCalcul = machines.get(indexMachineFinal);
                            }

                            // Appel du service de calcul pour obtenir l'image
                            Image image = serviceCalcul.calculerImage(scene, indexi, indexj, 50, 50);
                            ImageEnvoyer imageEnvoyer = new ImageEnvoyer(image, indexi, indexj);

                            try {
                                clientDisplay.recevoirImage(imageEnvoyer);
                            } catch (Exception e) {
                                System.out.println("Erreur pendant l'envoi au client: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            synchronized (machines) {
                                try {
                                    machines.remove(indexMachineFinal);
                                    System.out.println("Service de calcul retiré, Total: " + machines.size());
                                } catch (IndexOutOfBoundsException ex) {
                                    // Index plus valide, un autre thread a déjà supprimé cette machine
                                }
                            }

                            // Calcul par le service central
                            Image image = scene.compute(indexi, indexj, 50, 50);
                            System.out.println("Calcul local effectué pour les coordonnées (" + indexi + ", " + indexj + ")");
                            try {
                                ImageEnvoyer imageEnvoyer = new ImageEnvoyer(image, indexi, indexj);
                                clientDisplay.recevoirImage(imageEnvoyer);
                            } catch (Exception ex) {
                                System.out.println(
                                        "Erreur pendant l'envoi au client après calcul local: " + ex.getMessage());
                            }
                        }
                    }
                };
                t.start();
            }
        }

        Instant fin = Instant.now();
        long duree = Duration.between(debut, fin).toMillis();
        System.out.println("Image calculée en :" + duree + " ms");
    }
}