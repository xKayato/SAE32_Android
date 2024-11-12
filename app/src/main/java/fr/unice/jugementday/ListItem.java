package fr.unice.jugementday;

import java.util.List;

public class ListItem {
    private String title;
    private List<Integer> photoIds; // Liste des identifiants des photos

    public ListItem(String title, List<Integer> photoIds) {
        this.title = title;
        this.photoIds = photoIds;
    }

    public String getTitle() {
        return title;
    }

    public List<Integer> getPhotoIds() {
        return photoIds;
    }
}
