package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    /**
     * Enum representing the model's status.
     */
    public enum modelStatus {
        PreTrained, Training, Trained, Tested
    }

    /**
     * Enum representing the model's results.
     */
    public enum modelResults {
        None, Good, Bad
    }

    private String name;
    private Data data;
    private Student student;
    private modelStatus status;
    private modelResults results;
    private boolean isPublished = false;
    private int totalNumberOfBatches = -1;
    private int batchesProcessed = 0;

    public Model(String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.student = student;
        status = modelStatus.PreTrained;
        results = modelResults.None;
    }

    public Data getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public Student getStudent() {
        return student;
    }

    public void updateStatus(modelStatus status) {
        this.status = status;
    }

    public modelStatus getStatus() {
        return status;
    }

    public void setResults(boolean results) {
        if (results)
            this.results = modelResults.Good;
        else this.results = modelResults.Bad;
        status = modelStatus.Tested;
    }

    public String getDocumentation() {
        String doc = ("Model: " + name + "\n" + "Data: " + data.getTypeString() + ", " + data.getSize() + "\n");
        if (status.equals(modelStatus.Tested)) {
            if (results.equals(modelResults.Good))
                doc = doc+("Status: Tested, Results: Good" + "\n");
            else if (results.equals(modelResults.Bad))
                doc = doc+("Status: Tested, Results: Bad" + "\n");
            else doc = doc+("Status: Trained" + "\n");
        }
        else if (status.equals(modelStatus.Trained))
            doc = doc+("Status: Trained" + "\n");
        else if (status.equals(modelStatus.Training))
            doc = doc+("Status: Training" + "\n");
        else if (status.equals(modelStatus.PreTrained))
            doc = doc+("Status: Pre-Trained" + "\n");
        return doc;
    }

    public modelResults getResults() {
        return results;
    }

    public void Published() {
        isPublished = true;
    }

    public void train(DataBatch batch) {}; //"Trains" the model on a given DataBatch.

}