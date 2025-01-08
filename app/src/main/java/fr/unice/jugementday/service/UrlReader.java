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
    private static final String PASSID = "&passid=SalutJeSuisUnMotDePassePourGet";  // Le passid constant
    public static String address = "http://10.3.122.146/getdata.php";

    // Constructeur : initialise le gestionnaire de threads
    public UrlReader() {
        this.executorService = Executors.newSingleThreadExecutor(); // Lancement d'un thread
    }

    public String fetchData(String urlString) {
        URL url;
        urlString += PASSID;  // Ajout du passid

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
            return e.getMessage(); // Afficher l'erreur
        }
    }

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
            shutdown();
            return result.toString();
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public String buildUrl(String baseUrl, String table, String[] options) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?table=").append(table);

        for (String option : options) {
            urlBuilder.append("&").append(option);
        }

        return urlBuilder.toString();
    }
}
