package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private List<ListItem> generateData() {
        List<ListItem> items = new ArrayList<>();
        items.add(new ListItem(getString(R.string.lastRealeseText), Arrays.asList(R.drawable.chainsawman, R.drawable.tokyoghoul, R.drawable.spyxfamilly, R.drawable.snk)));
        items.add(new ListItem(getString(R.string.mostAppreciatedText), Arrays.asList(R.drawable.settings, R.drawable.logo)));
        items.add(new ListItem(getString(R.string.leastViewedText), Arrays.asList(R.drawable.settings, R.drawable.logo)));
        return items;
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


        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Buttons.profileClick(HomeActivity.this);
            }
        });

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Buttons.homeClick(HomeActivity.this);
            }
        });

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Buttons.searchClick(HomeActivity.this);
            }
        });




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

}