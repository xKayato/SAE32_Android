package fr.unice.jugementday.service;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UrlReader {

    // Variable pour accéder au composant où l'on saisit l'URL
    // sous la forme http://...
    private String inputURL;
    // Variable pour accéder au composant qui affiche le texte
    // trouvé à l'URL donné dans inputURL
    private String output;
    // La variable qui va permettre de gérer les threads
    private ExecutorService exe;
    // Et celle qui représente la thread que l'on va utiliser
    private Future<String> todo;

    // Fonction associée au click sur le bouton
    // Pour rappel, v est l'objet graphique cliqué
    public void read(String s) {
        URL u;
        // On récupère l'url saisie

        // On crée un objet de type URL correspondant
        // Pas de traitement spécial de l'erreur dans le cas
        // ou l'utilisateur a saisi n'importe quoi
        try {
            u = new URL(s);
        } catch (MalformedURLException e) {
            // Ce n'est qu'un exemple, pas de traitement propre de l'exception
            e.printStackTrace();
            u = null;
        }

        // On crée l'objet qui va gérer la thread
        exe = Executors.newSingleThreadExecutor();
        // On lance la thread
        todo = lireURL(u);
        // On attend le résultat
        try {
            s = todo.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // On affiche le résultat

    }

    public Future<String> lireURL(URL u) {
        return exe.submit(() -> {
            URLConnection c;
            String inputline;
            StringBuilder codeHTML = new StringBuilder("");

            try {
                c = u.openConnection();
                //temps maximun alloué pour se connecter
                c.setConnectTimeout(60000);
                //temps maximun alloué pour lire
                c.setReadTimeout(10000);
                //flux de lecture avec l'encodage des caractères UTF-8
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
                while ((inputline = in.readLine()) != null) {
                    //concaténation+retour à la ligne avec \n
                    codeHTML.append(inputline + "\n");
                }
                //il faut bien fermer le flux de lecture
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return codeHTML.toString();
        });
    }
}
