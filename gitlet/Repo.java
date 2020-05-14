package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/** the Repo class.
 * @author Manaal */

public class Repo implements Serializable {
    /** master branch. */
    private static Branch master;
    /** the branch were currently on. */
    private Branch current;
    /** the current commit. */
    private Commit currentCommit;
    /** head commit. actually same as current commit */
    private Commit head;

    /** current working directory. */
    private static final File CWD = new File(".");
    /** gitlet directory. */
    private static final File GITLET = new File(".gitlet");
    /** branch files dir. */
    private static final File BRANCHFILES = new File(".gitlet/.branches");
    /** staging directory for addition. */
    private static final File STAGEFILES = new File(".gitlet/.stage");
    /** removal directory. */
    private static final File REMOVE = new File(".gitlet/.remove");
    /** dir of commits. */
    private static final File COMMITFILES = new File(".gitlet/.commits");
    /** dir of blobs. */
    private static final File BLOBS = new File(".gitlet/.blobs");
    /** file representing serialization saved for repo class. */
    private static final File REPO = new File(".repo");
    /** directory to keep paths of remote directory. */
    private static final File REMOTES = new File(".gitlet/.REMOTES");
    /** file SEP. */
    private static final String SEP = File.separator;

    /** create repo. */
    Repo() {
        if (!REPO.exists()) {
            Commit initial = new Commit();
            master = new Branch(initial);
            master.setCurrent(master);
            current = master;
            currentCommit = initial;
            this.saveRepo();
        }
    }

    /** initialize repo. */
    public void initializeRepo() {
        GITLET.mkdir();
        STAGEFILES.mkdir();
        REMOVE.mkdir();
        COMMITFILES.mkdir();
        BRANCHFILES.mkdir();
        BLOBS.mkdir();
        CWD.mkdir();
        REMOTES.mkdir();
        currentCommit.serialize();
        currentCommit.serializeBlobs();
        master.serialize();
    }

    /** serialize repo. */
    public void saveRepo() {
        Utils.writeObject(REPO, this);
    }

    /** set head commit.
     * @param c */
    public void setCommitCurrent(Commit c) {
        currentCommit = c;
    }

    /** get current commit.
     * @return Commit */
    public Commit getHead() {
        return currentCommit;
    }

    /** get current branch.
     * @return Branch */
    public Branch getCurrent() {
        return current;
    }

    /** set current branch.
     * @param curr */
    public void setCurrent(Branch curr) {
        current = curr;
    }

    /** clear the staging directory. */
    public void clearStage() {
        for (File staged: STAGEFILES.listFiles()) {
            staged.delete();
        }
    }

    /** stage a file for addition.
     * @param name */
    public void stage(String name) {
        File cw = new File(CWD + SEP + name);
        File stage = new File(STAGEFILES + SEP + name);
        if (!cw.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        }
        if (stage.exists()) {
            if (stage.compareTo(cw) != 0) {
                Utils.writeContents(stage, Utils.readContentsAsString(cw));
            }
        }
        File remove = new File(REMOVE + SEP + name);
        if (remove.exists()) {
            File a = new File(STAGEFILES + SEP + name);
            Utils.writeContents(a, Utils.readContents(remove));
            remove.delete();
        }
        if (currentCommit.getFiles().containsKey(name)) {
            File cwdB = new File(CWD + SEP + name);
            String hash = currentCommit.getFiles().get(name);
            File commBlob = new File(BLOBS + SEP + hash);
            String commBlobHash = Utils.sha1(Utils.readContents(commBlob));
            String cwdBlobHash = Utils.sha1(Utils.readContents(cwdB));
            File stageBlob = new File(STAGEFILES + SEP + name);
            if (commBlobHash.compareTo(cwdBlobHash) == 0) {
                if (stageBlob.exists()) {
                    stageBlob.delete();
                }
                return;
            }
        }
        File adding = new File(STAGEFILES + SEP + name);
        Utils.writeContents(adding, Utils.readContents(cw));
        saveRepo();
    }

    /** stage a file for addition.
     * take in MESSAGE and make a commit. MERGED if parents > 1 */
    public void commit(String message, Commit merged) {
        if (STAGEFILES.list().length == 0 && REMOVE.list().length == 0) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        ArrayList<Commit> parents = new ArrayList<>();
        Commit parent = currentCommit;
        parents.add(parent);
        if (merged != null) {
            parents.add(merged);
        }
        HashMap<String, String> allStaged = new HashMap<>();
        for (File staged: STAGEFILES.listFiles()) {
            String fileSha = Utils.readContentsAsString(staged);
            String hashID = Utils.sha1(fileSha + staged.getName());
            allStaged.put(staged.getName(), hashID);
            staged.delete();
        }
        Commit newCommit = new Commit(message, parents, allStaged);
        for (File removal: REMOVE.listFiles()) {
            HashMap<String, String> copy = newCommit.getFiles();
            copy.remove(removal.getName());
            removal.delete();
        }
        newCommit.setParents(currentCommit);
        newCommit.serialize();
        newCommit.serializeBlobs();
        currentCommit = newCommit;
        current.addCommit(newCommit);
        current.setHeadCommit(newCommit);
        String newP = BRANCHFILES + SEP + current.getName();
        File branchUpdate = new File(newP);
        Utils.writeObject(branchUpdate, current);
        saveRepo();
    }


    /** checkout to a previous version of filename.
     * @param filename */
    public void checkoutFile(String filename) {
        HashMap<String, String> headFiles = currentCommit.getFiles();
        boolean foundInPrev = false;
        for (HashMap.Entry<String, String> entry : headFiles.entrySet()) {
            String commitCurrFileName = entry.getKey();
            if (commitCurrFileName.equals(filename)) {
                File cwd = new File(CWD + SEP + filename);
                File blob = new File(BLOBS + SEP + entry.getValue());
                Utils.writeContents(cwd, Utils.readContents(blob));
                foundInPrev = true;
                saveRepo();
                break;
            }
        }
        if (!foundInPrev) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** checkout to FIL of a given COMMID. */
    public void checkoutCommit(String commid, String fil) {
        File f = new File(COMMITFILES + SEP + commid);
        boolean abbExist = false;
        for (File poss: COMMITFILES.listFiles()) {
            String sub = poss.getName().substring(0, 8);
            if (sub.equals(commid)) {
                abbExist = true;
                f = poss;
            }
        }
        if (!f.exists() && !abbExist) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        Commit oldC = Utils.readObject(f, Commit.class);
        HashMap<String, String> old = oldC.getFiles();
        if (old.get(fil) == null) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }
        String fileHash = old.get(fil);
        File cwdVersion = new File(CWD + SEP + fil);
        File newStuff = new File(BLOBS + SEP + fileHash);
        Utils.writeContents(cwdVersion, Utils.readContents(newStuff));
        currentCommit = oldC;
        saveRepo();
    }

    /** checkout to the head commit of given branch.
     * @param nam */
    public void checkoutBranch(String nam) {
        File branch = new File(BRANCHFILES + SEP + nam);
        if (!branch.exists()) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        Branch checkout = Utils.readObject(branch, Branch.class);
        if (nam.equals(current.getName())) {
            Utils.message("No need to checkout the current branch");
            System.exit(0);
        }
        Commit checkHead = checkout.getHeadCommit();
        HashMap<String, String> checks = checkHead.getFiles();
        for (Map.Entry<String, String> entry: checks.entrySet()) {
            File inCWD = new File(CWD + SEP + entry.getKey());
            if (inCWD.exists()) {
                HashMap<String, String> headF = currentCommit.getFiles();
                if (!headF.containsKey(entry.getKey())) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first. ");
                    System.exit(0);
                }
            }
            File c1 = new File(BLOBS + SEP + entry.getValue());
            Utils.writeContents(inCWD, Utils.readContents(c1));
        }
        File inCWD2 = new File(CWD + SEP);
        for (String n: Utils.plainFilenamesIn(inCWD2)) {
            if (!checkHead.getFiles().containsKey(n)) {
                File del = new File(inCWD2 + SEP + n);
                del.delete();
            }
        }
        if (!current.getName().equals(nam)) {
            clearStage();
        }
        current = checkout;
        current.setHeadCommit(checkHead);
        currentCommit = checkHead;
    }


    /** remove file given.
     * @param fileName */
    public void rmF(String fileName) {
        File f = new File(STAGEFILES + SEP + fileName);
        if (f.exists()) {
            f.delete();
            return;
        }
        if (currentCommit.getFiles().containsKey(fileName)) {
            File blobInCWD = new File(CWD + SEP + fileName);
            File toRemove = new File(REMOVE + SEP + fileName);
            if (blobInCWD.exists()) {
                Utils.writeContents(toRemove, Utils.readContents(blobInCWD));
            } else {
                String hash = currentCommit.getFiles().get(fileName);
                File fromPrev = new File(BLOBS + SEP + hash);
                Utils.writeContents(toRemove, Utils.readContents(fromPrev));
            }
            blobInCWD.delete();
        } else {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }
        saveRepo();
    }

    /** global log command - order dont matter. */
    public void globallog() {
        String result = "";
        for (String s: Utils.plainFilenamesIn(COMMITFILES)) {
            File f = new File(COMMITFILES + SEP + s);
            Commit c = Utils.readObject(f, Commit.class);
            result += c.logString();
            result += System.lineSeparator();
        }
        Utils.message(result);
    }

    /** find commits with given message.
     * @param message */
    public void find(String message) {
        boolean found = false;
        for (String s: Utils.plainFilenamesIn(COMMITFILES)) {
            File f = new File(COMMITFILES + SEP + s);
            Commit c = Utils.readObject(f, Commit.class);
            if (c.getMessage().equals(message)) {
                System.out.println(c.getHash());
                found = true;
            }
        }
        if (!found) {
            Utils.message("Found no commit with that message.");
        }
    }

    /** get status of all files atm. */
    public void status() {
        if (!GITLET.exists()) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        System.out.println("=== Branches ===");
        for (String name: Utils.plainFilenamesIn(BRANCHFILES)) {
            if (name.equals("master")) {
                System.out.println("*" + name);
                continue;
            }
            System.out.println(name);
        }
        System.out.println("\n=== Staged Files ===");
        Utils.plainFilenamesIn(STAGEFILES).forEach(System.out::println);
        System.out.println("\n=== Removed Files ===");
        Utils.plainFilenamesIn(REMOVE).forEach(System.out::println);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String file: Utils.plainFilenamesIn(CWD)) {
            boolean last = current.getHeadCommit().getFiles().containsKey(file);
            File stage = new File(STAGEFILES + SEP + file);
            boolean inRem = Utils.plainFilenamesIn(REMOVE).contains(file);
            File cwdF = new File(CWD + SEP + file);
            String shaCWD = Utils.sha1(Utils.readContents(cwdF));
            if (!file.equals(".repo") && last && !stage.exists() && !inRem) {
                String hashCurr = current.getHeadCommit().getFiles().get(file);
                File currHead = new File(BLOBS + SEP + hashCurr);
                String shaCommit = Utils.sha1(Utils.readContents(currHead));
                if (shaCommit.compareTo(shaCWD) != 0) {
                    System.out.println(file + " (modified)");
                }
            } else if (stage.exists()) {
                String stageHash = Utils.sha1(Utils.readContents(stage));
                if (shaCWD.compareTo(stageHash) != 0) {
                    System.out.println(file);
                }
            }
        }
        for (String staged: Utils.plainFilenamesIn(STAGEFILES)) {
            if (!new File(CWD + SEP + staged).exists()) {
                System.out.println(staged + " (deleted)");
            }
        }
        HashMap<String, String> h = current.getHeadCommit().getFiles();
        for (Map.Entry<String, String> f: h.entrySet()) {
            if (!Utils.plainFilenamesIn(CWD).contains(f.getKey())
                    && !Utils.plainFilenamesIn(REMOVE).contains(f.getKey())) {
                System.out.println(f.getKey() + " (deleted)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        for (String o: Utils.plainFilenamesIn(CWD)) {
            if (!o.equals("h.txt") && !o.equals(".repo") && !h.containsKey(o)
                    && !Utils.plainFilenamesIn(STAGEFILES).contains(o))  {
                System.out.println(o);
            }
        }
        System.out.println();
    }

    /** make a new branch w given branch name.
     * @param branchname */
    public void branch(String branchname) {
        File branchExists = new File(BRANCHFILES + SEP + branchname);
        if (branchExists.exists()) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        ArrayList<Branch> parents = new ArrayList<>();
        parents.add(current);
        Branch newest = new Branch(branchname, parents, current.getCommits());
        newest.serialize();
    }

    /** remove an entire branch.
     * @param branchname  */
    public void rmbranch(String branchname) {
        File branch = new File(BRANCHFILES + SEP + branchname);
        if (!branch.exists()) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }

        Branch b = Utils.readObject(branch, Branch.class);
        if (b.getName().equals(current.getName())) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        branch.delete();
    }

    /** reset to a given commitID.
     * @param commitID  */
    public void reset(String commitID) {
        File commission = new File(COMMITFILES + SEP + commitID);
        if (!commission.exists()) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = Utils.readObject(commission, Commit.class);
        for (Map.Entry<String, String> entry: c.getFiles().entrySet()) {
            File inCWD = new File(CWD + SEP + entry.getKey());
            if (inCWD.exists()) {
                HashMap<String, String> currF = currentCommit.getFiles();
                if (!currF.containsKey(entry.getKey())) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first. ");
                    System.exit(0);
                }
                String blobID = currF.get(entry.getKey());
                File overwriter = new File(BLOBS + SEP + blobID);
                String shaBlob = Utils.sha1(Utils.readContents(overwriter));
                String shaCWD = Utils.sha1(Utils.readContents(inCWD));
                if (shaBlob.compareTo(shaCWD) != 0) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first. ");
                    System.exit(0);
                }
            }
        }
        for (Map.Entry<String, String> entry: c.getFiles().entrySet()) {
            checkoutCommit(commitID, entry.getKey());
        }
        clearStage();
        current.setHeadCommit(c);
        File branchUpdate = new File(BRANCHFILES + SEP + current.getName());
        Utils.writeObject(branchUpdate, current);
    }

    /** merge two branches, the given and the current.
     * @param branchName */
    public void merge(String branchName) {
        File branchFile = new File(BRANCHFILES + SEP + branchName);
        if (!branchFile.exists()) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch given = Utils.readObject(branchFile, Branch.class);
        Commit currComm = current.getHeadCommit();
        Commit givComm = given.getHeadCommit();
        Commit split = findSplitPoint(currComm, givComm);
        basicMergeCases(split, given, current);
        HashMap<String, String> cf = currComm.getFiles();
        HashMap<String, String> givFiles = givComm.getFiles();
        HashMap<String, String> sf = split.getFiles();
        mergeHelper1(currComm, givComm, split);
        for (Map.Entry<String, String> e: givFiles.entrySet()) {
            File f = new File(CWD + SEP + e.getKey());
            File givF = new File(BLOBS + SEP + e.getValue());
            String k = e.getKey();
            if (!cf.containsKey(k) && !sf.containsKey(k)) {
                Utils.writeContents(f, Utils.readContents(givF));
                stage(e.getKey());
            }
            if (!sf.containsKey(k) && cf.containsKey(k)
                    && givFiles.containsKey(k)) {
                String hash = cf.get(e.getKey());
                File curr = new File(BLOBS + SEP + hash);
                String currID = Utils.readContentsAsString(curr);
                String givID = Utils.readContentsAsString(givF);
                if (currID.compareTo(givID) != 0) {
                    mergeConflict(curr, givF, e.getKey());
                    stage(e.getKey());
                }
            }
        }
        saveRepo();
        mergeCommit(current, given);
    }

    /** merge helper takes in CURRCOMM, GIVCOMM, SPLIT. */
    public void mergeHelper1(Commit currComm, Commit givComm, Commit split) {
        HashMap<String, String> cf = currComm.getFiles();
        HashMap<String, String> givFiles = givComm.getFiles();
        HashMap<String, String> sf = split.getFiles();
        for (Map.Entry<String, String> fileMap: sf.entrySet()) {
            String fileName = fileMap.getKey();
            String currBlobID = cf.get(fileName);
            File currBlob = new File(BLOBS + SEP + currBlobID);
            String currToCompare = null;
            if (currBlob.exists()) {
                currToCompare = Utils.sha1(Utils.readContents(currBlob));
            }
            String givBlobID = givFiles.get(fileName);
            File givBlob = new File(BLOBS + SEP + givBlobID);
            String givToCompare = null;
            if (givBlob.exists()) {
                givToCompare = Utils.sha1(Utils.readContents(givBlob));
            }
            String splitBlobID = sf.get(fileName);
            File splitBlob = new File(BLOBS + SEP + splitBlobID);
            String splitToCompare = Utils.sha1(Utils.readContents(splitBlob));

            if (currToCompare != null && givToCompare != null
                    && givToCompare.compareTo(splitToCompare) != 0
                    && currToCompare.compareTo(splitToCompare) == 0) {
                File inCWD = new File(CWD + SEP + fileName);
                Utils.writeContents(inCWD, Utils.readContents(givBlob));
                stage(inCWD.getName());
                continue;
            }
            if (currToCompare != null && givToCompare == null
                    && currToCompare.compareTo(splitToCompare) == 0) {
                File inCWD = new File(CWD + SEP + fileName);
                inCWD.delete();
                currComm.getFiles().remove(fileName);
                continue;
            }
            if (currToCompare != null && givToCompare != null
                    && givToCompare.compareTo(splitToCompare) == 0
                    && currToCompare.compareTo(splitToCompare) != 0) {
                continue;
            }
            boolean bothExist = (currToCompare != null && givToCompare != null
                    && currToCompare.compareTo(givToCompare) != 0);
            boolean givenC = (currToCompare != null && givToCompare == null
                    && currToCompare.compareTo(splitToCompare) != 0);
            boolean currC = (givToCompare != null && currToCompare == null
                    && givToCompare.compareTo(splitToCompare) != 0);
            if (bothExist | givenC | currC) {
                mergeConflict(currBlob, givBlob, fileName);
                stage(fileName);
            }
        }
        saveRepo();
    }
    /** RETURN the COMMIT latest common ancestor of CURRENTS and B. */
    public Commit findSplitPoint(Commit currents, Commit b) {
        HashMap<String, String> bCurr = new HashMap<String, String>();
        HashMap<String, String> visitedBranch2 = new HashMap<>();
        visitedBranch2.put(b.getHash(), b.getMessage());
        splitHelper(b, visitedBranch2);
        PriorityQueue<Commit> findSplit = new PriorityQueue<>();
        findSplit.add(currents);
        while (!findSplit.isEmpty()) {
            Commit f = findSplit.poll();
            if (f.getParents() != null && !bCurr.containsKey(f.getHash())) {
                bCurr.put(f.getHash(), f.getMessage());
                if (visitedBranch2.containsKey(f.getHash())) {
                    return f;
                }
                for (int i = f.getParents().size(); i > 0; i -= 1) {
                    Commit parent = f.getParents().get(i - 1);
                    if (visitedBranch2.containsKey(parent.getHash())) {
                        return parent;
                    }
                    if (!bCurr.containsKey(parent.getHash())) {
                        findSplit.add(parent);
                    }
                }
            }
        }
        return null;
    }

    /** helper for the split function with VISITED and CURR. */
    public void splitHelper(Commit curr, HashMap<String, String> visited) {
        if (curr.getParents() == null) {
            visited.put(curr.getHash(), curr.getMessage());
            return;
        } else {
            Commit parent0 = curr.getParents().get(0);
            if (!visited.containsKey(parent0.getHash())) {
                visited.put(parent0.getHash(), parent0.getMessage());
            }
            splitHelper(parent0, visited);
        }
        if (curr.getParents().size() > 1) {
            Commit parent1 = curr.getParents().get(1);
            if (!visited.containsKey(parent1.getHash())) {
                visited.put(parent1.getHash(), parent1.getMessage());
            }
            splitHelper(parent1, visited);
        }
    }

    /** v basic cases for merge -- errors and fast forwarding.
     * split commit SPLIT merge GIVEN CUR */
    public void basicMergeCases(Commit split, Branch given, Branch cur) {
        if (given.getName().equals(cur.getName())) {
            Utils.message("Cannot merge a branch with itself");
            System.exit(0);
        }
        if (split.getDate().equals(given.getHeadCommit().getDate())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (split.getDate().equals(cur.getHeadCommit().getDate()))  {
            checkoutBranch(given.getName());
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (String name:Utils.plainFilenamesIn(CWD)) {
            if (!Utils.plainFilenamesIn(STAGEFILES).isEmpty()) {
                Utils.message("You have uncommitted changes.");
                System.exit(0);
            }
            if (given.getHeadCommit().getFiles().containsKey(name)
                    && !cur.getHeadCommit().getFiles().containsKey(name)) {
                Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /** deal with a conflict by writing conflict files
     * for GIV FILEN CURR. */
    public void mergeConflict(File curr, File giv, String filen) {
        Utils.message("Encountered a merge conflict.");
        String message = "";
        message += "<<<<<<< HEAD\n";
        if (curr.exists()) {
            message += Utils.readContentsAsString(curr);
        }
        message += "=======\n";
        if (giv.exists()) {
            message += Utils.readContentsAsString(giv);
        }
        message += ">>>>>>>\n";
        if (curr.exists()) {
            File inCWD = new File(CWD + SEP + filen);
            Utils.writeContents(inCWD, message);
        }
    }

    /** make a commit thats a merge between CUR GIVEN. */
    public void mergeCommit(Branch cur, Branch given) {
        String msg = "Merged " + given.getName() + " into "
                + cur.getName() + ".";
        commit(msg, given.getHeadCommit());
    }

    /** add a remote directory DIR with USERNAME. */
    public void addRemote(String username, String dir) {
        File remoteDir = new File(REMOTES + SEP + username);
        if (remoteDir.exists()) {
            Utils.message("A remote with that name already exists.");
            System.exit(0);
        } else {
            String thePath = dir.replace("/", SEP);
            Utils.writeContents(remoteDir, thePath);
        }
    }

    /** remove a remote address of RM. */
    public void rmRemote(String rm) {
        File remote = new File(REMOTES + SEP + rm);
        if (!remote.exists()) {
            Utils.message("A remote with that name does not exist.");
            System.exit(0);
        }
        remote.delete();
    }

    /** push from current head to repo RM head of BRANCH. */
    public void push(String rm, String branch) {
        File remote = new File(REMOTES + SEP + rm);
        String path = Utils.readContentsAsString(remote);
        File exists = new File(path);
        if (!exists.exists()) {
            Utils.message("Remote directory not found");
            System.exit(0);
        }
        File orgPath = new File(path);
        File parentDir = new File(orgPath.getParent());
        String remP = parentDir + SEP + ".repo";
        Repo remoteRepo = Utils.readObject(new File(remP), Repo.class);
        String remB = orgPath + SEP + ".branches/" + SEP + branch;
        File rb = new File(remB);
        if (!rb.exists()) {
            ArrayList<Commit> commits = new ArrayList<>();
            ArrayList<Branch> parents = new ArrayList<>();
            Branch newBranch = new Branch(branch, parents, commits);
            newBranch.serialize();
        }
        Branch remoteBranch = Utils.readObject(rb, Branch.class);
        remoteRepo.setCurrent(remoteBranch);
        Commit headRepo = remoteBranch.getHeadCommit();
        Commit copyCurr = current.getHeadCommit();
        ArrayList<Commit> futureCommits = new ArrayList<>();
        boolean found = false;
        while (copyCurr.getParents() != null) {
            if (copyCurr.getHash().equals(headRepo.getHash())) {
                found = true;
                break;
            } else {
                futureCommits.add(copyCurr);
            }
            copyCurr = copyCurr.getParents().get(0);
        }
        if (!found) {
            Utils.message("Please pull down remote changes before pushing.");
        }
        for (Commit fut: futureCommits) {
            remoteBranch.addCommit(fut);
            remoteBranch.setHeadCommit(fut);
            remoteRepo.setCommitCurrent(fut);
            File loc = new File(COMMITFILES + SEP + fut.getHash());
            File rem = new File(orgPath + SEP + ".commits"
                    + SEP + fut.getHash());
            Utils.writeContents(rem, Utils.readContents(loc));
            HashMap<String, String> bleb = fut.getFiles();
            for (Map.Entry<String, String> b: bleb.entrySet()) {
                File r = new File(orgPath + SEP + ".blobs"
                        + SEP + b.getValue());
                File l = new File(BLOBS + SEP + b.getValue());
                Utils.writeContents(r, Utils.readContents(l));
            }
        }
        Utils.writeObject(rb, remoteBranch);
        File repo = new File(parentDir + SEP + ".repo");
        Utils.writeObject(repo, remoteRepo);
    }

    /** fetch from a remote RM repository BRANCH to the current. */
    public void fetch(String rm, String branch) {
        File remote = new File(REMOTES + SEP + rm);
        String path = Utils.readContentsAsString(remote);
        File exists = new File(path);
        if (!exists.exists()) {
            Utils.message("Remote directory not found.");
            System.exit(0);
        }
        File og = new File(path + SEP);
        File remoteBranchFile = new File(og + SEP
                + ".branches" + SEP + branch);
        if (!remoteBranchFile.exists()) {
            Utils.message("That remote does not have that branch.");
            System.exit(0);
        }
        Branch remoteVersion = Utils.readObject(remoteBranchFile, Branch.class);
        String newName = rm + "/" + branch;
        File local = new File(BRANCHFILES + SEP + newName);
        File before = new File(BRANCHFILES + SEP + rm);
        if (!local.exists()) {
            before.mkdir();
            branch(newName);
        }
        local = new File(BRANCHFILES + SEP + newName);
        Branch localVersion = Utils.readObject(local, Branch.class);
        for (Commit toAdd: remoteVersion.getCommits()) {
            String h = toAdd.getHash();
            if (!Utils.plainFilenamesIn(COMMITFILES).contains(h)) {
                localVersion.addCommit(toAdd);
                localVersion.setHeadCommit(toAdd);
                toAdd.serialize();
                HashMap<String, String> theBlobs = toAdd.getFiles();
                for (Map.Entry<String, String> cop: theBlobs.entrySet()) {
                    String p = og + SEP + ".blobs" + SEP + cop.getValue();
                    File remoteBlobs = new File(p);
                    File loc = new File(BLOBS + SEP + cop.getValue());
                    Utils.writeContents(loc, Utils.readContents(remoteBlobs));
                }
            }
        }
        Utils.writeObject(local, localVersion);
        saveRepo();
    }

    /** pull from REMOTE REMOTEBRANCH command. */
    public void pull(String remote, String remotebranch) {
        fetch(remote, remotebranch);
        merge(remote + "/" + remotebranch);
    }
}
