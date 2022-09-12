package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event {
    private Model model;
    private Student student;

    public TestModelEvent(Model model, Student student){
        super();
        this.model = model;
        this.student = student;}

    public Model getModel() {
        return model;
    }

    public Student getStudent() {
        return student;
    }
}