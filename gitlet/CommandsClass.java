package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Queue;
import java.util.ArrayDeque;
public class CommandsClass implements Serializable {

    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main Gitlet Folder for everything that . */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /** Staging Area. */
    static final File STAGING_FOLDER = Utils.join(GITLET_FOLDER, "StagingArea");
    /** Branches Foleder. */
    static final File BRANCHES = Utils.join(GITLET_FOLDER, "Branches");
    /** Holds the filenames and ids of the current staging area. */
    static final File CURR_STAGE =
            Utils.join(STAGING_FOLDER, "Current Staging Area");
    /** Holds the filenames and ids of
     * the staging area for removal. */
    static final File REMOVE_STAGE =
            Utils.join(STAGING_FOLDER, "Staging Area for Removal");
    /** Folder that holds all
     * versions of all files. */
    static final File ALL_FILES =
            Utils.join(GITLET_FOLDER, "allFiles");
    /** The Magic Number. */
    static final int MAGIC = 40;
    /** Folder that holds every single commit. */
    static final File COMMIT_FILE = Utils.join(GITLET_FOLDER, "Commits");
    /** File holding the current branch name we are on. */
    static final File CURR_BRANCH = Utils.join(BRANCHES, "Current Branch");
    /** Current Branch. */
    private static Branch head = new Branch("master");
    /** Creates initial commit with branch master.
     * @param args
     * */
    public static void init(String[] args) {
        if (CommandsClass.GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
            return;
        }
        validateNumArgs("init", args, 1);
        TreeMap<String, String> initContents = new TreeMap<String, String>();
        Commit initial = new Commit("initial commit", null, initContents);
        initial.pushInit();
        head.writeLatestCommit(initial);
        pushBranch();
    }
    public static void add(String[] args) {
        validateNumArgs("add", args, 2);
        initialized();
        File currFile = Utils.join(CommandsClass.CWD, args[1]);
        if (!currFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blobs curr = new Blobs(args[1]);
        StagingArea.getAllFiles();
        StagingArea.getAllRemoved();
        StagingArea.addStage(curr);
        StagingArea.pushToStage();
        StagingArea.pushToRemove();
    }
    /**
     * Same as parent except for files in the staging area which are updated.
     * StagingArea files: IDs with contents. I need
     * to go through every file in StagingArea and put it into Commit.
     * @param args
     */
    public static void commit(String[] args) {
        loadBranch();
        validateNumArgs("commit", args, 2);
        if (args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        StagingArea.getAllFiles();
        StagingArea.getAllRemoved();
        if (StagingArea.getFilesRemove().isEmpty()) {
            if (StagingArea.getFilesAdd().isEmpty()) {
                System.out.println("No changes added to the commit");
                return;
            }
        }
        Commit parent1 = head.getLatestCommit();
        Commit currCommit =
                new Commit(args[1], parent1.getID(), parent1.getContents());
        currCommit.pushCommit();
        head.writeLatestCommit(currCommit);
    }

    /**
     * first case: 1. need to get head commit
     * 2. need to get file ID in head commit
     * 3. need to read contents from correct file
     * 4. need to overwrite contents in current WD
     * Second: 1. need to get given commit with ID
     * 3. need to get file ID in any commit
     * 2. need to get the file in the all files
     * 3. need to overwrite contents in current WD
     * Last case:
     * checkout needs to delete files in CWD that don't appear in the new commit
     * @param args
     */
    public static void checkout(String[] args) {
        checkQuick(args);
        loadBranch();
        Commit curr = null;
        String id = "";
        if (args.length == 3) {
            curr = head.getLatestCommit();
            id = curr.getSpecificBlobID(args[2]);
            if (id.equals("")) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            updateContents(args[2], id);
        } else if (args.length == 4) {
            checkout4(args);
        } else if (args.length == 2) {
            if (head.getBranchName().equals(args[1])) {
                System.out.println("No need to checkout the current branch");
                return;
            }
            Branch newB = new Branch(args[1]);
            curr = newB.getLatestCommit();
            if (curr == null) {
                System.out.println("No such branch exists");
                return;
            }
            TreeMap<String, String> currContents = curr.getContents();
            checkUntracked(head.getLatestCommit());
            List<String> filesCWD = Utils.plainFilenamesIn(CWD);
            ArrayList<String> filesWD = toArray(filesCWD);
            String contents;
            String contentsSHA1;
            File cwdFile;
            for (String file : filesCWD) {
                cwdFile = Utils.join(CWD, file);
                if (currContents.size() == 0) {
                    Utils.restrictedDelete(cwdFile);
                } else if (!currContents.containsKey(file)) {
                    Utils.restrictedDelete(cwdFile);
                } else {
                    contentsSHA1 = currContents.get(file);
                    File temp = Utils.join(ALL_FILES, contentsSHA1);
                    contents = Utils.readContentsAsString(temp);
                    Utils.writeContents(cwdFile, contents);
                }
            }
            for (String c : currContents.keySet()) {
                if (!filesWD.contains(c)) {
                    cwdFile = Utils.join(CWD, c);
                    contentsSHA1 = currContents.get(c);
                    File temp = Utils.join(ALL_FILES, contentsSHA1);
                    contents = Utils.readContentsAsString(temp);
                    Utils.writeContents(cwdFile, contents);
                }
            }
            head = newB;
            pushBranch();
            StagingArea.clearAdd();
        }
    }

    public static void checkout4(String [] args) {
        Commit curr;
        String id;
        if (args[1].length() < MAGIC) {
            curr = shortenedID(args[1]);
        } else {
            curr = Commit.getCommitFromID(args[1]);
        }
        if (curr == null) {
            System.out.println("No commit with that ID exists");
            return;
        }
        id = curr.getSpecificBlobID(args[3]);
        if (id.equals("")) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        updateContents(args[3], id);
    }
    public static void checkQuick(String[] args) {
        if (args.length > 3 && !args[2].equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    public static ArrayList<String> toArray(List<String> curr) {
        ArrayList<String> newest = new ArrayList<String>();
        for (String s : curr) {
            newest.add(s);
        }
        return newest;
    }
    /**
     * Helper function for checkout. This updates the contents
     * @param fileName
     * @param commitID
     */
    public static void updateContents(String fileName, String commitID) {
        File wDFile = Utils.join(CWD, fileName);
        File commitFile = Utils.join(ALL_FILES, commitID);
        String readContents = Utils.readContentsAsString(commitFile);
        Utils.writeContents(wDFile, readContents);
    }

    public static void log(String[] args) {
        loadBranch();
        Commit curr = head.getLatestCommit();
        String parent1 = "";
        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.getID());
            System.out.println("Date: " + curr.getDate());
            System.out.println(curr.getMessage());
            System.out.println();
            parent1 = curr.getFirstParent();
            if (parent1 == null) {
                curr = null;
            } else {
                curr = Commit.getCommitFromID(curr.getFirstParent());
            }
        }
    }

    /**
     * Deletes branch pointer -- meaning it deletes
     * the file in BRANCHES with the given branch name.
     * @param args
     */
    public static void rmBranch(String[] args) {
        loadBranch();
        validateNumArgs("rm-branch", args, 2);
        if (head.getBranchName().equals(args[1])) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branchDeleted = Utils.join(BRANCHES, args[1]);
        if (!branchDeleted.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        branchDeleted.delete();
    }
    /**
     * Checks out all files tracked by given commit.
     * Can call checkout on each file in the commit ID contents.
     * Removes tracked files that are not present in that commit --
     * so just remove files from CWD that aren't in the latest commit ??
     * I need to remove the files from CWD that aren't updated.
     * Move the current branch's head to the given commit node.
     * @param args
     */
    public static void reset(String[] args) {
        loadBranch();
        validateNumArgs("reset", args, 2);
        Commit curr = null;
        if (args[1].length() < MAGIC) {
            curr = shortenedID(args[1]);
        } else {
            curr = Commit.getCommitFromID(args[1]);
        }
        if (curr == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        checkUntracked(curr);
        TreeMap<String, String> contents = curr.getContents();
        String id = curr.getID();
        for (String fileName : contents.keySet()) {
            String[] args2 = {"checkout", id, "--", fileName};
            checkout(args2);
        }
        removeNotPresent(curr);
        StagingArea.clearAdd();
        head.writeLatestCommit(curr);
    }

    public static void removeNotPresent(Commit curr) {
        List<String> filesCWD = Utils.plainFilenamesIn(CWD);
        TreeMap<String, String> contents = curr.getContents();
        for (String f : filesCWD) {
            if (!contents.containsKey(f)) {
                Utils.restrictedDelete(Utils.join(CWD, f));
            }
        }
    }
    /**
     * This unstages the file with the given
     * name if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the WD.
     * @param args
     */
    public static void remove(String[] args) {
        loadBranch();
        validateNumArgs("rm", args, 2);
        StagingArea.getAllFiles();
        StagingArea.getAllRemoved();
        Boolean removed = false;
        Commit curr = head.getLatestCommit();
        if (StagingArea.getFilesAdd().containsKey(args[1])) {
            StagingArea.getFilesAdd().remove(args[1]);
            StagingArea.pushToStage();
            removed = true;
        }
        if (curr.getContents() != null
                && curr.getContents().containsKey(args[1])) {
            String blobID = curr.getContents().get(args[1]);
            File temp = Utils.join(CommandsClass.ALL_FILES, blobID);
            String contents = Utils.readContentsAsString(temp);
            StagingArea.stageForRemove(new Blobs(args[1], contents));
            StagingArea.pushToRemove();
            File inCWD = Utils.join(CWD, args[1]);
            Utils.restrictedDelete(inCWD);
            removed = true;
        }
        if (!removed) {
            System.out.println("No reason to remove the file.");
        }
    }

    /**
     * Displays information about all commits ever made.
     * Need to iterate over file in the directory branches and call log on it.
     * @param args
     */
    public static void globalLog(String[] args) {
        validateNumArgs("global-log", args, 1);
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_FILE);
        Commit curr;
        for (String currCommitID : allCommits) {
            if (currCommitID.length() == MAGIC) {
                curr = Commit.getCommitFromID(currCommitID);
                System.out.println("===");
                System.out.println("commit " + curr.getID());
                System.out.println("Date: " + curr.getDate());
                System.out.println(curr.getMessage());
                System.out.println();
            }
        }
    }
    public static void find(String[] args) {
        String commitMessage = "";
        commitMessage = args[1];
        for (int i = 2; i < args.length; i++) {
            commitMessage = commitMessage + " " + args[i];
        }
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_FILE);
        Commit curr;
        Boolean commitExisted = false;
        for (String commitID : allCommits) {
            if (commitID.length() <= MAGIC) {
                curr = Commit.getCommitFromID(commitID);
                if (curr.getMessage().equals(commitMessage)) {
                    System.out.println(commitID);
                    commitExisted = true;
                }
            }
        }
        if (!commitExisted) {
            System.out.println("Found no commit with that message.");
        }

    }

    /**
     * Displays what the current status of gitlet in a specified format.
     * @param args
     */
    public static void status(String[] args) {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        loadBranch();
        StagingArea.getAllFiles();
        StagingArea.getAllRemoved();
        System.out.println("=== Branches ===");
        List<String> allBranches = Utils.plainFilenamesIn(BRANCHES);
        java.util.Collections.sort(allBranches);
        for (String b : allBranches) {
            if (b.equals(head.getBranchName())) {
                System.out.println("*" + b);
            } else if (!b.equals("Current Branch")) {
                System.out.println(b);
            }
        }
        System.out.println("\n=== Staged Files ===");
        Set<String> stagedFiles =  StagingArea.getFilesAdd().keySet();
        ArrayList<String> sortedFiles = sorted(stagedFiles);
        for (String s : sortedFiles) {
            System.out.println(s);
        }
        System.out.println("\n=== Removed Files ===");
        Set<String> removedFiles =  StagingArea.getFilesRemove().keySet();
        ArrayList<String> sortedFilesR = sorted(removedFiles);
        for (String s : sortedFilesR) {
            System.out.println(s);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }
    public static ArrayList<String> sorted(Set<String> sorting) {
        ArrayList<String> sortedFiles = new ArrayList<String>();
        for (String s : sorting) {
            sortedFiles.add(s);
        }
        java.util.Collections.sort(sortedFiles);
        return sortedFiles;
    }

    public static void branch(String[] args) {
        loadBranch();
        validateNumArgs("branch", args, 2);
        if (Branch.checkExists(args[1])) {
            System.out.println("A branch with that name already exists.");
        }
        String branchName = args[1];
        Branch b = new Branch(branchName);
        Commit curr = head.getLatestCommit();
        b.writeLatestCommit(curr);
    }
    public static void loadEverything() {
        loadBranch();
        StagingArea.getAllFiles();
        StagingArea.getAllRemoved();
    }
    /**
     * Merges two branches into one.
     * @param args
     */
    public static void merge(String[] args) {
        validateNumArgs("merge", args, 2);
        loadEverything();
        checkFailures(args[1]);
        Branch givenNow = new Branch(args[1]);
        Commit curr = head.getLatestCommit();
        Commit gave = givenNow.getLatestCommit();
        Commit splitPoint = splitPoint(curr, gave);
        cSC(splitPoint, gave, curr, args[1]);
        TreeSet<String> allP =
                combine(curr.getContents().keySet(),
                        gave.getContents().keySet(),
                        splitPoint.getContents().keySet());
        String cBI, gBI, sBI;
        Boolean encounteredConflict = false;
        Blobs currBlob;
        String finalFile;
        ArrayList<String> tempDel = new ArrayList<String>();
        for (String file : allP) {
            cBI = curr.getSpecificBlobID(file);
            gBI = gave.getSpecificBlobID(file);
            sBI = splitPoint.getSpecificBlobID(file);
            if (checkDoNothing(cBI, gBI, sBI)) {
                continue;
            } else if ((sBI.equals(cBI)
                    && !sBI.equals(gBI) && !gBI.equals(""))
                    || (sBI.equals("") && !gBI.equals("") && cBI.equals(""))) {
                File temp = Utils.join(CWD, file);
                String tempCont =
                        Utils.readContentsAsString(Utils.join(ALL_FILES, gBI));
                Utils.writeContents(temp, tempCont);
                addStageSteps(file, gBI);
                tempDel.add(file);
            } else if (!sBI.equals("") && sBI.equals(cBI) && gBI.equals("")) {
                removeStageSteps(file, cBI);
            } else if (!cBI.equals(gBI)) {
                encounteredConflict = true;
                finalFile = getFinalCont(cBI, gBI);
                currBlob = new Blobs(file, finalFile);
                StagingArea.addStage(currBlob);
                File temp = Utils.join(CWD, file);
                Utils.writeContents(temp, finalFile);
                tempDel.add(file);
            }
        }
        StagingArea.pushToStage();
        StagingArea.pushToRemove();
        del(tempDel);
        Commit currCommit = new Commit("Merged "
                + args[1] + " into " + head.getBranchName()
                + ".", curr.getID(), gave.getID(), curr.getContents());
        currCommit.pushCommit();
        head.writeLatestCommit(currCommit);
        String[] args2 = {"reset", currCommit.getID()};
        reset(args2);
        conflicted(encounteredConflict);
    }

    public static String getFinalCont(String currBlobID, String gaveBlobID) {
        String fileCurr;
        String giveCurr;
        String finalFile;
        File temp;
        File temp2;
        if (currBlobID.equals("")) {
            fileCurr = "";
            temp = Utils.join(ALL_FILES, gaveBlobID);
            giveCurr = Utils.readContentsAsString(temp);
        } else if (gaveBlobID.equals("")) {
            giveCurr = "";
            temp = Utils.join(ALL_FILES, currBlobID);
            fileCurr = Utils.readContentsAsString(temp);
        } else {
            temp = Utils.join(ALL_FILES, currBlobID);
            temp2 = Utils.join(ALL_FILES, gaveBlobID);
            fileCurr = Utils.readContentsAsString(temp);
            giveCurr = Utils.readContentsAsString(temp2);
        }
        finalFile = "<<<<<<< HEAD\n" + fileCurr
                + "=======\n" + giveCurr + ">>>>>>>\n";
        return finalFile;
    }

    public static Boolean checkDoNothing(String cID, String gID, String sID) {
        if (sID.equals(gID)
                && !sID.equals(cID)
                && !cID.equals("")) {
            return true;
        } else if (cID.equals(gID)) {
            return true;
        } else if (sID.equals("")
                && gID.equals("")
                && !cID.equals("")) {
            return true;
        } else if (!sID.equals("")
                && sID.equals(gID)
                && cID.equals("")) {
            return true;
        }
        return false;
    }
    public static void conflicted(Boolean encounteredConflict) {
        if (encounteredConflict) {
            System.out.println("Encountered a merge conflict");
        }
    }
    public static void del(ArrayList<String> tempDel) {
        for (String c: tempDel) {
            Utils.restrictedDelete(Utils.join(CWD, c));
        }
    }

    public static void cSC(Commit s, Commit g, Commit c, String b) {
        checkExtraFailure(c);
        if (s.getID().equals(g.getID())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch");
            System.exit(0);
        }
        if (s.getID().equals(c.getID())) {
            String[] args2 = {"checkout", b};
            checkout(args2);
            System.out.println("Current branch fast-forwarded");
            System.exit(0);
        }
    }
    public static TreeSet<String> combine(Set<String> f,
                                          Set<String> s, Set<String> t) {
        TreeSet<String> finalized = new TreeSet<String>();
        for (String c : f) {
            if (!finalized.contains(c)) {
                finalized.add(c);
            }
        }
        for (String c : s) {
            if (!finalized.contains(c)) {
                finalized.add(c);
            }
        }
        for (String c : t) {
            if (!finalized.contains(c)) {
                finalized.add(c);
            }
        }
        return finalized;
    }
    public static void addStageSteps(String file, String currBlobID) {
        File newFile = Utils.join(ALL_FILES, currBlobID);
        String finalFile = Utils.readContentsAsString(newFile);
        Blobs currBlob = new Blobs(file, finalFile);
        StagingArea.addStage(currBlob);
    }

    public static void removeStageSteps(String file, String currBlobID) {
        File newFile = Utils.join(ALL_FILES, currBlobID);
        String finalFile = Utils.readContentsAsString(newFile);
        Blobs currBlob = new Blobs(file, finalFile);
        StagingArea.stageForRemove(currBlob);
    }

    public static Commit splitPoint(Commit current, Commit given) {
        ArrayList<String> currHistory = getHistory2(current);
        ArrayList<String> givenHistory = getHistory2(given);
        for (String curr : currHistory) {
            if (givenHistory.contains(curr)) {
                return Commit.getCommitFromID(curr);
            }
        }
        return Commit.getCommitFromID(currHistory.get(-1));
    }

    public static ArrayList<String> getHistory2(Commit current) {
        Queue<Commit> currHistory = new ArrayDeque<Commit>();
        currHistory.add(current);
        ArrayList<String> historyOrderedID = new ArrayList<String>();
        while (!currHistory.isEmpty()) {
            Commit curr = currHistory.remove();
            historyOrderedID.add(curr.getID());
            if (curr.getFirstParent() != null) {
                currHistory.add(Commit.getCommitFromID(curr.getFirstParent()));
            }
            if (curr.getSecondParent() != null) {
                currHistory.add(Commit.getCommitFromID(curr.getSecondParent()));
            }
        }
        return historyOrderedID;
    }

    public static void checkExtraFailure(Commit curr) {
        List<String> wd = Utils.plainFilenamesIn(CWD);
        if (curr == null) {
            for (String c : wd) {
                System.out.println("There is an untracked file in "
                        + "the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        } else {
            TreeMap<String, String> currContents = curr.getContents();
            for (String cWD : wd) {
                if (!currContents.containsKey(cWD))  {
                    System.out.println("There is an untracked file in "
                           + "the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    public static void checkFailures(String given) {
        if (!StagingArea.getFilesAdd().isEmpty()
                || !StagingArea.getFilesRemove().isEmpty()) {
            System.out.println("You have uncommitted changes");
            System.exit(0);
        }
        if (!Branch.checkExists(given)) {
            System.out.println("A branch with that name does not exist");
            System.exit(0);
        }
        if (given.equals(head.getBranchName())) {
            System.out.println("Cannot merge a branch with itself");
            System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                           + "arguments for: %s.", cmd));
        }
    }

    public static void initialized() {
        if (!(GITLET_FOLDER.exists())) {
            System.out.println("Have not "
                   + "initialized Gitlet version-control system");
            System.exit(0);
        }
    }

    public static Commit shortenedID(String shortened) {
        List<String> allCommits =
                Utils.plainFilenamesIn(COMMIT_FILE);
        for (String currID : allCommits) {
            if (currID.substring(0,
                    shortened.length()).equals(shortened)) {
                return Commit.getCommitFromID(currID);
            }
        }
        return null;
    }

    public static void pushBranch() {
        Utils.writeContents(CURR_BRANCH, head.getBranchName());
    }
    public static void loadBranch() {
        String branchName = Utils.readContentsAsString(CURR_BRANCH);
        if (branchName == null) {
            return;
        }
        head = new Branch(branchName);
    }

    public static void checkUntracked(Commit latest) {
        List<String> filesCWD = Utils.plainFilenamesIn(CWD);
        TreeMap<String, String> contents = latest.getContents();
        for (String file : filesCWD) {
            if (contents.isEmpty()) {
                System.out.println("There is an untracked file"
                       + " in the way; delete it, or add and commit it first");
                System.exit(0);
            }
        }
    }

    public static Branch getHead() {
        return head;
    }
}

