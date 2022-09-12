package bgu.spl.mics.application.objects.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import bgu.spl.mics.application.objects.*;


public class Output {
    private List<GsonStudent> students = new ArrayList<>();
    private List<GsonConference> Conferences = new ArrayList<>();
    private int cpuTimeUsed;
    private int gpuTimeUsed;
    private int batchesProcessed;

    public Output(){};

    public void setStudents(List<GsonStudent> students) {
        this.students = students;
    }

    public List<GsonStudent> getStudents() {
        return students;
    }

    public void setConferences(List<GsonConference> conferences) {
        Conferences = conferences;
    }

    public List<GsonConference> getConferences() {
        return Conferences;
    }

    public int getCpuTimeUsed() {
        return cpuTimeUsed;
    }

    public void setCpuTimeUsed(int cpuTimeUsed) {
        this.cpuTimeUsed = cpuTimeUsed;
    }

    public int getGpuTimeUsed() {
        return gpuTimeUsed;
    }

    public void setGpuTimeUsed(int gpuTimeUsed) {
        this.gpuTimeUsed = gpuTimeUsed;
    }

    public int getBatchesProcessed() {
        return batchesProcessed;
    }

    public void setBatchesProcessed(int batchesProcessed) {
        this.batchesProcessed = batchesProcessed;
    }
}
