package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.PublishResultsEvent;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private int time = 0;
    private ConcurrentLinkedDeque<Model> models;
    private ConcurrentLinkedDeque<PublishResultsEvent> events;
    private boolean isLastConference = false;

    public ConfrenceInformation (String name, int date){
        this.name = name;
        this.date = date;
        models = new ConcurrentLinkedDeque<>();
        events = new ConcurrentLinkedDeque<>();

    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public ConcurrentLinkedDeque<Model> getModels() {
        return models;
    }

    public void addModels(Model model) {
        models.addLast(model);
    }

    public ConcurrentLinkedDeque<PublishResultsEvent> getEvents() {
        return events;
    }

    public void addEvents(PublishResultsEvent event) {
        events.addLast(event);
    }

    public int getTime() {
        return time;
    }

    public void updateTime (){
        time++;
    }

    public ConcurrentLinkedDeque<Model> getPublications() {
        return models;
    }

    public String getDocumentation() {
        String doc = ("Conference: " + name + "\n" + "Publications:" + "\n");
        Iterator<Model> itr = models.iterator();
        while(itr.hasNext()) {
            doc = doc + (itr.next().getName());
            if (itr.hasNext())
                doc = (doc + ", ");
            else doc = (doc + "\n");
        }
        return (doc + "\n");
    }

    public void lastConference() {
        isLastConference = true;
    }

    public boolean isLastConference() {
        return isLastConference;
    }
}