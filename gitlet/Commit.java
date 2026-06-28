package gitlet;
import java.io.File;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
public class Commit implements Serializable {

    /** Commit Message. */
    private String logMessage;
    /** Time Commit was created. */
    private String timeCreated;
    /** Blob Contents of each Commit (name -> SHA1). */
    private TreeMap<String, String> contents;
    /** First parent. */
    private String parent1;
    /** Second parent. */
    private String parent2;
    /** Commit ID. */
    private String iD;
    public Commit(String message, String parent1Passed,
                  TreeMap<String, String> currContents) {
        this.logMessage = message;
        this.parent1 = parent1Passed;
        this.contents = currContents;
        if (parent1 == null) {
            Date date = new Date(0);
            Format x = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            timeCreated = x.format(date);
        } else {
            Date date = new Date();
            Format x = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            timeCreated = x.format(date);
            update();
            untrack();
        }
        iD = Utils.sha1(logMessage + parent1
                + contents.keySet() + timeCreated);
    }
    public Commit(String message, String parent1Passed,
                   String parent2Passed, TreeMap<String, String> currContents) {
        this.logMessage = message;
        this.parent1 = parent1Passed;
        this.contents = currContents;
        update();
        untrack();
        this.parent2 = parent2Passed;
        Date date = new Date();
        Format x = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        timeCreated = x.format(date);
        iD = Utils.sha1(logMessage + parent1
                + parent2 + contents.keySet() + timeCreated);
    }

    public String getMessage() {
        return logMessage;
    }

    public String getFirstParent() {
        return parent1;
    }

    public String getSecondParent() {
        return parent2;
    }

    public  TreeMap<String, String> getContents() {
        return contents;
    }

    public String getID() {
        return iD;
    }


    public String getDate() {
        return timeCreated;
    }

    public String getSpecificBlobID(String fileName) {
        String spec = contents.get(fileName);
        if (spec == null) {
            return "";
        }
        return spec;
    }

    /** What am I saving in the actual folder in .gitlet?
     * I am saving a folder of the current working directory
     * that holds all the file names and contents of every file.
     * Will have to go through every blob. We are not
     * pushing the timestamp and log message here.
     */
    public void pushInit() {
        CommandsClass.GITLET_FOLDER.mkdir();
        CommandsClass.STAGING_FOLDER.mkdir();
        CommandsClass.BRANCHES.mkdir();
        CommandsClass.ALL_FILES.mkdir();
        CommandsClass.COMMIT_FILE.mkdir();
        File init = new File(CommandsClass.COMMIT_FILE, this.getID());
        Utils.writeObject(init, this);
    }

    /**
     * Updates a commit object with the Staging Area files.
     * First goes through and updates all files that already exist
     * in commit. Then adds files to commit that weren't in parent.
     */
    public void update() {
        for (String stagedName : StagingArea.getFilesAdd().keySet()) {
            String stagedSHA = StagingArea.getFilesAdd().get(stagedName);
            addToContents(stagedName, stagedSHA);
        }
    }
    /**
     * Untracks files that had been staged for removal.
     */
    public void untrack() {
        for (String stagedName : StagingArea.getFilesRemove().keySet()) {
            contents.remove(stagedName);
        }
    }
    /**
     * Need to push staging Area contents to the
     * files folder and push the commit to a file.
     */
    public void pushCommit() {
        File currBlob;
        File stagedBlob;
        String currContents;
        for (String stagedName: StagingArea.getFilesAdd().keySet()) {
            stagedBlob = Utils.join(CommandsClass.STAGING_FOLDER,
                    StagingArea.getFilesAdd().get(stagedName));
            currBlob = Utils.join(CommandsClass.ALL_FILES,
                    StagingArea.getFilesAdd().get(stagedName));
            if (stagedBlob.exists()) {
                currContents = Utils.readContentsAsString(stagedBlob);
                Utils.writeContents(currBlob, currContents);
            }
        }
        File currCommit = Utils.join(CommandsClass.COMMIT_FILE, this.getID());
        File currCommitContents = Utils.join(CommandsClass.COMMIT_FILE,
                this.getID() + "contents");
        Utils.writeObject(currCommitContents, this.getContents());
        Utils.writeObject(currCommit, this);
        StagingArea.clear();
    }
    public void addToContents(String fileName, String sHA1) {
        contents.put(fileName, sHA1);
    }

    /**
     * This gets the commit from the files -- but i shouldn't
     * be saving duplicates ???? Also how do you get the commit??
     * @param id
     * @return Commit
     */
    public static Commit getCommitFromID(String id) {
        File currCommit = Utils.join(CommandsClass.COMMIT_FILE, id);
        if (!currCommit.exists()) {
            return null;
        }
        Commit current = Utils.readObject(currCommit, Commit.class);
        File currCommitContents = Utils.join(CommandsClass.COMMIT_FILE,
                current.getID() + "contents");
        if (currCommitContents.exists()) {
            TreeMap newContents =
                    Utils.readObject(currCommitContents, TreeMap.class);
            current.contents = (TreeMap<String, String>) newContents;
        } else {
            current.contents = new TreeMap<String, String>();
        }
        return current;
    }
}
