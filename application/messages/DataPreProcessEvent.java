package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.services.GPUService;

public class DataPreProcessEvent implements Event {
    private DataBatch dataBatch;
    private GPUService relevantGPU;
    private int totalNumOfBatches;

    public DataPreProcessEvent(DataBatch data, GPUService gpu, int n) {
        super();
        dataBatch = data;
        relevantGPU = gpu;
        totalNumOfBatches = n;
    }

    public DataBatch getDataBatch() {
        return dataBatch;
    }

    public GPUService getRelevantGPU() {
        return relevantGPU;
    }

    public int getTotalNumOfBatches() {
        return totalNumOfBatches;
    }
}
