package fr.unice.jugementday.service;
import java.util.List;
import java.util.HashMap;
public class ListItem {
    private final String title;
    private final List<HashMap<String, Integer>> works; // Liste des identifiants des id et noms
    public ListItem(String title, List<HashMap<String, Integer>> works) {
        this.title = title;
        this.works = works;
    }
    public String getTitle() {
        return title;
    }
    public List<HashMap<String, Integer>> getWorks() {
        return works;
    }
}