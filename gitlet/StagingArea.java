package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;







public class StagingArea implements Serializable {

    /** SHA-1 IDs of files pointing to blobs.*/
    private static TreeMap<String, String> filesAdd
            = new TreeMap<String, String>();
    /** SHA-1 IDs of files pointing to blobs (removed).*/
    private static TreeMap<String, String> filesRemove
            = new TreeMap<String, String>();

    public static void clearAdd() {
        filesAdd = new TreeMap<String, String>();
        clearDirectory(CommandsClass.STAGING_FOLDER);
    }

    public static void clearRemove() {
        filesRemove = new TreeMap<String, String>();
        Utils.writeObject(CommandsClass.REMOVE_STAGE, filesRemove);
    }

    public static void clear() {
        clearAdd();
        clearRemove();
    }

    /**
     * If the Blob in the CWD matches the blob in the HEAD commit
     * -- don't do anything. Accesses latest commit (HEAD) and
     * checks if it holds a blob with the same ID.
     * If not, it overwrites/adds the file to the stagingArea and
     * makes sure its not staged for removal. This method sets
     * up which methods need to be pushed to the
     * actual StagingArea folder. How do I get the currentWD?
     * @param blob
     */
    public static void addStage(Blobs blob) {
        if (filesRemove.containsKey(blob.getName())) {
            filesRemove.remove(blob.getName());
            return;
        }
        Commit lastTrack = CommandsClass.getHead().getLatestCommit();
        if (lastTrack != null) {
            if (lastTrack.getContents().containsValue(blob.getID())) {
                removeFromStage(blob.getName());
                return;
            }
        }
        Blobs temp = new Blobs(blob.getName(), blob.getContents());
        filesAdd.put(temp.getName(), temp.getID());
    }

    /**
     * Stages for removal.
     * @param blob
     */
    public static void stageForRemove(Blobs blob) {
        filesRemove.put(blob.getName(), blob.getID());
    }

    /**
     * Creates persistance for removal staged area.
     */
    public static void pushToRemove() {
        Utils.writeObject(CommandsClass.REMOVE_STAGE, filesRemove);
    }

    public static void getAllRemoved() {
        if (!CommandsClass.REMOVE_STAGE.exists()) {
            return;
        }
        TreeMap filesRemoved =
                Utils.readObject(CommandsClass.REMOVE_STAGE, TreeMap.class);
        filesRemove = (TreeMap<String, String>) filesRemoved;
    }

    /**
     * This method takes the Blobs from the filesAdd which should match
     * the CWD and puts a copy in the actual StagingArea folder.
     * Takes SHA1 and Name from filesAdd and gets the contents from
     * CWD and puts SHA1 and contents into stagingArea folder. Also pushes
     * a hashmap of fileNames -> SHA-1 IDs that are in staging
     * area to compare with parent commits.
     */
    public static void pushToStage() {
        String blobContent;
        clearDirectory(CommandsClass.STAGING_FOLDER);
        for (String name : filesAdd.keySet()) {
            File fileInStage =
                    Utils.join(CommandsClass.STAGING_FOLDER,
                            filesAdd.get(name));
            File fileInCWD = Utils.join(CommandsClass.CWD, name);
            blobContent = Utils.readContentsAsString(fileInCWD);
            Utils.writeContents(fileInStage, blobContent);
        }
        Utils.writeObject(CommandsClass.CURR_STAGE, filesAdd);
    }

    public static void getAllFiles() {
        if (!CommandsClass.CURR_STAGE.exists()) {
            return;
        }
        TreeMap filesAddition =
                Utils.readObject(CommandsClass.CURR_STAGE, TreeMap.class);
        StagingArea.filesAdd = (TreeMap<String, String>) filesAddition;
    }

    public static void clearDirectory(File file) {
        File [] allContents = file.listFiles();
        if (allContents != null) {
            for (File subFile : allContents) {
                if (subFile.isDirectory()) {
                    clearDirectory(subFile);
                }
                subFile.delete();
            }
        }
    }

    public static Boolean removeFromStage(String name) {
        String removed = filesAdd.remove(name);
        return (removed != null);
    }

    public static TreeMap<String, String> getFilesAdd() {
        return filesAdd;
    }

    public static TreeMap<String, String> getFilesRemove() {
        return filesRemove;
    }
}
