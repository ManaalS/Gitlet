package gitlet;
import java.io.File;

/** Commands stuff.
 * @author Manaal */

public class Commands {

    /** my repo w all the processing stuff. */
    private static Repo myRepo;

    /** make a commands class. */
    public Commands() {
        File repo = new File(".repo");
        if (repo.exists()) {
            myRepo = Utils.readObject(repo, Repo.class);
        } else {
            myRepo = new Repo();
        }
        myRepo.saveRepo();
    }

    /** initialize gitlet system. */
    public static void initCommand() {
        File dir = new File(".gitlet/");
        if (dir.exists()) {
            Utils.message("A gitlet version control system"
                    + "already exists in the current directory.");
            System.exit(0);
        } else {
            myRepo.initializeRepo();
            myRepo.saveRepo();
        }
    }

    /** stage a file FILENAME in repo. */
    public static void addCommand(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            Utils.message("File does not exist");
            System.exit(0);
        }
        myRepo.stage(fileName);
        myRepo.saveRepo();
    }

    /** commit all thats been staged in repo with MSG. */
    public static void commitCommand(String msg) {
        myRepo.commit(msg, null);
        myRepo.saveRepo();
    }

    /** add file FILENAME to removal stage. */
    public static void rmCommand(String fileName) {
        myRepo.rmF(fileName);
        myRepo.saveRepo();
    }

    /** log information about all the commits. */
    public static void logCommand() {
        Commit head = myRepo.getHead();
        while (head != null && head.getParents() != null) {
            String logString = head.logString();
            Utils.message(head.logString());
            head = head.getParents().get(0);
        }
        Utils.message(head.logString());
        myRepo.saveRepo();
    }

    /** like log, except out of order. */
    public void globalLogCommand() {
        myRepo.globallog();
    }

    /** find all commits with message MESSAGE. */
    public static void findCommand(String message) {
        myRepo.find(message);
        myRepo.saveRepo();
    }

    /** get status of all the files atm. */
    public static void statusCommand() {
        myRepo.status();
        myRepo.saveRepo();
    }

    /** decide what to call for checkout given ARGS as args. */
    public static void checkoutCommand(String...args) {
        if (args.length == 2) {
            myRepo.checkoutBranch(args[1]);
        } else if (args.length == 3) {
            myRepo.checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            myRepo.checkoutCommit(args[1], args[3]);
        } else {
            Utils.message("Incorrect Operands");
            System.exit(0);
        }
        myRepo.saveRepo();
    }

    /** make a new branch BNAME in repo. */
    public static void branchCommand(String bname) {
        myRepo.branch(bname);
        myRepo.saveRepo();
    }

    /** remote branch BNAME in repo. */
    public static void rmBranchCommand(String bname) {
        myRepo.rmbranch(bname);
        myRepo.saveRepo();
    }

    /** reset to COMMIDID commit. */
    public static void resetCommand(String commidID) {
        myRepo.reset(commidID);
        myRepo.saveRepo();
    }

    /** merge current branch with BRANCHNAME in repo. */
    public static void mergeCommand(String branchName) {
        myRepo.merge(branchName);
        myRepo.saveRepo();
    }

    /** add a remote dir to repo with USERNAME and DIR. */
    public static void addRemoteCommand(String username, String dir) {
        myRepo.addRemote(username, dir);
        myRepo.saveRepo();
    }

    /** remove remote REMOTENAME from repo with. */
    public static void rmRemoteCommand(String remotename) {
        myRepo.rmRemote(remotename);
        myRepo.saveRepo();
    }

    /** push to REMO and its REMOTEBRANCH from current branch. */
    public static void pushCommand(String remo, String remoteBranch) {
        myRepo.push(remo, remoteBranch);
        myRepo.saveRepo();
    }

    /** fetch from REMOTE and REMOTEBRANCH and add to current. */
    public static void fetchCommand(String remote, String remoteBranch) {
        myRepo.fetch(remote, remoteBranch);
        myRepo.saveRepo();
    }

    /** pull or merge REMOTENAME REMOTEBRANCH with curr. */
    public static void pullCommand(String remoteName, String remoteBranch) {
        myRepo.pull(remoteName, remoteBranch);
        myRepo.saveRepo();
    }
}
