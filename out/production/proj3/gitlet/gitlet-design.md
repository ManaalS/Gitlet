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
    
    --same thing as above, except pass in commit directly, dont use yet --
    public void setHead(Commit headC) 
    
    --sets the current branch in branch tree to a different branch, I dont use this yet -- 
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
    
    /** methods I havent implemented yet, these are for the other commands */
    public void rmC() 
    public void find() 
    public void status()
    public void branch()
    public void rmBranch()
    public void reset()
    public void merge()



## Commands
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
    
    /** have not implemented, but prints out commits like log except not neccessarily in order */ 
    public void globalLogCommand()
    
    /** other methods I have not implemented yet, in charge of processing their respective commands and checking that the operands are in the correct format, before calling Repo's helper methods */ 
    
    public void rmCommand()
    public void globalLogCommand()
    public void findCommand()
    public void statusCommand()
    public void branchCommand()
    public void rmBranchCommand()
    public void resetCommand()
    public void mergeCommand()


# Persistance
1. My program creates an internal Repo to keep track of all the files. Any commands that the user inputs that edits or accessing these files are processed directly in Repo. The program reads and writes in this class. 
2. Repo also updates and saves the branch trees. The Branch trees save the commit trees. 
3. The files/directories initializes, edited and accessed in Repo include: an overarching .gitlet repository to keep the system in; a staging area for addition and removal each; a directory for commits, branches, blobs each; and the current working directory. 
4. These files are written and read from, so they’re kept persistently unless we have to remove them. Since we only ever have one instance of Repo after initializing, we don’t have to worry about these files and directories clearing or encountering any other changes we don’t want. 
5. In turn, Commands creates an instance of Repo, and only one instance. Therefore, any changes made in Repo, whether to the tree structure or the files, are persistent
6. Finally, Repo makes an instance of Commands. By the same logic as (5), everything we need to keep persistent is kept persistent. 