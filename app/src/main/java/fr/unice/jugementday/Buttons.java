package fr.unice.jugementday;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class Buttons {


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
