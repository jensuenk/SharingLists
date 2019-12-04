package com.example.sharinglists;

public class ListModel {
    public String listTitle;
    public String listDescription;

    public ListModel() {

    }

    public ListModel(String listTitle, String listDescription) {
        this.listTitle = listTitle;
        this.listDescription = listDescription;
    }

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String noteTitle) {
        this.listTitle = noteTitle;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(String listDescription) {
        this.listDescription = listDescription;
    }
}
