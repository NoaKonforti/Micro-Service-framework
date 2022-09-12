package bgu.spl.mics.application.objects.Json;

import java.util.ArrayList;
import java.util.List;
import bgu.spl.mics.application.objects.*;


public class GsonConference {
    private String name;
    private int date;
    private List<GsonModel> publications = new ArrayList<>();

    public  GsonConference (ConfrenceInformation c){
        this.name = c.getName();
        this.date = c.getDate();
    }

    public void setPublications(List<GsonModel> publications) {
        this.publications = publications;
    }
}
