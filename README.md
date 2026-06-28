# Gitlet

A Java implementation of a lightweight version control system inspired by Git. This project recreates many of Git's core features, including versioned commits, branching, merging, and file restoration, while using persistent on-disk storage to maintain repository state across program executions.
This project was completed as part of UC Berkeley's CS 61B: Data Structures course.

## Features
* Initialize a version control repository
* Stage files for addition or removal
* Create commits that capture snapshots of tracked files
* Restore files or entire commits
* View commit history
* Create and switch between branches
* Merge branches with conflict detection and resolution
* Persistent storage of repository metadata and file snapshots
* SHA-1 content hashing for object identification

## Technical Highlights
* Implemented persistent object storage using Java serialization and the file system
* Designed commit, blob, and repository data structures to efficiently represent repository history
* Used SHA-1 hashing to uniquely identify file contents and commits
* Implemented graph traversal logic for commit history and branch merging
* Managed repository state across multiple program executions without relying on external libraries
* Built a command-line interface that mirrors the behavior of common Git commands

## Example Commands
java gitlet.Main init

java gitlet.Main add example.txt

java gitlet.Main commit "Initial commit"

java gitlet.Main branch feature

java gitlet.Main checkout feature

java gitlet.Main merge main

