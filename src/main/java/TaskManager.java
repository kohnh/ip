import java.util.Scanner;

public class TaskManager {
    public static final int TASKS_SIZE = 100;
    public static final String DASH_SEPARATOR = "------------------------------------------------------------\n";
    public static Task[] tasks = new Task[TASKS_SIZE];
    private static int oneBasedIndex = 1;
    public static void formatOutput(String stringToOutput) {
        System.out.println(DASH_SEPARATOR + stringToOutput + System.lineSeparator() + DASH_SEPARATOR);
    }
    public static void printMark(Task task, boolean done) {
        formatOutput(task.markDone(done));
    }
    public static void printTask(Task task) {
        formatOutput("Got it. I've added this task:" + System.lineSeparator()
                + task + System.lineSeparator() + "Now you have " + oneBasedIndex
                + " tasks in the list.");
        oneBasedIndex++;
    }

    public static void printList() {
        String s = "";
        s += "Here are the tasks in your list:" + System.lineSeparator();
        for (int i = 1; i < oneBasedIndex; i++) {
            s += String.valueOf(i) + "." + tasks[i] + System.lineSeparator();
        }
        formatOutput(s);
    }

    public static void receiveCommands() {
        Scanner in = new Scanner(System.in);
        String command = in.nextLine().trim();
        while (!command.equals("bye")) {
            boolean isList = command.equals("list");
            if (isList) {
                printList();
            } else {
                tryCommand(command);
            }
            command = in.nextLine().trim();
        }
    }

    private static void tryCommand(String command) {
        String firstWord = "";
        try {
            firstWord = command.substring(0, command.indexOf(' '));
            doCommand(command, firstWord);
        } catch (StringIndexOutOfBoundsException e) {
            try {
                checkExceptions(command);
            }catch (EmptyException ee) {
                formatOutput("☹ OOPS!!! The description of a " + command + " cannot be empty.");
            } catch (WrongCommandException ee) {
                formatOutput("☹ OOPS!!! I'm sorry, but I don't know what that means :-(");
            }
        } catch (WrongCommandException e) {
            formatOutput("☹ OOPS!!! I'm sorry, but I don't know what that means :-(");
        } catch (TaskOutOfBoundsException e) {
            formatOutput("☹ OOPS!!! The task number you specified does not exist.");
        } catch (NoBackslashException e) {
            formatOutput("☹ OOPS!!! You did not specify a deadline OR event datetime.");
        }
    }

    private static void doCommand(String command, String firstWord)
            throws WrongCommandException, TaskOutOfBoundsException, NoBackslashException {
        switch (firstWord) {
        case "mark":
            int pos = Integer.parseInt(command.substring("mark ".length()));
            tryMark(pos, true);
            break;
        case "unmark":
            pos = Integer.parseInt(command.substring("unmark ".length()));
            tryMark(pos, false);
            break;
        case "todo":
            tasks[oneBasedIndex] = new Todo(command, ' ');
            printTask(tasks[oneBasedIndex]);
            break;
        case "deadline":
            tryDeadline(command);
            break;
        case "event":
            tryEvent(command);
            break;
        default:
            throw new WrongCommandException();
        }
    }

    private static void tryEvent(String command) throws NoBackslashException {
        if (command.contains("/")) {
            tasks[oneBasedIndex] = new Event(command, '/');
            printTask(tasks[oneBasedIndex]);
        } else {
            throw new NoBackslashException();
        }
    }

    private static void tryDeadline(String command) throws NoBackslashException {
        if (command.contains("/")){
            tasks[oneBasedIndex] = new Deadline(command, '/');
            printTask(tasks[oneBasedIndex]);
        } else {
            throw new NoBackslashException();
        }
    }

    private static void tryMark(int pos, boolean done) throws TaskOutOfBoundsException {
        if (pos >= oneBasedIndex || pos < 1) {
            throw new TaskOutOfBoundsException();
        }
        printMark(tasks[pos], done);
    }

    private static void checkExceptions(String command) throws EmptyException, WrongCommandException {
        if (command.equals("mark") ||
                command.equals("unmark") ||
                command.equals("todo") ||
                command.equals("deadline") ||
                command.equals("event")) {
            throw new EmptyException();
        } else {
            throw new WrongCommandException();
        }
    }
}
