package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.services.CPUService;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private ConcurrentLinkedDeque<DataBatch> data;
    private Cluster cluster;
    private int time = 0;
    private boolean isProcessing = false;
    private CPUService service = null;
    private int endTime = -1;
    private DataPreProcessEvent currEvent = null;


    public CPU(int cores) {
        this.cores = cores;
        cluster = Cluster.getInstance();
        data = new ConcurrentLinkedDeque<>();
    }

    //@PRE: none
    //@POST: currentData != oldData
    public void addData(DataBatch dataBatch) {
        data.addLast(dataBatch);
    }

    public void setService(CPUService service) {
        this.service = service;
    }

    public CPUService getService() {
        return service;
    }

    //@PRE: none
    //@POST: trivial
    public int getCores() {
        return cores;
    }

    //@PRE: none
    //@POST: trivial
    public ConcurrentLinkedDeque<DataBatch> getData() {
        return data;
    }

    public void updateTimee() {
        time++;
    }

    public int resetTime() {
        int ret = time;
        time = 0;
        return ret;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void process(DataPreProcessEvent e) {
        currEvent = e;
        DataBatch batch = e.getDataBatch();
        isProcessing = true;
        int complexity = 0;
        switch (batch.getData().getType()) {
            case Images: complexity = 4;
            case Text: complexity = 2;
            case Tabular: complexity = 1;
        }
        endTime = time + (32/cores)*complexity;
    }

    public DataPreProcessEvent updateTime() {
        DataPreProcessEvent ret = null;
        if (isProcessing)
            time++;
        if (time == endTime) {
            isProcessing = false;
            ret = currEvent;
            endTime = -1;
            currEvent.getDataBatch().getData().processBatch();
        }
        return ret;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void stopProcessing() {
        cluster.stopProcessingData(currEvent, resetTime(), this);
    }
}
