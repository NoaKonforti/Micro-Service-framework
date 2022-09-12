package bgu.spl.mics.application.objects.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import bgu.spl.mics.application.objects.*;


public class GsonStudent {
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Student.Degree status;
    private int publications = 0;
    private int papersRead = 0;
    private List<GsonModel> trainedModels = new ArrayList<>();

    public GsonStudent (Student s){
        this.name = s.getName();
        this.department = s.getDepartment();
        this.status = s.getStatus();
//        if (s.getStatus().equals("MSc"))
//            status = "MSc";
//        else status = "PhD";
        this.publications = s.getPublications();
        this.papersRead = s.getPapersRead();
//        for (Model m: s.getModels()) {
//            if(m.getStatus().equals("Tested"))
//                models.addLast(m);
//        }
    }

    public void setTrainedModels(List<GsonModel> trainedModels) {
        this.trainedModels = trainedModels;
    }
}
