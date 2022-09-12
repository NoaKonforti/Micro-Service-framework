package bgu.spl.mics.application.objects;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications = 0;
    private int papersRead = 0;
    private ConcurrentLinkedDeque<Model> models;
    private boolean stoppedWorking = false;

    public Student(String name, String department, Degree status, ConcurrentLinkedDeque<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.models = models;
    }

    public Student(String name, String department, String degree) {
        this.name = name;
        this.department = department;
        if (degree.equals("MSc"))
            status = Degree.MSc;
        else status = Degree.PhD;
    }

    public void setModels(ConcurrentLinkedDeque<Model> models) {
        this.models = models;
    }

    public void addPublication() {
        publications++;
    }

    public void readPaper() {
        papersRead++;
    }

    public String getName() { return name; }
    public String getDepartment() { return department; }
    public Degree getStatus() { return status; }
    public int getPublications() { return publications; }
    public int getPapersRead() { return papersRead; }

    public ConcurrentLinkedDeque<Model> getModels() {
        return models;
    }

    public void attendConference (ConfrenceInformation conference){
        for (Model m : conference.getModels()){
            if (m.getStudent().equals(this)){ // student was the one to publish model m
                this.addPublication();
            }
            else{ // student read another student's paper
                this.readPaper();
            }
        }
    }
    public void stopWorking() {
        stoppedWorking = true;
    }

    public boolean stoppedWorking() {
        return stoppedWorking;
    }

    public String getDocumentation() {
        String doc = ("Student: " + name + "\n" +"Number of Publications: " + publications + "Papers Read: " + papersRead + "Trained Models:" + "\n");
        String unTrained = ("Un-Trained Models: " + "\n");
        Iterator<Model> itr = models.iterator();
        while (itr.hasNext()) {
            Model m = itr.next();
            if (m.getResults().equals(Model.modelResults.None))
                unTrained = unTrained + m.getDocumentation();
            else doc = doc + m.getDocumentation();
        }
        if (!unTrained.equals("Un-Trained Models: " + "\n"))
            doc = doc + unTrained;
        return (doc + "\n");
    }
}