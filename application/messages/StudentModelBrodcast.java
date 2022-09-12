package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;


public class StudentModelBrodcast implements Broadcast {
    Model model;
    private Student student;

    public StudentModelBrodcast (Student s){
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
