package gitlet;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Commits stuff.
 * @author Manaal */
public class Commit implements Serializable, Comparable {
    /** hash code of commit. */
    private String hash;
    /** list of parents. */
    private ArrayList<Commit> parents;
    /** timestamp of commit. */
    private String timestamp;
    /** hashmap representing mapping of blobs ofcommit. */
    private HashMap<String, String> blobs;
    /** message asst. w commit. */
    private String message;
    /** abbreviated commit hash. */
    private String abbrv;
    /** whether or not its a merge commit. */
    private boolean merged;
    /** timestamp pattern. */
    private static String a = "EEE MMM d HH:mm:ss yyyy Z";
    /** timestamp formatter. */
    private static SimpleDateFormat f = new SimpleDateFormat(a);
    /** commits directory. */
    private static final File COMMITFILES = new File(".gitlet/.commits");
    /** CWD. */
    private static final File CWD = new File(".");

    /**default constructor for initial commit. */
    public Commit() {
        message = "initial commit";
        parents = null;
        merged = false;
        blobs = new HashMap<>();
        Date initial = new Date(0);
        timestamp = f.format(initial);
        hash = hash();
        abbrv = hash.substring(0, 8);
    }

    /** non initial commit constructor.
     * takes in M and P and B and sets as properties */
    public Commit(String m, ArrayList<Commit> p, HashMap<String, String> b) {
        blobs = new HashMap<>();
        if (!p.isEmpty() && !p.get(0).getFiles().isEmpty()) {
            setFiles(p.get(0));
        }
        for (Map.Entry<String, String> entry: b.entrySet()) {
            if (blobs != null && blobs.containsKey(entry.getKey())) {
                blobs.replace(entry.getKey(), entry.getValue());
            } else {
                blobs.put(entry.getKey(), entry.getValue());
            }
        }
        message = m;
        parents = p;
        if (message == null || message.equals("")) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        if (p.size() > 1) {
            merged = true;
        }
        Date currentDate = new Date();
        hash = hash();
        timestamp = f.format(currentDate);
        abbrv = hash.substring(0, 8);
    }

    /** make a copy of a commit object.
     * @param c  */
    Commit(Commit c) {
        hash = c.hash;
        parents = c.getParents();
        timestamp = c.getDate();
        blobs = c.getFiles();
        message = c.getMessage();
        abbrv = c.getAbbrv();
        merged = c.isMerge();
    }

    /** set files.
     * based on PARENT files */
    @SuppressWarnings("unchecked")
    public void setFiles(Commit parent) {
        this.blobs = (HashMap<String, String>) parent.getFiles().clone();
    }

    /** serialize and save the commit. */
    public void serialize() {
        File outFile = new File(COMMITFILES + File.separator + this.hash);
        Utils.writeObject(outFile, this);
    }

    /** serialize and save the blobs. */
    public void serializeBlobs() {
        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            String p1 = ".gitlet/.blobs/" + File.separator + entry.getValue();
            File out = new File(p1);
            String p2 = CWD + File.separator + entry.getKey();
            Utils.writeContents(out, Utils.readContents(new File(p2)));
        }
    }

    /** set parent 0.
     * @param c */
    public void setParents(Commit c) {
        parents.set(0, c);
    }

    /** is the commit a merge.
     * @return boolean */
    public boolean isMerge() {
        return merged;
    }

    /** get message of commit.
     * @return String*/
    public String getMessage() {
        return message;
    }

    /** return timestamp of commit.
     * @return String*/
    public String getDate() {
        return timestamp;
    }

    /** compute hash code of commit.
     * @return String*/
    public String hash() {
        return Utils.sha1(message + parents + timestamp + blobs);
    }

    /** get hash code of commit.
     * @return String */
    public String getHash() {
        return this.hash;
    }

    /** get list of parents.
     * @return ArrayList*/
    public ArrayList<Commit> getParents() {
        return parents;
    }

    /** get file map.
     * @return HashMap */
    public HashMap<String, String> getFiles() {
        return blobs;
    }

    /** print out info about commit.
     * @return String */
    public String logString() {
        String log = "";
        log += "===\n";
        log += "commit" + " " + this.hash + "\n";
        if (merged) {
            log += "Merge: ";
            log += getParents().get(0).getAbbrv().substring(0, 7);
            log += " ";
            log += getParents().get(1).getAbbrv().substring(0, 7) + "\n";
        }
        log += "Date:" + " " + this.timestamp + "\n";
        log += this.getMessage();
        log += "\n";
        return log;
    }

    /** get abbrev commit.
     * @return String*/
    public String getAbbrv() {
        return abbrv;
    }

    @Override
    public int compareTo(Object other) {
        return 0;
    }
}
