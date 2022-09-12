package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.GPUService;
import org.junit.runners.Parameterized;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private GPUService gpuService = null;
    private int batchesProcessed = 0;
    private final int maxCapacity;
    private final int processingTime;
    private ConcurrentLinkedDeque<DataBatch> vram = new ConcurrentLinkedDeque<>();
    private int time = 0;
    private boolean isTraining = false;
    private TrainModelEvent currentEvent = null;
    private boolean isStopped = false;

    public GPU (Type type){ //constructor
        model = null;
        this.type = type;
        cluster = Cluster.getInstance();
        if (type.equals(Type.RTX3090)) {
            maxCapacity = 32;
            processingTime = 1;
        }
        else if (type.equals(Type.RTX2080)) {
            maxCapacity = 16;
            processingTime = 2;
        }
        else {
            maxCapacity = 8;
            processingTime = 4;
        }
    }

    public GPU (String type){ //constructor
        model = null;
        cluster = Cluster.getInstance();
        if (type.equals("RTX3090")) {
            this.type = Type.RTX3090;
            maxCapacity = 32;
            processingTime = 1;
        }
        else if (type.equals("RTX2080")) {
            this.type = Type.RTX2080;
            maxCapacity = 16;
            processingTime = 2;
        }
        else {
            this.type = Type.GTX1080;
            maxCapacity = 8;
            processingTime = 4;
        }
    }


    public void setModel (Model m){//setter for model
        model = m;
    }

    public Model getModel() {//getter model
        return model;
    }

    public Type getType() {
        return type;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void updateTime() {
        time++;
    }

    public int getTime() {
        return time;
    }

    public void setGpuService(GPUService service) {
        gpuService = service;
    }

    public void addBatch(DataBatch batch) {
        vram.addLast(batch);
    }

    public void preProcess(TrainModelEvent event) {
        currentEvent = event;
        model = event.getModel();
        int data = model.getData().getSize();
        int startIndex = 0;
        int numberOfBatches = data/1000;
        if (data%1000!=0)
            numberOfBatches++;
        model.getData().setTotalNumberOfBatches(numberOfBatches);
        //synchronized (cluster.getPreProcessLock()) {
            while (data >= 1000) {
                DataPreProcessEvent e = new DataPreProcessEvent(new DataBatch(model.getData(), startIndex), gpuService, numberOfBatches);
                cluster.sendPreProcessedData(e);
                startIndex = startIndex + 1000;
                data = data - 1000;
            }
            if (data > 0) {
                DataPreProcessEvent e = new DataPreProcessEvent(new DataBatch(model.getData(), startIndex), gpuService, numberOfBatches);
                cluster.sendPreProcessedData(e);
            }
        //}
    }

    private void getBatchFromDisk() {
        if (!cluster.accessDisk(gpuService).isEmpty())
            vram.addLast(cluster.accessDisk(gpuService).removeFirst());
    }

    public void startTraining(TrainModelEvent event) {
        currentEvent = event;
        currentEvent.getModel().updateStatus(Model.modelStatus.Training);
        isTraining = true;
        getBatchFromDisk();
    }

    public TrainModelEvent train() {
        TrainModelEvent ret = null;
        time++;
        if (time == processingTime) {
            vram.removeFirst();
            getBatchFromDisk();
            cluster.updateGPUTime(time);
            time = 0;
        }
        if (vram.isEmpty()) {
            currentEvent.getModel().updateStatus(Model.modelStatus.Trained);
            ret = currentEvent;
            currentEvent = null;
            isTraining = false;
        }
        return ret;
    }

    public Model testModel(Model m) {
        double successRate = 0;
        switch (m.getStudent().getStatus()) {
            case MSc: successRate = 0.6;
            case PhD: successRate = 0.8;
        }
        boolean testResults = false;
        if (Math.random() <= successRate)
            testResults = true;
        m.setResults(testResults);
        return m;
    }

    public GPUService getGpuService() {
        return gpuService;
    }

    public int getBatchesProcessed() {
        return batchesProcessed;
    }

    public boolean isTraining() {
        return isTraining;
    }

    public void stop() {
        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }
}