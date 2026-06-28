package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Sanya Kwatra
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            CommandsClass.init(args);
            break;
        case "add":
            CommandsClass.add(args);
            break;
        case "commit":
            CommandsClass.commit(args);
            break;
        case "checkout":
            CommandsClass.checkout(args);
            break;
        case "log":
            CommandsClass.log(args);
            break;
        case "rm":
            CommandsClass.remove(args);
            break;
        case "global-log":
            CommandsClass.globalLog(args);
            break;
        case "find":
            CommandsClass.find(args);
            break;
        case "status":
            CommandsClass.status(args);
            break;
        case "branch":
            CommandsClass.branch(args);
            break;
        case "rm-branch":
            CommandsClass.rmBranch(args);
            break;
        case "reset":
            CommandsClass.reset(args);
            break;
        case "merge":
            CommandsClass.merge(args);
            break;

        default:
            System.out.println("No command with that name exists.");
        }
        return;
    }





}
