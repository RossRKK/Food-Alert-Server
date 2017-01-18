/**
 * 
 */
package server.database;

/**
 * @author rossrkk
 */
public class Record {
    private int[] data;
    private String name;

    /**
     * Create a new blank record
     */
    public Record() {
        data = new int[DatabaseManager.fieldNames.length];
    }

    /**
     * Set the value of a specific field by index
     * 
     * @param i
     *            The index of the field
     * @param value
     *            The value that field should adopt
     */
    public void setData(int i, int value) {
        data[i] = value;
    }

    /**
     * Set the value of a specific field by fieldName
     * 
     * @param fieldName
     *            The name of the field ou want to set
     * @param value
     *            The value that field should adopt
     */
    public void setData(String fieldName, int value) {
        int i;
        for (i = 0; i < DatabaseManager.fieldNames.length; i++) {
            if (DatabaseManager.fieldNames[i].equals(fieldName)) {
                break;
            }
        }
        data[i] = value;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
