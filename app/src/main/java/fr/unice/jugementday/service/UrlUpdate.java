package fr.unice.jugementday.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class UrlUpdate {

    public String updateData(String table, String... options) {
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
            String finalUrl = Address.getUpdatePage() + "&" + params.toString();
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

    public String encryptToMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convertir les bytes en une chaîne hexadécimale
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashInBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            return null;
        }
    }
}
