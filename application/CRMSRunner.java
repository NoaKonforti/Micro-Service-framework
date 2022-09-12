package bgu.spl.mics.application;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Json.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        Cluster cluster = Cluster.getInstance();
        ConcurrentLinkedDeque<Thread> threads = new ConcurrentLinkedDeque<>();

        // Reading and Extracting from JSON file
        Gson gson = new Gson();
        File Fileinput = new File(args[0]) ;/////!!!!
        Input input = new Input();
        try{
            JsonElement element = JsonParser.parseReader(new FileReader(Fileinput.getPath()));
            // JsonElement element = JsonParser.parseReader(new FileReader(args[0]));
            JsonObject object = element.getAsJsonObject();
            // Extracting Basic Fields
            input.setTickTime(object.get("TickTime").getAsInt());
            input.setDuration(object.get("Duration").getAsInt());

            // Extracting Students
            JsonArray JsonArrayStudents = object.get("Students").getAsJsonArray();
            ConcurrentLinkedDeque<Student> Students = new ConcurrentLinkedDeque<>();

            for (JsonElement e : JsonArrayStudents) {
                // Get the JsonObject
                JsonObject objectStudent = e.getAsJsonObject();
                String name = objectStudent.get("name").getAsString();
                String department = objectStudent.get("department").getAsString();
                String status = objectStudent.get("status").getAsString(); ///// to create a builder in Student with string to ENUM!!!!!!!!!???!?!
                JsonArray JsonArrayModels = objectStudent.get("models").getAsJsonArray();
                ConcurrentLinkedDeque<Model> models = new ConcurrentLinkedDeque<>();
                Student student = new Student(name, department, status);

                for (JsonElement ModelElement : JsonArrayModels){
                    JsonObject objectModel = ModelElement.getAsJsonObject();
                    String nameModel =  objectModel.get("name").getAsString();
                    String typeData =  objectModel.get("type").getAsString();///// to create a builder in Data with ENUM
                    int size = objectModel.get("size").getAsInt();
                    Data data = new Data(typeData, size);
                    Model model = new Model(nameModel, data, student);
                    models.addLast(model);
                }
                student.setModels(models);
                Students.addLast(student);
            }
            input.setStudents(Students);

            //Extracting GPUS
            JsonArray JsonArrayGPUS = object.get("GPUS").getAsJsonArray();
            ConcurrentLinkedDeque<GPU> GPUS = new ConcurrentLinkedDeque<>();

            for(int i=0; i< JsonArrayGPUS.size(); i++){
                String type = JsonArrayGPUS.get(i).getAsString();
                GPU Gpu = new GPU(type);
                GPUS.addLast(Gpu);
            }
            input.setGPUS(GPUS);

            //Extracting CPUS
            JsonArray JsonArrayCPUS = object.get("CPUS").getAsJsonArray();
            //ConcurrentLinkedDeque<CPU> CPUS = new ConcurrentLinkedDeque<>();
            ArrayList<CPU> CPUSArray = new ArrayList<>();
            for(int i=0; i< JsonArrayCPUS.size(); i++){
                int cores = JsonArrayCPUS.get(i).getAsInt();
                CPU Cpu = new CPU(cores);
                CPUSArray.add(Cpu);
            }
            Collections.sort(CPUSArray, new CPUCompare());

            ConcurrentLinkedDeque<CPU> CPUS = new ConcurrentLinkedDeque<>();
            for (int i = 0; i < CPUSArray.size(); i++)
                CPUS.addLast(CPUSArray.get(i));
            input.setCPUS(CPUS);
            //Extracting Conferences
            JsonArray JsonArrayConference = object.get("Conferences").getAsJsonArray();
            ConcurrentLinkedDeque<ConfrenceInformation> Conferences = new ConcurrentLinkedDeque<>();

            for (JsonElement e : JsonArrayConference) {
                // Get the JsonObject
                JsonObject objectConference = e.getAsJsonObject();
                String name = objectConference.get("name").getAsString();
                int date = objectConference.get("date").getAsInt();
                ConfrenceInformation conference = new ConfrenceInformation(name, date);
                Conferences.addLast(conference);
            }
            input.setConferences(Conferences);

        } catch (IOException e) {
            e.printStackTrace();
        }

        LinkedList<Thread> join = new LinkedList<>();
        // Constructing MicroServices

        LinkedList<Thread> students = new LinkedList<>();
        LinkedList<Thread> cpus = new LinkedList<>();
        LinkedList<Thread> gpus = new LinkedList<>();


        for (Student s : input.getStudents()) {
            Thread student = new Thread (new StudentService(s.getName(), s));
            threads.addLast(student);
            students.addLast(student);
            student.setPriority(Thread.MAX_PRIORITY);
        }

        for (ConfrenceInformation c : input.getConferences()) {
            Thread Conference = new Thread (new ConferenceService(c.getName(), c));
            threads.addLast(Conference);
            join.addFirst(Conference);
            Conference.setPriority(Thread.MAX_PRIORITY);
        }
        for (CPU c : input.getCPUS()) {
            CPUService service = new CPUService(" ", c);
            c.setService(service);
            Thread CPU = new Thread (service);
            threads.addLast(CPU);
            cpus.addLast(CPU);
            if (c.getCores()>16)
                CPU.setPriority(Thread.MAX_PRIORITY);
        }

        for (GPU g : input.getGPUS()) {
            GPUService gpuService = new GPUService(" ", g);
            g.setGpuService(gpuService);
            Thread GPU = new Thread (gpuService);
            threads.addLast(GPU);
            gpus.addLast(GPU);
            GPU.setPriority(Thread.MAX_PRIORITY);
        }



        TimeService timeService = new TimeService(input.getTickTime(), input.getDuration());
        Thread TimeService = new Thread (timeService);

        cluster.setCPUS(input.getCPUS());
        cluster.setGPUS(input.getGPUS());

        for (Thread t : threads) {
            t.start();
        }

        TimeService.start();
        try {
            for (Thread t: join) {
                t.join();
            }
            TimeService.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cluster.terminate();
        synchronized (cluster.getPreProcessLock()) {
            cluster.getPreProcessLock().notifyAll();
        }

        for (Student s: input.getStudents()) {
            s.stopWorking();
        }

        try {
            for (Thread t: gpus) {
                t.interrupt();
            }
            for (Thread t: students) {
                if (t.getState().equals(Thread.State.WAITING) || t.getState().equals(Thread.State.BLOCKED))
                    t.interrupt();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        try {
            for (Thread t: cpus) {
                t.join();
            }
            for (Thread t: students) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Output output = new Output();
        List<GsonStudent> S = new ArrayList<>();
        for (Student s: input.getStudents()) {
            GsonStudent student = new GsonStudent(s);
            List<GsonModel> M = new ArrayList<>();
            for (Model m: s.getModels()) {
                if(m.getStatus().equals(Model.modelStatus.Tested)) {
                    GsonModel mod = new GsonModel(m);
                    M.add(mod);
                }
            }
            student.setTrainedModels(M);
            S.add(student);
        }
        output.setStudents(S);
        List<GsonConference> C = new ArrayList<>();
        for (ConfrenceInformation c: input.getConferences()) {
            GsonConference conference = new GsonConference(c);
            List<GsonModel> M = new ArrayList<>();
            for (Model m: c.getModels()) {
                GsonModel mod = new GsonModel(m);
                M.add(mod);
            }
            conference.setPublications(M);
            C.add(conference);
        }
        output.setConferences(C);

        output.setGpuTimeUsed(cluster.getStatistics().getGPUTime());
        output.setBatchesProcessed(cluster.getStatistics().getBatchesProcessed());
        output.setCpuTimeUsed(cluster.getStatistics().getCPUTime());


        Gson jsonOutput = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter("Output.JSON")){
            jsonOutput.toJson(output,writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Output.JSON File was successfully created.");

        try {
            for (Thread t: threads) {
                t.stop();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}

