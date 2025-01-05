package fr.unice.jugementday.service;

import android.content.Context;
import android.content.SharedPreferences;

public class JsonStock {

    // Nom du fichier SharedPreferences
    private static final String PREF_NAME = "JsonStockPrefs";

    // Clés pour les données de JsonStock
    private static final String KEY_WORKS = "json_works";
    private static final String KEY_PEOPLE = "json_people";
    private static final String KEY_JUDGED = "json_judged";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructeur de la classe
    public JsonStock(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Méthode pour stocker les Works
    public void storeWorks(String works) {
        editor.putString(KEY_WORKS, works);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer les Works
    public String getWorks() {
        return sharedPreferences.getString(KEY_WORKS, null);  // Retourne null si non trouvé
    }

    // Méthode pour stocker les People
    public void storePeople(String people) {
        editor.putString(KEY_PEOPLE, people);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer les People
    public String getPeople() {
        return sharedPreferences.getString(KEY_PEOPLE, null);  // Retourne null si non trouvé
    }

    // Méthode pour effacer les données de Works
    public void clearWorks() {
        editor.remove(KEY_WORKS);
        editor.apply();
    }

    // Méthode pour effacer les données de People
    public void clearPeople() {
        editor.remove(KEY_PEOPLE);
        editor.apply();
    }

    // Méthode pour effacer toutes les données
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // Méthode pour stocker les Judged
    public void storeJudged(String judged) {
        editor.putString(KEY_JUDGED, judged);
        editor.apply();  // Appliquer les changements de manière asynchrone
    }

    // Méthode pour récupérer les Judged
    public String getJudged() {
        return sharedPreferences.getString(KEY_JUDGED, null);  // Retourne null si non trouvé
    }
}