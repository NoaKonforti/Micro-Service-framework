package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConfrenceInformation conference;

    public ConferenceService(String name, ConfrenceInformation conference) {
        super(name);

        this.conference = conference;
    }

    @Override
    protected void initialize() {

        subscribeEvent(PublishResultsEvent.class, (PublishResultsEvent call) -> {
            conference.addModels(call.getModel());
            conference.addEvents(call);
            complete(call, call.getModel());
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast call) ->{
            conference.updateTime();
            if (conference.getTime() >= conference.getDate()){
                this.terminate();
                sendBroadcast( new PublishConferenceBroadcast(conference));
                for (PublishResultsEvent e : conference.getEvents()){ // resolve all relevant futures
                    e.getModel().Published();

                }
            }
        }) ;
        subscribeBroadcast(TerminateBroadcast.class, c -> {
            terminate();
        });
    }
}