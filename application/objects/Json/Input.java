package bgu.spl.mics.application.objects.Json;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import bgu.spl.mics.application.objects.*;


public class Input {
    private ConcurrentLinkedDeque<Student> Students;
    private ConcurrentLinkedDeque<GPU> GPUS;
    private ConcurrentLinkedDeque<CPU> CPUS;
    private ConcurrentLinkedDeque<ConfrenceInformation> Conferences;
    private int TickTime;
    private int Duration;

    public Input (){}


    public ConcurrentLinkedDeque<ConfrenceInformation> getConferences() {
        return Conferences;
    }

    public void setConferences(ConcurrentLinkedDeque<ConfrenceInformation> conferences) {
        Conferences = conferences;
    }

    public ConcurrentLinkedDeque<CPU> getCPUS() {
        return CPUS;
    }

    public void setCPUS(ConcurrentLinkedDeque<CPU> CPUS) {
        this.CPUS = CPUS;
    }

    public ConcurrentLinkedDeque<GPU> getGPUS() {
        return GPUS;
    }

    public void setGPUS(ConcurrentLinkedDeque<GPU> GPUS) {
        this.GPUS = GPUS;
    }

    public ConcurrentLinkedDeque<Student> getStudents() {
        return Students;
    }

    public void setStudents(ConcurrentLinkedDeque<Student> students) {
        Students = students;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public int getTickTime() {
        return TickTime;
    }

    public void setTickTime(int tickTime) {
        TickTime = tickTime;
    }
}