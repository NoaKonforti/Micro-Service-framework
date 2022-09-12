package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;


public class StudentModelEvent implements Event {
    Model model;
    private Student student;

    public StudentModelEvent (Student s){
        super();
        student = s;
    }

    public Model getModel() {
        return model;
    }

    public Student getStudent() {
        return student;
    }
}
