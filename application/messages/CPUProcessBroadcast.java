package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.services.CPUService;

public class CPUProcessBroadcast implements Broadcast {
    private CPUService cpuService;

    public CPUProcessBroadcast(CPUService cpu) {
        super();
        cpuService = cpu;
    }

    public CPUService getCpuService() {
        return cpuService;
    }
}
