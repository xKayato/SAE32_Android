package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fr.unice.jugementday.button.MenuButtons;

public class HomeActivity extends AppCompatActivity {

    private HashMap<String, Integer> oeuvres = new HashMap<String, Integer>();

    private List<ListItem> generateData() {
        List<ListItem> items = new ArrayList<>();
        oeuvres.put("ChainSawMan", R.drawable.chainsawman);
        items.add(new ListItem(getString(R.string.lastRealeseText), Arrays.asList(
                createHashMap("ChainSawMan", oeuvres.get("ChainSawMan")),
                createHashMap("TokyoGhoul", R.drawable.tokyoghoul),
                createHashMap("SpyxFamily", R.drawable.spyxfamilly),
                createHashMap("SNK", R.drawable.snk)
        )));
        items.add(new ListItem(getString(R.string.mostAppreciatedText), Arrays.asList(
                createHashMap("Settings", R.drawable.settings),
                createHashMap("Logo", R.drawable.logo)
        )));
        items.add(new ListItem(getString(R.string.leastViewedText), Arrays.asList(
                createHashMap("Settings", R.drawable.settings),
                createHashMap("Logo", R.drawable.logo)
        )));
        return items;
    }

    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);


        ListView listView = findViewById(R.id.listView);

        // Utiliser la méthode pour générer les données
        List<ListItem> items = generateData();

        CustomArrayAdapter adapter = new CustomArrayAdapter(this, items);
        listView.setAdapter(adapter);

        // Bouton pour aller sur la page profile
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));
        // Bouton pour aller sur la page Accueil
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));
        // Bouton pour aller sur la page Rechercher
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }

}