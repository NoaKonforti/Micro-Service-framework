package bgu.spl.mics.application.objects.Json;

import bgu.spl.mics.application.objects.*;


public class GsonModel {
    private String name;
    private Data data;
    private Model.modelStatus status;
    private Model.modelResults results;

    public GsonModel (Model m){
        this.name = m.getName();
        this.data = m.getData();
        this.status = m.getStatus();
        this.results = m.getResults();
    }

}
