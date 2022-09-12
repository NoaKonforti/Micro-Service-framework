package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;
    private int totalNumberOfBatches;

    public Data(Type type, int size) {
        this.type = type;
        this.size = size;
        processed = 0;
    }

    public Data(String t, int s) {
        size = s;
        if (t.equals("Images"))
            type = Type.Images;
        else if (t.equals("Text"))
            type = Type.Text;
        else type = Type.Tabular;
    }

    public void process(int samples) {
        processed = processed + samples;
    }

    public int getSize() {
        return size;
    }

    public int getProcessed() {
        return processed;
    }

    public Type getType() {
        return type;
    }

    public String getTypeString() {
        String ret = "Tabular";
        if (type.equals(Type.Images))
            ret = "Images";
        else if (type.equals(Type.Text))
            ret = "Text";
        return ret;
    }

    public void setTotalNumberOfBatches(int batches) {
        totalNumberOfBatches = batches;
    }

    public void processBatch() {
        processed++;
    }

    public boolean isDone() {
        return (processed==totalNumberOfBatches);
    }
}
