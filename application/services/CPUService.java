package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.CPU;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(CPUProcessBroadcast.class, c -> {
            if (c.getCpuService().equals(this)) {
                if (!cpu.isProcessing()) {
                    DataPreProcessEvent e = cpu.getCluster().awaitData(cpu);
                    if (e != null)
                        cpu.process(e);
                    sendBroadcast(new CPUProcessBroadcast(this));
                }
            }
        });
        subscribeBroadcast(TickBroadcast.class, c -> {
            DataPreProcessEvent e = cpu.updateTime();
            if (e != null) {
                cpu.getCluster().completeProcessingData(e,cpu.resetTime(), cpu);
                sendBroadcast(new CPUProcessBroadcast(this));
            }
        });
        subscribeBroadcast(TerminateBroadcast.class, c -> {
            terminate();
            if (cpu.isProcessing())
                cpu.stopProcessing();
        });
        sendBroadcast(new CPUProcessBroadcast(this));
    }
}
