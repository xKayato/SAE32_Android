package fr.unice.jugementday.service;

import android.content.Context;
import android.content.Intent;

import fr.unice.jugementday.HomeActivity;
import fr.unice.jugementday.ProfileActivity;
import fr.unice.jugementday.SearchActivity;

public class MenuButtons {

    // Gestion des clics sur les boutons du menu

    public static void profileClick(Context context){
        Intent i=new Intent(context, ProfileActivity.class);
        context.startActivity(i);
    }

    public static void homeClick(Context context){
        Intent i=new Intent(context, HomeActivity.class);
        context.startActivity(i);
    }

    public static void searchClick(Context context){
        Intent i=new Intent(context, SearchActivity.class);
        context.startActivity(i);
    }
}
