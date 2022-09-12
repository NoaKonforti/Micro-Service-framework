package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TimeEvent;
import bgu.spl.mics.application.objects.Cluster;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static class MessageBusImplSingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private ConcurrentHashMap<MicroService, MicroServiceInfo> microServices;
	private ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedDeque<MicroService>> events;
	private ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedDeque<MicroService>> broadcasts;
	private ConcurrentHashMap<Event,Future> futureHolder;

	private MessageBusImpl() {
		 microServices = new ConcurrentHashMap<>();
		 events = new ConcurrentHashMap<>();
		 broadcasts = new ConcurrentHashMap<>();
		 futureHolder = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusImpl.MessageBusImplSingletonHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		while(events.get(type) == null)
			synchronized (events) { //Synchronizes the specific deque which is being changed.
			if (events.get(type) == null)
				events.put(type, new ConcurrentLinkedDeque<>());
			}
		synchronized (events.get(type)) { //Synchronizes the specific deque which is being changed.
			events.get(type).addLast(m);
			events.get(type).notifyAll();
		}
		synchronized (microServices.get(m)) {
			microServices.get(m).addEvent(type);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (broadcasts.get(type) == null) {
			synchronized (type) { //Synchronizes the specific deque which is being changed.
				if (broadcasts.get(type) == null) {
					broadcasts.put(type, new ConcurrentLinkedDeque<>());
					broadcasts.get(type).addLast(m);
				}
			}
		} else {
			synchronized (broadcasts.get(type)) { //Synchronizes the specific deque which is being changed.
				broadcasts.get(type).addLast(m);
			}
		}
		synchronized (microServices.get(m)) {
			microServices.get(m).addBroadcast(type);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		futureHolder.get(e).resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		Iterator<MicroService> itr = broadcasts.get(b.getClass()).iterator();
		while (itr.hasNext()) {
			MicroService m = itr.next();
			synchronized (microServices.get(m).messeges) {
				microServices.get(m).addMessage(b);
				microServices.get(m).messeges.notifyAll();
			}
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = new Future<T>();
		futureHolder.put(e,future);
		MicroService m;
		while (events.get(e.getClass()) == null) {
			synchronized (events) {
				if (events.get(e.getClass()) == null)
					events.put(e.getClass(), new ConcurrentLinkedDeque<>());
			}
		}
		synchronized (events.get(e.getClass())){
			while (events.get(e.getClass()).isEmpty())
				try {
					events.get(e.getClass()).wait();
				} catch (InterruptedException ex) {}
			m = events.get(e.getClass()).removeFirst();
			events.get(e.getClass()).addLast(m);
		}
		synchronized (microServices.get(m).messeges) {
			microServices.get(m).addMessage(e);
			microServices.get(m).messeges.notifyAll();
		}

		return future;
	}

	@Override
	public void register(MicroService m) {
		microServices.put(m,new MicroServiceInfo());
	}

	@Override
	public void unregister(MicroService m) {
		Iterator<Class<? extends Event>> itrE = microServices.get(m).events.iterator(); //Unsubsribes m from relevant events and broadcasts, while syncorizes each event\broadcast's queue.
		while (itrE.hasNext()) {
			Class<? extends Event> type = itrE.next();
			synchronized (events.get(type)) {
				events.get(type).remove(m);
			}
		}
		Iterator<Class<? extends Broadcast>> itrB = microServices.get(m).broadcasts.iterator();
		while (itrB.hasNext()) {
			Class<? extends Broadcast> type = itrB.next();
			synchronized (broadcasts.get(type)) {
				broadcasts.get(type).remove(m);
			}
		}
		synchronized (m) {
			Iterator<? extends Message> itr = microServices.get(m).messeges.iterator();
			while (!microServices.get(m).messeges.isEmpty()) {
				if (microServices.get(m).messeges.peekFirst() instanceof Event) {
					sendEvent((Event<? extends Object>) microServices.get(m).messeges.peekFirst());
				}
				microServices.get(m).messeges.removeFirst();
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message message = null;
		while(message == null) {
			synchronized (microServices.get(m).messeges) {
				if (microServices.get(m).messeges.isEmpty()) {
					try {
						microServices.get(m).messeges.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else message = microServices.get(m).messeges.removeFirst();
			}
		}
		return message;
	}

	private class MicroServiceInfo {
		private LinkedBlockingDeque<Message> messeges;
		private ConcurrentLinkedDeque<Class<? extends Event>> events;
		private ConcurrentLinkedDeque<Class<? extends Broadcast>> broadcasts;

		private MicroServiceInfo() {
			messeges = new LinkedBlockingDeque<>();
			events = new ConcurrentLinkedDeque<>();
			broadcasts = new ConcurrentLinkedDeque<>();
		}

		private void addMessage(Message m) {
			if (m.getClass() == TestModelEvent.class)
				messeges.addFirst(m);
			else messeges.addLast(m);
		}

		private void addEvent(Class<? extends Event> e) {
			events.addLast(e);
		}

		private void addBroadcast(Class<? extends Broadcast> b) {
			broadcasts.addLast(b);
		}
	}

}
