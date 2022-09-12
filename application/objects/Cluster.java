package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.services.GPUService;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static class ClusterSingletonHolder {
		private static Cluster instance = new Cluster();
	}

	private ConcurrentLinkedDeque<GPU> GPUS;
	private ConcurrentLinkedDeque<CPU> CPUS;
	private Statistics statistics;
	private ConcurrentLinkedDeque<DataPreProcessEvent> toProcess;
	private ConcurrentHashMap<GPUService,ConcurrentLinkedDeque<DataBatch>> disk;
	private ConcurrentLinkedDeque<DataPreProcessEvent> preprocessQueue;
	private Object preProcessLock = new Object();
	private boolean terminated = false;


	private Cluster() {
		statistics = new Statistics();
		preprocessQueue = new ConcurrentLinkedDeque<>();
		disk = new ConcurrentHashMap<>();
		toProcess = new ConcurrentLinkedDeque<>();
	}

	public void setGPUS(ConcurrentLinkedDeque<GPU> gpus) {
		GPUS = gpus;
		for (GPU gpu: gpus) {
			disk.put(gpu.getGpuService(), new ConcurrentLinkedDeque<>());
		}
	}

	public void setCPUS(ConcurrentLinkedDeque<CPU> cpus) {
		CPUS = cpus;
	}

	public ConcurrentLinkedDeque<DataPreProcessEvent> getPreprocessQueue() {
		return preprocessQueue;
	}

	public Object getPreProcessLock() {
		return preProcessLock;
	}

	//Retrieves the single instance of this class
	public static Cluster getInstance() {
		return ClusterSingletonHolder.instance;
	}

	public void sendPreProcessedData(DataPreProcessEvent e) {
		synchronized (preprocessQueue) {
			preprocessQueue.addLast(e);
		}
		synchronized (preProcessLock) {
			preProcessLock.notifyAll();
		}
	}

	public DataPreProcessEvent awaitData(CPU cpu) {
		if (preprocessQueue.isEmpty()) {
			synchronized (preProcessLock) {
				try {
					preProcessLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					if (terminated)
						cpu.stopProcessing();
				}
			}
		}
		synchronized (preprocessQueue) {
			if (preprocessQueue.isEmpty())
				return null;
			else return preprocessQueue.removeFirst();
		}
	}


	public void completeProcessingData(DataPreProcessEvent e, int time, CPU cpu) {
		synchronized (disk.get(e.getRelevantGPU())) {
			disk.get(e.getRelevantGPU()).addLast(e.getDataBatch());
		}
		if (e.getDataBatch().getData().isDone())
			synchronized (e.getRelevantGPU().getLock()) {
				e.getRelevantGPU().getLock().notifyAll();
			}
		statistics.updateCPUTime(time);
		statistics.updateBatchesProcessed(1);
	}

	public void stopProcessingData(DataPreProcessEvent e, int time, CPU cpu) {
		synchronized (e.getRelevantGPU().getLock()) {
			e.getRelevantGPU().getLock().notifyAll();
		}
		statistics.updateCPUTime(time);
		e.getRelevantGPU().getGpu().stop();
		e.getRelevantGPU().getGpu().getModel().getStudent().stopWorking();
	}

	public void terminate() {
		terminated = true;
	}

	public void updateGPUTime(int time) {
		statistics.updateGPUTime(time);
	}

	public ConcurrentLinkedDeque<DataBatch> accessDisk(GPUService gpu) {
		return disk.get(gpu);
	}

	public final Statistics getStatistics() {
		return statistics;
	}


	public class Statistics{
		private LinkedList<String> models;
		private int batchesProcessed;
		private int CPUTime;
		private int GPUTime;

		private Statistics() {
			models = new LinkedList<String>();
			batchesProcessed = 0;
			CPUTime = 0;
			GPUTime = 0;
		}

		private void addModel(String name) {
			models.addLast(name);
		}

		private void updateBatchesProcessed(int batches) {
			batchesProcessed = batchesProcessed + batches;
		}

		private void updateCPUTime(int time) {
			CPUTime = CPUTime + time;
		}

		private void updateGPUTime(int time) {
			GPUTime = GPUTime + time;
		}

		public LinkedList<String> getModels() {
			return models;
		}

		public int getBatchesProcessed() {
			return batchesProcessed;
		}

		public int getCPUTime() {
			return CPUTime;
		}

		public int getGPUTime() {
			return GPUTime;
		}
	}

	public void addModelStatistics(String name) {
		statistics.addModel(name);
	}

	public String getDocumentation() {
		String doc = ("Total CPU time used: " + statistics.getCPUTime() + "\n" + "Total GPU time used: " + statistics.getGPUTime() + "\n" + "Amount of batches processed by the CPUs: " + statistics.getBatchesProcessed());
		return doc;
	}

	public boolean isTerminated() {
		return terminated;
	}
}
