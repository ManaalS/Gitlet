# Gitlet Design Doc 2

# Classes and Data Structures

Last updated: April 21

## Commit: 

an object that represents a set of tracked files

    private String hash; //stores the hashcode representation of the commit
    
    private ArrayList<Commit> parents; //parents of commit
    
    private String timestamp; //timestamp associated with the commit
    
    private HashMap<String, String> blobs; //files associated with the commit
    
    private String message; //message describing the commit changes
    
    private String abbrv; //the abbreviated hashcode of the commit
    
    private boolean merged; //whether or not the commit has 2 parents (equivalently, whether or not there was a merge for the commit) 
## Branch:

an object that represents a subset of a commit tree

    private String name; //name of the branch, initial is master
    
    private String head; //storing the hash code of the head commit in the current branch
    
    private Commit headCommit; //stores the actual Commit object associated with head commit of current branch
    
    private String parent; //parent of the branch, if there is any
    
    private ArrayList<Commit> branchCommits; //ALL the commits in the current branch


## Repo

an object that is in charge of accessing files, keeping pointers to current blobs/trees/commits, and doing the bulk of any command that requires updating and accessing files


    /**master branch of the current tree*/
    private static Branch master;
    
    /**current branch of our current tree*/
    private Branch current;
    
    /**current Commit of our current tree*/
    private Commit currentCommit; 
    
    /**head of CURRENT branch**/
    private Commit head;
    
    /**all the branches map, stored with their name as String part and their Commits as the ArrayList<Commits> part.*//
    private HashMap<String, ArrayList<Commit>> branches;
    
    /**gitlet directory*/
    private static final File gitlet = new File(".gitlet/");
    
    /**complete file path for branches*/
    private static final File branchFiles = new File(".gitlet/.branches/");
    
    /**complete file path for staging area*/
    private static final File stageFiles = new File(".gitlet/.stage/");
    
    /**complete file path for staging for removal area*/ 
    private static final File removeFiles = new File(".gitlet/.remove/");
    
    /**complete file path for commits*/
    private static final File commitFiles = new File(".gitlet/.commits/");
    
    /**complete file path for files*/
    private static final File blobs = new File(".gitlet/.blobs/");
    
    /**complete file path for CWD */
    private static final File cwd = new File(".");

    /**dont really use anywhere atm, but supposed to store whats staged for addition*/
    private ArrayList<String> stage;
    
    /**dont really use anywhere, but supposed to store whats staged for removal*/
    private ArrayList<String> remove; 


## Commands:

initial processing stage after Main, initializes a Repo so that we can edit and access files according to what the command needs. Also takes care of surface-level commands like log.  

    //initializes Repo to access more helpers to fulfill commands
    private Repo myRepo;


## Main:

The very first class that we go through when a user inputs a command. Creates a new Command class, checks operand format a bit, and calls the commands in the Command class. 

    /**initalizes Commands to access helper methods*/
    Commands myCommands = new Commands(); 


# Algorithms
## Commit

 

    -- default constructor, for initial commits --
    -- sets message to "initial commit", parents to null, merged to false, blobs to an empty HashMap, timestamp to default time. Also computes hash(), abbrv -- 
    public Commit()
    
    -- constructor for noninitial commits, takes in message, parents, and files -- 
    -- copies over parent's blobs if there are any. If the commit's new blobs have changes, then overwrite the parents respective files. Then, set message = m, parents = p. If no message, throw an error. set merge depending on how many parents there are. Set the timeStamp to current date/time, and serialize the commit and its blobs. -- 
    public Commit(String m, ArrayList<Commit> p, HashMap<String, String> theBlobs)
    
    --set files of current commit by inheriting files from parent commit (more specifically blobs)  -- 
    public void setFiles(Commit parent)
    
    -- create a file for the commit if does not exist, write Commit this to the file -- 
    public void serialize()
    
    -- create files for the blobs if does not exist, write blobs this to the file -- 
    public void serializeBlobs()
    
    -- compute the hash code of the commit -- 
    public String hash()
    
    -- helper method that formats each commit's for the log command -- 
    public String toString()
    
    -- getter methods -- 
    public boolean isMerge()
    public String getMessage()
    public String getDate() 
    public ArrayList<Commit> getParents() 
    public HashMap<String, String> getFiles()
## Branch
    --represents the default constructor for the master branch, assuming c is correctly assigned as the initial commit. --
    -- Sets name to master, parent to null, compute head, assign headCommit to c, adds c to the branchCommits, and serializes the Branch -- 
    public Branch(Commit c)
    
    -- assigns head to bhead, name to bname, parent to bparent's name, branchCommits to comm, and serializes the branch w serialize(). also sets this branch to current branch --
    public Branch(String bname, String bhead, Branch bparent, ArrayList<Commit> comm) {
    
    -- create a file for the branch based on its name (if not already existing), and then read the branch to the file -- 
    public void serialize()
    
    -- don't use this yet, but have it for resetting the head commit to another commit based on its hash id-- 
    public void setHead(String id) 
    
    --same thing as above, except pass in commit directly --
    public void setHead(Commit headC) 
    
    --sets the current branch in branch tree to a different branch -- 
    public void setCurrent(Branch b) 
    
    --adds another commit to the branch --
    public void addCommit(String hash)
    
    --same as above, directly adding to branchCommits --
    public void addCommit(Commit c) 
    
    -- getter methods -- 
    public ArrayList<Commit> getCommits()
    public Commit getHeadCommit()
    public String getName()
    public String getHead()
    



## Repo
    /** Creates an intial commit, and initial master branch, sets master branch as current branch, puts master branch info in branches map */
    Repo()
    
    /** initializes all relevant directories, serializes master branch and commit stuff */
    public void initializeRepo() 
    
    /** add a branch to the branches map, and set current as newBranch */
    public void addBranch(Branch newBranch)
    
    /** add a commit to a branch */ 
    public void addCommitToBranch(Commit c, Branch b)
    
    /** set commit as current commit */ 
    public void setCommitCurrent(Commit c)
    
    /** clear the staging area */ 
    public void clearStage() 
    
    /** commit everything in the staging area, associated with user-specified message */
    public void commit(String message)
    
    /** stage the file for commit. */
    public void stage(File f, String name)
    
    /** checkout the file given its file name */
    public void checkoutFile(String filename)
    
    /** checkout the commit's user-specified file */
    public void checkoutCommit(String commitId, String fileName)
    
    /** checkout a branch given its name */ 
    public void checkoutBranch(String branchname) 
    
    /** getter methods */
    public Commit getHead()
    
     /** remove file given. */
    public void rmF(String fileName) 
    
     /** remove an entire branch.  */
    public void rmbranch(String branchname) {

    /** display commits with given message. */
    public void find(String message) 
    
    /** get the status of all files currently. */
    public void status()
    
    /** make a new branch w given branch name. */
    public void branch(String branchname)
    
    /** remove an entire branch.  */
    public void rmbranch(String branchname) 
    
    /** reset to a given commitID. */
    public void reset(String commitID)
    
    /** merge two branches, the given and the current. */
    public void merge(String branchName) 
    
    /** RETURN the COMMIT latest common ancestor of CURRENTS and B. */
    public Commit findSplitPoint(Commit currents, Commit b) 
    
    /** helper for the split function, visit all the nodes/commits in the branch tracing back from curr */
    public void splitHelper(Commit curr, HashMap<String, String> visited)
    
    /** basic cases for merge -- errors and fast forwarding and the like */
    public void basicMergeCases(Commit split, Branch given, Branch cur) 
    
    /** deal with a conflict by writing conflict files */
    public void mergeConflict(File curr, File giv, String filen)
    
    /** make a commit thats a merge between CUR GIVEN. */
    public void mergeCommit(Branch cur, Branch given)

    /** REMOTES FUNCTIONALITY. */
    
    /** add a remote directory DIR with USERNAME. */
    public void addRemote(String username, String dir)
    
    /** remove a remote address of RM. */
    public void rmRemote(String rm)
    
    /** push from current head to repo RM head of BRANCH. */
    public void push(String rm, String branch)
    
    /** fetch from a remote RM repository BRANCH to the current. */
    public void fetch(String rm, String branch)
    
    /** pull from REMOTE REMOTEBRANCH command. */
    public void pull(String remote, String remotebranch)
    
    
  

 
    



## Commands 
    /** NOTE: this class doesnt really do the dirty work. It just is an extra processing step that takes in the command from Main and then calls the functions in Repo and saves the Repo. */
    
    /**create a Repo */ 
    Commands()
    
    /**initialize Repo, check if gitlet system already exists */
    public void initCommand()
    
    /** check if file with fileName exists, and if it does, stage it for addition by calling Repo's stage helper fxn. */
    public void addCommand(String fileName)
    
    /** commit all files in staging area by calling Repo's commit fxn */
    public void commitCommand(String msg)
    
    /** iterate through the head commit and the head commit's parents, calling Commit's toString */ 
    public void logCommand() 
    
    /** prints out commits like log except not neccessarily in order */ 
    public void globalLogCommand()
        
    /** add file FILENAME to removal stage. */
    public static void rmCommand(String fileName)
    
    /** like log, except out of order. */
    public void globalLogCommand()
    
    /** find all commits with message MESSAGE. */
    public static void findCommand(String message)
    
    /** get status of all the files atm. */
    public static void statusCommand()
    
    /** decide what to call for checkout given ARGS as args. */
    public static void checkoutCommand(String...args) 
    
    /** make a new branch BNAME in repo. */
    public static void branchCommand(String bname)
    
    /** remove branch BNAME in repo. */
    public static void rmBranchCommand(String bname)
    
    /** reset to earlier commit. */
    public static void resetCommand(String commidID)
    
    /** merge current branch with BRANCHNAME in repo. */
    public static void mergeCommand(String branchName)
    
    /** add a remote dir to repo with USERNAME and DIR. */
    public static void addRemoteCommand(String username, String dir) 
    
    /** remove remote REMOTENAME from repo with. */
    public static void rmRemoteCommand(String remotename)
    
    /** push to REMO and its REMOTEBRANCH from current branch. */
    public static void pushCommand(String remo, String remoteBranch)
    
    /** fetch from REMOTE and REMOTEBRANCH and add to current. */
    public static void fetchCommand(String remote, String remoteBranch)
    
    /** pull or merge REMOTENAME REMOTEBRANCH with curr. */
    public static void pullCommand(String remoteName, String remoteBranch)
    


# Persistance
1. My program creates an internal Repo to keep track of all the files. Any commands that the user inputs that edits or accessing these files are processed directly in Repo. The program reads and writes in this class. 
2. Repo also updates and saves the branch trees. The Branch trees save the commit trees. 
3. The files/directories initializes, edited and accessed in Repo include: an overarching .gitlet repository to keep the system in; a staging area for addition and removal each; a directory for commits, branches, blobs each; and the current working directory. 
4. These files are written and read from, so they’re kept persistently unless we have to remove them. Since we only ever have one instance of Repo after initializing, we don’t have to worry about these files and directories clearing or encountering any other changes we don’t want. 
5. In turn, Commands creates an instance of Repo, and only one instance. Therefore, any changes made in Repo, whether to the tree structure or the files, are persistent
6. Finally, Repo makes an instance of Commands. By the same logic as (5), everything we need to keep persistent is kept persistent. 
