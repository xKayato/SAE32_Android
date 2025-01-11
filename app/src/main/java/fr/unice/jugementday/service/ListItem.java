package fr.unice.jugementday.service;
import java.util.List;
import java.util.HashMap;
public class ListItem {
    private String title;
    private List<HashMap<String, Integer>> works; // Liste des identifiants des photos et noms
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