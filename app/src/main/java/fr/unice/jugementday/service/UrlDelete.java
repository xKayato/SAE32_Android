package fr.unice.jugementday.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UrlDelete {


    /**
     * Suppression de données dans la base de données
     * @param table la table de la base de données
     * @param options les options à envoyer
     * @return la réponse du serveur
     */
    public String deleteData(String table, String... options) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            // Construire l'URL avec les paramètres
            StringBuilder params = new StringBuilder();
            params.append("table=").append(URLEncoder.encode(table, "UTF-8"));

            // Ajouter les options dynamiques
            for (String option : options) {
                params.append("&").append(option);
            }

            // Construire l'URL finale
            String finalUrl = Address.getDeletePage() + "&" + params;
            URL url = new URL(finalUrl);

            // Connexion HTTP
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Lire la réponse du serveur
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            inputStream.close();

        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }
}
