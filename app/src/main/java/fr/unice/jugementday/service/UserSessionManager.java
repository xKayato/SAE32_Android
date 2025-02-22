package fr.unice.jugementday.service;


import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    // Nom du fichier SharedPreferences
    private static final String PREF_NAME = "UserPrefs";

    // Clés pour les données de session
    private static final String KEY_LOGIN = "user_login";
    private static final String KEY_ACCESS = "user_access";
    private static final String KEY_ENCRYPTED_PASSWORD = "user_password";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // Constructeur de la classe
    public UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Méthode pour stocker le login
    public void setLogin(String login) {
        editor.putString(KEY_LOGIN, login);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer le login
    public String getLogin() {
        return sharedPreferences.getString(KEY_LOGIN, null);  // Retourne null si le login n'est pas trouvé
    }


    // Méthode pour stocker le login
    public void setPassword(String password) {
        editor.putString(KEY_ENCRYPTED_PASSWORD, password);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer le login
    public String getPassword() {
        return sharedPreferences.getString(KEY_ENCRYPTED_PASSWORD, null);  // Retourne null si le login n'est pas trouvé
    }

    // Méthode pour stocker l'acces
    public void setAccess(String access) {
        editor.putString(KEY_ACCESS, access);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer l'acces
    public String getAccess() {
        return sharedPreferences.getString(KEY_ACCESS, null);  // Retourne null si le login n'est pas trouvé
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
