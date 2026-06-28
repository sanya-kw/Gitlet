# Gitlet Design Document
author: Sanya Kwatra

## 1. Classes and Data Structures

### Main.java
This class is the entry point of the program. It implements methods to set up 
persistance and calls the needed method based on the commands passed in.

### Fields
1. None ???

### Branch.java
This class represents specific Commit branches that are named. Where we 
can specify Master, HEAD, and any other branches the user creates. Branches are names for a reference(a SHA-1 identifier)

### Fields
1. File currBranch : the file object that represents that branch
2. HashMap branchCommits : represents all the commits within that directory
3. String branchName : the name of that branch

### Commits.java
This class represents a commit. A commit will have a logMessage, a timeCreated, up to two parents, an ID, and a branch. 

### Fields
1. Hashmap <String, Blobs> contents: the contents of the every file in the commit. ID pointing to actual Blob so we can check if file contents are the same easily (through SHA-1 ID)
2. String ID: unique reference (SHA-1)
3. String parent1, parent2: parent commits
4. Timestamp timeCreated: when the commit was added
5. String logMessage: log message
6. Static Hashmap <String, Commit> allCommits: commitIDs pointing to the commits -- every commit 
  gets added
7. Branch branch : where the commit will be pushed to within .gitlet

### Blobs.java
This class represents the contents of files.

### Fields
1. String fileContents: current content of a file
2. String ID: unique reference (depends on name and contents -- unique)
3. String fileName: Name file should be stored under in commit folder and staging folder

### StagingArea.java
This class represents the staging area of git. 

### Fields
1. Hashmap filesAdd: Filenames pointing to blobs ready to be staged
2. Hashmap filesRemove: Filenames pointing to blobs ready to be removed

###CommandsClass.java
This class implements the logic for every command for gitlet.

###Fields
1. File CWD : current working directory
2. File GITLET_FOLDER : .gitlet folder within CWD
3. File STAGING_FOLDER : staging area within gitlet folder
4. File REMOVING_FOLDER : removing area within gitlet folder
5. Boolean initialized : boolean that makes sure a gitlet version-control system in the current working directory has been created
6. Commit HEAD : the latest commit


## 2. Algorithms

####Main
1. main(String[] args): This is the entry point of the program. It first checks to make sure that the input array is not empty. Then, depending on the input argument, different functions are called to perform the operation.

####Commit
1. getMessage, getTiemCreated, getFirstParent, getSecondParent, getID : returns needed instance variables
2. addToAll() : Adds current Commit object to the HashMap of all commits so it can be found later
3. fetch( String currID) : returns the commit with the given SHA-1 code
4. pushInit : creates .gitlet folder after checking that it exists, creates the staging folder, creates inital commit
5. pushCommit : pushes the current commit after checking that those files have been staged, creating a new folder in .gitlet with the snapshot of the current working directory
6. update : updates the Commit with the staged files, adding previously untracked files and updating/overwriting tracked files
7. untrack : removes any files from the commit that are previously staged for removal

####CommandsClass
1. init : validates number of args, creates the initial commit and pushes it, sets up master and head branch, sets boolean initialized to true
2. add : takes a snapshot of the current files, adds to staging area (overwrites original staging area, only if different from original)
3. commit : saves a snapshot of tracked files in current commit and staging area, moves needed branches along, clears staging area, saves a log message, a SHA-1, and a timestamp
4. rm : unstage a file in addition, or stage and remove if it is a tracked file in the CURRENT commit
5. log : display info about each commit backwards along tree until initial commit starting at current HEAD
6. global-log : displays information about all commits ever made in any oder
7. find : prints out ids of all commits with given commit message
8. status : displays branches that currently exist, and marks current branch with a *, displays files that are currently in StagingArea, 
9. checkout : function depends on input
   1. takes version of file in HEAD and puts in working directory (Overwrites)
   2. Takes verson of file in given commit ID and puts in working directory (overwrites)
   3. takes all files in commit at HEAD of given branch and puts in working directory (overwrites), makes given branch HEAD, deletes files that aren't present in given file, staging are is cleared 
10. branch : creates a new branch with the given name and points it at HEAD
11. rm-branch : delete given branch (deletes pointer for reference)
12. reset : checks out all files tracked by given commit, removes tracked files not present in that commit, moves HEAD to that commit node, clears staging area, 
13. merge : merges files from a given branch into current branch
14. validateNumArgs : makes sure the correct number of inputs for that command has been inputted

####StagingArea
1. clearAdd, clearRemove, clear : clears Staging area and Removal staging area
2. addStage(Blob) : adds a given Blob to the staging area given the criteria tha tth efile exists and that the working directory version of the file differs from the version of the file in the latest commit
3. stageForRemove : stages a file for removal
4. pushToStage : pushes files in staging area to STAGING_FOLDER

####Blobs
1. Blobs(FileName) : constructs a blob given the name of its file
2. getID, getContents, getName : returns needed instance variables


## 3. Persistence

Create .gitlet with the first call to init -- has to be init. Within that I will have a directory that has a file 
for every blob every created. The name of the file is the SHA-1 code and the contents are the contents of the blob. 
I will have a file with the object serialized of every commit. A commit will have a TreeMap that has the name and SHA-1 
code of every file within it so it can access the files within the file directory as needed. 


## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

![](../../../../../var/folders/39/1k8421hs6xn0hlctcx5yg8vh0000gn/T/TemporaryItems/NSIRD_screencaptureui_tSBJo8/Screen Shot 2022-04-21 at 9.02.06 AM.png)