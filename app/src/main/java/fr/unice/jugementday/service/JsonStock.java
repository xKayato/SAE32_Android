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

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public JsonStock(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setWorks(String works) {
        editor.putString(KEY_WORKS, works);
        editor.apply();
    }

    public String getWorks() {
        return sharedPreferences.getString(KEY_WORKS, null);
    }

    public void setPeople(String people) {
        editor.putString(KEY_PEOPLE, people);
        editor.apply();
    }

    public String getPeople() {
        return sharedPreferences.getString(KEY_PEOPLE, null);
    }

    public void setJudged(String judged) {
        editor.putString(KEY_JUDGED, judged);
        editor.apply();
    }

    public String getJudged() {
        return sharedPreferences.getString(KEY_JUDGED, null);
    }
}
