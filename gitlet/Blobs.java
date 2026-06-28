package gitlet;
import java.io.File;
import java.io.Serializable;


public class Blobs implements Serializable {

    /** Contents of Blob. */
    private String fileContents;

    /** ID of Blob. */
    private String id;

    /** Name of file. */
    private String fileName;


    public Blobs(String name, String contents) {
        fileContents = contents;
        fileName = name;
        id = Utils.sha1(fileName + fileContents);
    }

    /**
     * Creates a blob of the file in the working directory from its name.
     * @param name
     */
    public Blobs(String name) {
        File currFile = Utils.join(CommandsClass.CWD, name);
        if (!currFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        fileContents = Utils.readContentsAsString(currFile);
        fileName = name;
        id = Utils.sha1(fileName + fileContents);
    }

    public String getID() {
        return id;
    }

    public String getContents() {
        return fileContents;
    }

    public String getName() {
        return fileName;
    }


}
