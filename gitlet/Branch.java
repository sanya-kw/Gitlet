package gitlet;
import java.io.File;
import java.io.Serializable;

/**
 * This class represents a Branch. It creates a folder
 * within .gitlet. Every new branch is a file in folder branches
 * that has the ID of the latest commit.
 * @author Sanya Kwatra
 */
public class Branch implements Serializable {

    /** New Branch Folder. */
    private String branchName;

    /** Specific file object. */
    private File specificBranch;

    public Branch(String name) {

        branchName = name;

        specificBranch = Utils.join(CommandsClass.BRANCHES, branchName);
    }

    /**
     * Updates commit in specific branch.
     * @param latest
     */
    public void writeLatestCommit(Commit latest) {
        Utils.writeContents(specificBranch, latest.getID());
    }

    /**
     * Returns latest commit of a given branch.
     * @return Commit
     */
    public Commit getLatestCommit() {
        if (specificBranch.exists()) {
            String commitID = Utils.readContentsAsString(specificBranch);
            return Commit.getCommitFromID(commitID);
        }
        return null;
    }

    public static Boolean checkExists(String named) {
        File possibility = Utils.join(CommandsClass.BRANCHES, named);
        return possibility.exists();
    }

    public String getBranchName() {
        return branchName;
    }

}
