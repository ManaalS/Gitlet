package gitlet;
import java.io.Serializable;
import java.util.ArrayList;
import java.io.File;

/** Branch class for commit tree.
 * @author Manaal */
public class Branch implements Serializable {

    /** name of branch. */
    private String name;
    /** head commit of branch. */
    private Commit headCommit;
    /** parents of branch. */
    private ArrayList<Branch> parents;
    /** list of commits in branch. */
    private ArrayList<Commit> branchCommits;
    /** is this the current branch. */
    private boolean current;
    /** the current branch. */
    private Branch currentBranch;

    /**default constructor for initial master.
     * @param c */
    public Branch(Commit c) {
        branchCommits = new ArrayList<>();
        name = "master";
        parents = null;
        headCommit = c;
        branchCommits.add(c);
        setCurrent(this);
    }

    /** make a Branch with name N, parents.
     * BPARENT and commits COMM */
    public Branch(String n, ArrayList<Branch> bparent, ArrayList<Commit> comm) {
        name = n;
        parents = bparent;
        branchCommits = comm;
        if (!comm.isEmpty()) {
            headCommit = comm.get(comm.size() - 1);
        }
        setCurrent(this);
    }

    /** save and serialize. */
    public void serialize() {
        File out = new File(".gitlet/.branches/" + File.separator + this.name);
        Utils.writeObject(out, this);
    }

    /** set this branch as the current.
     * @param b */
    public void setCurrent(Branch b) {
        currentBranch = b;
    }

    /** add commit to this branch.
     * @param c */
    public void addCommit(Commit c) {
        branchCommits.add(c);
    }
    /** is this the current branch.
     * @return ArrayList<Commit> */
    public ArrayList<Commit> getCommits() {
        return branchCommits;
    }

    /** get head of branch.
     * @return Commit */
    public Commit getHeadCommit() {
        return headCommit;
    }

    /** get branch name.
     * @return String*/
    public String getName() {
        return name;
    }

    /** set head commit of the branch.
     * @param c  */
    public void setHeadCommit(Commit c) {
        headCommit = c;
    }
}
