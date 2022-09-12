package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;
    private Object lock = new Object();
    private ConcurrentLinkedDeque<TrainModelEvent> delayedTrainModels = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<TestModelEvent> delayedTestModels = new ConcurrentLinkedDeque<>();

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.gpu.setGpuService(this);
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, c -> {
            TrainModelEvent e = null;
            if (gpu.isTraining())
                e = gpu.train();
            if (e!=null) {
                complete(e, e.getModel());
                while (!delayedTestModels.isEmpty()) {
                    TestModelEvent testEvent = delayedTestModels.removeFirst();
                    complete(testEvent, gpu.testModel(testEvent.getModel()));
                }
                if (!delayedTrainModels.isEmpty() && !gpu.getCluster().isTerminated()) {
                    TrainModelEvent trainEvent = delayedTrainModels.removeFirst();
                    gpu.preProcess(trainEvent);
                    synchronized (lock) {
                        try { //Waits until all batches are processed (the last one will use notifyall).
                            lock.wait();
                        } catch (InterruptedException ex) {}
                    }
                    if (gpu.isStopped()) {
                        terminate();
                        complete(trainEvent, null);
                    }
                        //Starts training the model
                    else gpu.startTraining(trainEvent);
                }
            }
        });
        subscribeEvent(TrainModelEvent.class, c -> {
            if (!gpu.isTraining()) {
                gpu.preProcess(c);
                if (!gpu.getCluster().isTerminated()) {
                    synchronized (lock) {
                        try { //Waits until all batches are processed (the last one will use notifyall).
                            lock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (gpu.isStopped()) {
                        terminate();
                        complete(c, null);
                        while (!delayedTestModels.isEmpty()) {
                            TestModelEvent testEvent = delayedTestModels.removeFirst();
                            complete(testEvent, gpu.testModel(testEvent.getModel()));
                        }
                    }
                    //Starts training the model
                    else gpu.startTraining(c);
                } else delayedTrainModels.addLast(c);
                //else sendEvent(c);
            }
        });
        subscribeEvent(TestModelEvent.class, c -> {
            if (!gpu.isTraining())
                complete(c, gpu.testModel(c.getModel()));
            else delayedTestModels.addLast(c);
            //else sendEvent(c);
        });
        subscribeBroadcast(TerminateBroadcast.class, c -> {
            terminate();
            while (!delayedTestModels.isEmpty()) {
                TestModelEvent testEvent = delayedTestModels.removeFirst();
                complete(testEvent, gpu.testModel(testEvent.getModel()));
            }
        });
    }

    public void requestBatch(){
        if (!gpu.getCluster().accessDisk(this).isEmpty())
            gpu.addBatch(gpu.getCluster().accessDisk(this).removeFirst());
    }

    public GPU getGpu() {
        return gpu;
    }

    public Object getLock() {
        return lock;
    }
}
