package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private Student student;


    public StudentService(String name, Student student) {
        super("Student" + student.getName() + "Service");
        this.student = student;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, (PublishConferenceBroadcast call) ->{
            student.attendConference(call.getConference());
        });

        subscribeBroadcast(StudentModelBrodcast.class, (StudentModelBrodcast call) ->{
            if (call.getStudent().equals(student)) { // only working on broadcast if it is the student's model
                if (student.getModels().peekFirst().getResults() == Model.modelResults.None && !student.stoppedWorking()) {// if next model wasn't trained yet
                    Model model = student.getModels().removeFirst();
                    student.getModels().addLast(model);
                    if (!student.stoppedWorking()) {
                        Future<Model> TrainedModelFuture = sendEvent(new TrainModelEvent(model));
                        if(!student.stoppedWorking())
                            model = TrainedModelFuture.get();
                    }
                    if (model != null && !student.stoppedWorking()) {
                        Future<Model> TestModelFuture = sendEvent(new TestModelEvent(model, student));
                        TestModelFuture.get();
                        if (model.getResults() == Model.modelResults.Good && !student.stoppedWorking()) {
                            sendEvent(new PublishResultsEvent(model));
                        }
                        if (!student.stoppedWorking())
                            sendBroadcast(new StudentModelBrodcast(student));
                    }
                }
            }
        });
        subscribeBroadcast(TerminateBroadcast.class, c -> {
            terminate();
        });
        Model m = student.getModels().removeFirst();
        student.getModels().addLast(m);
        sendBroadcast(new StudentModelBrodcast(student));
    }
}