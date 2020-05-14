package gitlet;

/** initial stage.
 * @author Manaal */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Commands myCommands = new Commands();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            myCommands.initCommand(); break;
        case "add":
            myCommands.addCommand(args[1]); break;
        case "commit":
            myCommands.commitCommand(args[1]); break;
        case "rm":
            myCommands.rmCommand(args[1]); break;
        case "log":
            myCommands.logCommand(); break;
        case "global-log":
            myCommands.globalLogCommand();
            break;
        case "find":
            myCommands.findCommand(args[1]);
            break;
        case "status":
            myCommands.statusCommand();
            break;
        case "checkout":
            myCommands.checkoutCommand(args);
            break;
        case "branch":
            myCommands.branchCommand(args[1]);
            break;
        case "rm-branch":
            myCommands.rmBranchCommand(args[1]);
            break;
        case "reset":
            myCommands.resetCommand(args[1]);
            break;
        case "merge":
            myCommands.mergeCommand(args[1]);
            break;
        case "add-remote":
            myCommands.addRemoteCommand(args[1], args[2]);
            break;
        case "rm-remote":
            myCommands.rmRemoteCommand(args[1]);
            break;
        case "push":
            myCommands.pushCommand(args[1], args[2]);
            break;
        case "fetch":
            myCommands.fetchCommand(args[1], args[2]); break;
        case "pull":
            myCommands.pullCommand(args[1], args[2]); break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
