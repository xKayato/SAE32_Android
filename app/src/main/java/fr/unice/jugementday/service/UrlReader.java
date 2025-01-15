package fr.unice.jugementday.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UrlReader {

    private final ExecutorService executorService;

    /**
     * Constructeur
     */
    public UrlReader() {
        this.executorService = Executors.newSingleThreadExecutor(); // Lancement d'un thread
    }

    /**
     * Récupère les données d'une URL
     * @param options les options à envoyer
     * @return les données récupérées
     */
    public String fetchData(String options) {
        URL url;
        String urlString = Address.getGetPage() + options;
        // Validation de l'URL
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return "Erreur : URL malformée.";
        }
        // Création d'une tâche pour récupérer les données
        Future<String> future = readFromUrl(url);

        // Attente et récupération du résultat
        try {
            return future.get();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Récupère les données d'une URL
     * @param url l'URL à lire
     * @return les données récupérées
     */
    private Future<String> readFromUrl(URL url) {
        return executorService.submit(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(60000); // Timeout pour la connexion
                connection.setReadTimeout(10000);   // Timeout pour la lecture

                // Lecture des données
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                return e.getMessage(); // Afficher l'erreur
            }
            executorService.shutdown();
            return result.toString();
        });
    }
}
