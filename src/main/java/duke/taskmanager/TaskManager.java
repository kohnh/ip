package duke.taskmanager;

import duke.exceptions.EmptyException;
import duke.exceptions.NoBackslashException;
import duke.exceptions.TaskOutOfBoundsException;
import duke.exceptions.WrongCommandException;
import duke.taskmanager.tasks.Deadline;
import duke.taskmanager.tasks.Event;
import duke.taskmanager.tasks.Task;
import duke.taskmanager.tasks.Todo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TaskManager {
    public static final int TASKS_SIZE = 100;
    public static final String DASH_SEPARATOR = "------------------------------------------------------------\n";
    public static Task[] tasks = new Task[TASKS_SIZE];

    private static int oneBasedIndex = 1;

    public TaskManager() {
        try {
            File f = new File("data/duke.txt"); // create a File for the given file path
            Scanner s = new Scanner(f); // create a Scanner using the File as the source
            while (s.hasNext()) {
                String line = s.nextLine();
                StringBuilder command = new StringBuilder();
                switch (line.charAt(1)) {
                case 'T':
                    command.append("todo");
                    command.append(line.substring("[ ][ ]".length()));
                    break;
                case 'D':
                    command.append("deadline");
                    command.append(line.substring("[ ][ ]".length(), line.indexOf('('))).append("/by ");
                    command.append(line.substring(line.indexOf('(') + "(by: ".length(),line.indexOf(')')));
                    break;
                case 'E':
                    command.append("event");
                    command.append(line.substring("[ ][ ]".length(), line.indexOf('('))).append("/at ");
                    command.append(line.substring(line.indexOf('(') + "(at: ".length(),line.indexOf(')')));
                    break;
                default:
                    break;
                }
                System.out.println(command);
                tryCommand(String.valueOf(command));
                Character mark = line.charAt(4);
                if (mark.equals('X')) {
                    tryCommand("mark " + (oneBasedIndex - 1));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found sorry.");
        }

    }

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
        StringBuilder s = new StringBuilder();
        s.append("Here are the tasks in your list:").append(System.lineSeparator());
        for (int i = 1; i < oneBasedIndex; i++) {
            s.append(i).append(".").append(tasks[i]).append(System.lineSeparator());
        }
        formatOutput(s.toString());
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
        try {
            saveTasks();
        } catch (IOException e) {
            System.out.println("I/O error...");
        }
    }

    private static void saveTasks() throws IOException {
        File file = new File("data/duke.txt");
        FileWriter fw;
        file.getParentFile().mkdirs();
        file.createNewFile();
        fw = new FileWriter(file);
        StringBuilder textToAppend = new StringBuilder();
        for (int i = 1; i < oneBasedIndex; i++) {
            textToAppend.append(tasks[i]).append(System.lineSeparator());
        }
        fw.write(String.valueOf(textToAppend));
        fw.close();
    }

    private static void tryCommand(String command) {
        String firstWord;
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
            tryMark(command, true);
            break;
        case "unmark":
            tryMark(command, false);
            break;
        case "todo":
            tasks[oneBasedIndex] = new Todo(command, ' ');
            printTask(tasks[oneBasedIndex]);
            break;
        case "deadline":
            //Fallthrough
        case "event":
            tryDeadlineOrEvent(command,firstWord);
            break;
        default:
            throw new WrongCommandException();
        }
    }

    private static void tryDeadlineOrEvent(String command, String firstWord) throws NoBackslashException {
        if (command.contains("/")){
            switch (firstWord) {
            case "deadline":
                tasks[oneBasedIndex] = new Deadline(command, '/');
                break;
            case "event":
                tasks[oneBasedIndex] = new Event(command, '/');
                break;
            }
            printTask(tasks[oneBasedIndex]);
        } else {
            throw new NoBackslashException();
        }
    }

    private static void tryMark(String command, boolean done) throws TaskOutOfBoundsException {
        try {
            int startIdx = command.substring(0, command.indexOf(' ') + 1).length();
            int pos = Integer.parseInt(command.substring(startIdx));
            if (pos >= oneBasedIndex || pos < 1) {
                throw new TaskOutOfBoundsException();
            }
            printMark(tasks[pos], done);
        } catch (NumberFormatException e) {
            formatOutput("☹ OOPS!!! You did not enter a number.");
        }
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
