package fr.unice.jugementday.service;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    // Nom du fichier SharedPreferences
    private static final String PREF_NAME = "UserPrefs";

    // Clés pour les données de session
    private static final String KEY_LOGIN = "user_login";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructeur de la classe
    public UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Méthode pour stocker le login
    public void storeLogin(String login) {
        editor.putString(KEY_LOGIN, login);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer le login
    public String getLogin() {
        return sharedPreferences.getString(KEY_LOGIN, null);  // Retourne null si le login n'est pas trouvé
    }

    // Méthode pour vérifier si l'utilisateur est connecté (si le login existe)
    public boolean isLoggedIn() {
        return sharedPreferences.contains(KEY_LOGIN);  // Retourne true si le login est stocké
    }

    // Méthode pour supprimer le login (déconnexion)
    public void logout() {
        editor.remove(KEY_LOGIN);  // Supprimer le login
        editor.apply();  // Appliquer les changements
    }

}
