/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Review, text based code analyzer
 * Copyright (C) 2022 Seanox Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

/**
 * Review, a text based code analyzer.
 *
 * @author  Seanox Software Solutions
 * @version 1.5.0 20221024
 */
public class Review {

    /** list of files*/
    private volatile static List<File> queue;

    /** list/queue of tasks */
    private volatile static List<Task> tasks;

    /** list of workers*/
    private volatile static List<Worker> workers;

    /** number of sites */
    private volatile static long founds;
    
    /** number of corrections */
    private volatile static long corrections;
    
    /** start time */
    private volatile static long timing;

    /** amount of processed data (bytes) */
    private volatile static long volume;
    
    /** amount of processed files */
    private volatile static long files;
    
    /** number of occurring errors */
    private volatile static long errors;
    
    /** number of performed reviews */
    private volatile static long reviews;

    /** number of workers/threads */
    private static final int THREADS = 25;
    
    /** Internal class for managing application arguments */
    private static class Options {
        
        /** (De)Activation of the replacement */
        private static boolean replace;

        /** (De)Activation of the help output */
        private static boolean help;
    }
    
    /**
     * Writes a message to the system output stream.
     * @param message message
     */
    private static synchronized void print(Object message) {
        if (message instanceof Throwable) {
            StringWriter writer = new StringWriter();
            ((Throwable)message).printStackTrace(new PrintWriter(writer));
            message = writer.toString();
        } else message = String.valueOf(message);
        synchronized (Review.class) {
            System.out.println(((String)message).replaceAll("[\\r\\n\\s]+$", ""));
        }
    }

    /**
     * Writes a formatted message to the system output stream.
     * @param message message
     * @param values  value(s) 
     */
    private static void print(String message, Object... values) {
        Review.print(String.format(message, values));
    }

    /**
     * Writes the contents in a file.
     * @param  file  file
     * @param  bytes content
     * @throws IOException
     *     In the case of the failed file access.
     */
    private static void writeFile(File file, byte[] bytes)
            throws IOException {
        Files.write(file.toPath(), bytes);
        Review.volume += bytes.length;
    }
    
    /**
     * Reads all tasks from a file.
     * @param  file anti-pattern file
     * @return array of created and prepared task
     * @throws IOException
     *     In the case of the failed file access.
     * @throws ReviewParserException
     *     In case of invalid syntax or structure.
     */    
    private static Task[] readTasks(File file)
            throws IOException, ReviewParserException {
        
        String[] lines = new String(Files.readAllBytes(file.toPath())).split("(\r\n)|(\n\r)|\r|\n");
        IntStream.range(0, lines.length).forEach(index -> {
            if (lines[index].matches("^\\s*[^#].*"))
                lines[index] = (index +1) + ":" + lines[index].trim();
        });        
        
        String content = String.join("\n", lines);
        content = content.replaceAll("[\\x00-\\x09\\x0B-\\x20]+", " ");
        content = content.replaceAll("(?m)^ *#[^\n]*?(\n|$)", "");
        content = content.trim();
        
        List<Task> tasks = new ArrayList<>();
        for (String section : content.split("(\\s*\\R\\s*){2,}")) {
            String line = section.replaceAll("(?s)^(\\d+):.*", "$1");
            section = section.replaceAll("(?m)^\\s*\\d+:\\s*", "");
            try {tasks.add(Task.parse(String.format("#%s%n%s", line, section)));
            } catch (ReviewParserException exception) {
                throw new ReviewParserException(String.format("%s in the section from line %s", exception.getMessage(), line));
            }
        }
        
        return tasks.toArray(new Task[0]);            
    }
    
    /**
     * Returns the bytes of a resource file.
     * @param  resource
     * @return bytes of a resource file
     * @throws IOException
     *     In the case when the access to the resources fails.
     */
    private static byte[] getResourceBytes(String resource)
            throws IOException {
        ClassLoader classLoader = Review.class.getClassLoader();
        try (DataInputStream inputStream = new DataInputStream(classLoader.getResourceAsStream("resources/" + resource))) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bytes = new byte[65535];
            for (int length; (length = inputStream.read(bytes)) >= 0;)
                buffer.write(bytes, 0, length);
            return buffer.toByteArray();
        }
    }
    
    /**
     * Returns the text of a resource file.
     * @param  resource
     * @return text of a resource file
     * @throws IOException
     *     In the case when the access to the resources fails.
     */
    private static String getResourceText(String resource)
            throws IOException {
        String text = new String(Review.getResourceBytes(resource));
        text = text.replaceAll("\\x20{4}", "\t");
        text = text.replaceAll("(?m)\\s+$", System.getProperty("line.separator"));
        text = text.replaceAll("\\s+$", "");
        return text;
    }
    
    /**
     * Main entry in the application.
     * @param  options start parameter
     * @throws Exception
     *     In the case of occurring errors.
     */
    public static void main(String[] options)
            throws Exception {

        // for testing:
        // options = new String[] {"-x", "-d", "./test", "./test/anti-pattern.txt"};
        // options = new String[] {"-d", "./test", "./test/anti-pattern.txt"};
        // options = new String[] {"-h"};
        
        File path = new File(".");
        String pattern = null;
        for (int loop = 0; options != null && loop < options.length; loop++) {
            String option = options[loop].trim();
            if (loop < options.length -1
                    || option.startsWith("-")) {
                if (option.equalsIgnoreCase("-x"))
                    Options.replace = true;
                else if (option.equalsIgnoreCase("-d")
                        && !options[++loop].toLowerCase().startsWith("-"))
                    path = new File(options[loop].trim());
                else if (option.equalsIgnoreCase("-h"))
                    Options.help = true;                
            } else pattern = option;
        }

        System.out.println("Review [Version 0.0.0 00000000]");
        System.out.println("Copyright (C) 0000 Seanox Software Solutions");
        System.out.println("Expression Based Static Code Analysis");
        
        if (pattern == null) {
            System.out.println();
            System.out.println(Review.getResourceText("usage.txt"));
            if (Options.help)
                System.out.println(Review.getResourceText("help.txt"));
            return;
        }

        Review.timing = System.currentTimeMillis();
        
        try {
            
            Review.tasks = new ArrayList<>(Arrays.asList(Review.readTasks(new File(pattern))));
            System.out.println();
            System.out.printf("\tfound %s tasks%n", Review.tasks.size());

            Review.queue = new ArrayList<>();
            if (path != null
                    && path.exists())
                Review.queue = Arrays.asList(Files.walk(path.toPath())
                        .map(Path::toFile)
                        .filter(file -> file.isFile() && file.exists())
                        .toArray(File[]::new));
            Review.queue = new ArrayList<>(Review.queue);
            System.out.printf("\tfound %d files%n", Review.queue.size());
            
            System.out.printf("\testablishing %s workers%n", Review.THREADS);
            Review.workers = new ArrayList<>();
            while (Review.workers.size() < Review.THREADS)
                Review.workers.add(new Worker());

            System.out.printf("\tstarting review%n");
            Review.workers.forEach(Worker::start);

            while (Review.workers.stream().anyMatch(Worker::isAlive))
                try {Thread.sleep(25);
                } catch (InterruptedException exception) {
                    break;
                }

            for (Worker worker : Review.workers)
                worker.interrupt();

            float time = (System.currentTimeMillis() -Review.timing) /1000f;
            float volume = Review.volume /1024f /1024f;
            
            String summary = Review.getResourceText("summary.txt");
            summary = String.format(summary,
                    Review.founds,
                    Review.corrections,
                    Review.errors,
                    time,
                    Review.reviews,
                    (long)(Review.reviews /time),
                    Review.files,
                    (long)(Review.files /time),
                    volume,
                    volume /time);
            System.out.println();
            System.out.println(summary);
            
        } catch (Exception exception) {
            Review.print(System.lineSeparator());
            if (exception instanceof ReviewException) {
                Review.print(exception.getMessage());
                return;
            }
            Review.print("An unexpected error occurred:");
            Review.print(exception);
        }
    }

    /** General exception in the context of Review. */
    private static class ReviewException extends Exception {
        
        private static final long serialVersionUID = 8677747635250456149L;

        private ReviewException(String message) {
            super(message);
        }
    }
    
    /** General parser exception in the context of Review. */
    private static class ReviewParserException extends ReviewException {
        
        private static final long serialVersionUID = 2873474249219182099L;

        private ReviewParserException(String message) {
            super(message);
        }
    }
    
    /** 
     * Inner class for a Worker. 
     * Worker are (re)used as thread and perform the analysis of a file.
     * Review has a fixed number of workers, which reviews are assigned.
     * A review is a set of tasks.
     */
    private static class Worker extends Thread {
        
        /** time of last interruption */
        private volatile long timing;
        
        /**
         * Interrupts the processing with maximum exploitation of the time slice
         * of the operating system.
         * @throws InterruptedException
         *      In the case where the current thread is interrupted.
         */
        private void sleepSmart()
                throws InterruptedException {

            if (this.timing == 0)
                this.timing = System.currentTimeMillis();
            if ((System.currentTimeMillis() -this.timing) < 20)
                return;
            
            Thread.sleep(25);
            this.timing = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            
            while (!this.isInterrupted()) {

                File file;
                synchronized (Review.queue) {
                    if (Review.queue.size() <= 0)
                        break;
                    file = Review.queue.remove(0);
                }

                for (Task task : Review.tasks) {

                    try {this.sleepSmart();
                    } catch (InterruptedException exception) {
                        break;
                    }
                    
                    if (!task.filter.accept(file))
                        continue; 
                    Review.files++;
                    task.perform(file);
                }
                
                System.gc();
            }
        }
    }
    
    /**
     * Internal class to use conditions.
     * Conditions are inclusions and exclusions for files and content.
     */
    private static class Condition {

        /** type */
        private final Type type;

        /** rule (pattern as plain text) */
        private final String rule;

        /** pattern (compiled rule) */
        private final Pattern pattern;
        
        /**
         * Constructor, creates a new Condition object. 
         * @param type type
         * @param rule rule
         */
        private Condition(Type type, String rule) {
            
            this.type = type;
            switch (this.type) {
                
                case FILE:
                    
                    rule = rule.trim();
                    rule = rule.replace('\\', '/');
                    rule = rule.replaceAll("\\/+", "/");
                    rule = Pattern.quote(rule);
                    rule = rule.replaceAll("\\*", "\\\\E.*\\\\Q");
                    rule = rule.replaceAll("\\?", "\\\\E.\\\\Q");
                    rule = "(" + rule + ")";
                    this.rule = rule;
                    
                    this.pattern = Pattern.compile(this.rule);
                    
                    break;

                case CONTENT:

                    this.rule = rule;
                    this.pattern = Pattern.compile(this.rule);
                    
                    break;
                    
                default:
                    throw new RuntimeException("Not supported condition type found");
            }
        }
        
        /** Type of conditions */
        private enum Type {
            FILE,
            CONTENT
        }
    }
    
    /** Condition for includes. */
    private static class Include extends Condition {
        
        private Include(Condition.Type type, String rule) {
            super(type, rule);
        }
    }
    
    /** Condition for excludes. */
    private static class Exclude extends Condition {
        
        private Exclude(Condition.Type type, String rule) {
            super(type, rule);
        }
    }

    /**
     * Inner class for a Task.
     * A Task is one part of the code analysis.
     */
    private static class Task {

        /** line number */
        private long number;        
        
        /** conditions (file + content) */
        private Condition[] conditions;

        /** file filter */
        private FileFilter filter;

        /** command */
        private String command;

        /** action */
        private String action;

        /** Constructor, creates a new Task object. */
        private Task() {
        }
        
        /**
         * Decodes hexadecimal characters in a text.
         * @param  text text to be decoded
         * @return the decoded text
         * @throws UnsupportedEncodingException
         *     In case of invalid encoding.
         */
        private static String decode(String text)
                throws UnsupportedEncodingException {
            text = text.replaceAll("\\s", "\\\\x20");
            text = text.replaceAll("\\%", "\\\\x25");
            text = text.replaceAll("\\+", "\\\\x2B");
            text = text.replaceAll("(?i)\\\\x([0-9a-f]{2})", "%$1");
            return URLDecoder.decode(text, "ISO-8859-1");
        }
        
        /**
         * Creates file conditions for the passed expression(s).
         * @param  expression expression(s)
         * @return content file for the passed expression(s)
         * @throws ReviewParserException
         *     In case if errors are found in the expression(s).
         * @throws UnsupportedEncodingException
         *     In case of invalid encoding.
         */        
        private static Condition[] parseFileConditions(String expression)
                throws ReviewParserException, UnsupportedEncodingException {
            
            expression = expression.replaceAll("(?:^|\\s+)([\\+\\-])\\s+", " $1").trim();
            if (expression.isEmpty()
                    || expression.matches("(^|\\s)(?![\\+\\-])"))
                throw new ReviewParserException("Invalid file condition found");

            List<Condition> conditions = new ArrayList<>();
            for (String rule : expression.split("\\s+")) {
                if (rule.startsWith("+"))
                    conditions.add(new Include(Condition.Type.FILE, Task.decode(rule.substring(1))));
                else if (rule.startsWith("-"))
                    conditions.add(new Exclude(Condition.Type.FILE, Task.decode(rule.substring(1))));
                else throw new ReviewParserException("Invalid file condition found");     
            }
            
            return conditions.toArray(new Condition[0]);
        }
        
        /**
         * Creates content conditions for the passed expression(s).
         * @param  expressions expression(s)
         * @return content conditions for the passed expression(s)
         * @throws ReviewParserException
         *     In case if errors are found in the expression(s).
         * @throws UnsupportedEncodingException
         *     In case of invalid encoding.
         */
        private static Condition[] parseContentConditions(String... expressions)
                throws ReviewParserException, UnsupportedEncodingException {
            
            if (expressions == null
                    || expressions.length <= 0)
                throw new ReviewParserException("Invalid pattern found");
            
            String expression = String.join(" ", expressions).trim();
            expression = expression.replaceAll("([+-])\\s+", "$1");
            List<Condition> conditions = new ArrayList<>();
            for (String rule : expression.split("\\s+")) {
                if (conditions.size() == 0)
                    conditions.add(new Include(Condition.Type.CONTENT, Task.decode(rule)));
                else if (rule.startsWith("+"))
                    conditions.add(new Include(Condition.Type.CONTENT, Task.decode(rule.substring(1))));
                else if (rule.startsWith("-"))
                    conditions.add(new Exclude(Condition.Type.CONTENT, Task.decode(rule.substring(1))));
                else throw new ReviewParserException("Invalid condition found");
                // the first condition must be a include
                if (conditions.size() == 1
                        && conditions.get(0) instanceof Exclude)
                    throw new ReviewParserException("Invalid pattern found");
            }
            
            return conditions.toArray(new Condition[0]);
        }
        
        /**
         * Creates a file filter based on the passed conditions.
         * @param  conditions condition(s)
         * @return a file filter based on the passed conditions
         */
        private static FileFilter createFileFilter(Condition[] conditions) {
            
            String expression;
            
            expression = String.join("|", Arrays.stream(conditions).filter(
                    condition -> Condition.Type.FILE.equals(condition.type)
                            && condition instanceof Include).map(condition -> condition.rule).toArray(String[]::new)).trim();
            final Pattern include = !expression.isEmpty() ? Pattern.compile("(?i)^" + expression + "$") : null;
            
            expression = String.join("|", Arrays.stream(conditions).filter(
                    condition -> Condition.Type.FILE.equals(condition.type)
                            && condition instanceof Exclude).map(condition -> condition.rule).toArray(String[]::new)).trim();
            final Pattern exclude = !expression.isEmpty() ? Pattern.compile("(?i)^" + expression + "$") : null;

            return file -> {
                String name;
                try {name = file.getCanonicalPath().replaceAll("\\\\", "/");
                } catch (IOException exception) {
                    return false;
                }
                return (include == null
                            || include.matcher(name).matches())
                        && (exclude == null
                                || !exclude.matcher(name).matches());
            };
        }
        
        /**
         * Creates a task based on the information of the passed section.
         * @param  section
         * @return a task based on the information of the passed section
         * @throws ReviewParserException
         *     In case if errors are found in the section.
         */
        private static Task parse(String section)
                throws ReviewParserException {
            
            String[] lines = section.split("(\\s*\\R\\s*)+");
            if (lines.length < 3)
                throw new ReviewParserException("Invalid task structure found");
            
            List<Condition> conditions;
            try {conditions = new ArrayList<>(Arrays.asList(Task.parseFileConditions(lines[1])));
            } catch (UnsupportedEncodingException exception) {
                throw new ReviewParserException("Invalid encoded filter found");
            }
            try {conditions.addAll(Arrays.asList(Task.parseContentConditions(Arrays.copyOfRange(lines, 2, lines.length -1))));
            } catch (PatternSyntaxException exception) {
                throw new ReviewParserException("Invalid pattern expression found");
            } catch (UnsupportedEncodingException exception) {
                throw new ReviewParserException("Invalid encoded pattern found");
            }
            
            Task task = new Task();
            task.number = Long.valueOf(lines[0].replaceAll("^#(\\d+)$", "$1"));
            task.conditions = conditions.toArray(new Condition[0]);
            task.filter = Task.createFileFilter(task.conditions);
            task.conditions = Arrays.stream(task.conditions).filter(condition -> Condition.Type.CONTENT.equals(condition.type)).toArray(Condition[]::new);
            if (task.conditions.length <= 0)
                throw new ReviewParserException("Invalid task structure found");
            String pattern = "(?i)^(DETECT|PATCH|REMOVE)(?:\\s+(.*))*$";
            String line = lines[lines.length -1].trim();
            if (!line.matches(pattern))
                throw new ReviewParserException("Invalid task action found");
            task.command = line.replaceAll(pattern, "$1").toUpperCase();
            try {task.action = Task.decode(line.replaceAll("^\\S+\\s+", ""));
            } catch (UnsupportedEncodingException exception) {
                throw new ReviewParserException("Invalid encoded action found");
            }
            return task;            
        }
        
        /**
         * Locates the line of the first character of the match.
         * @param  matcher matcher
         * @param  content content
         * @param  offset  offset
         * @return the line of the first character of the match
         */        
        private static int locateMatchLine(Matcher matcher, String content, int offset) {
            char digit = content.charAt(matcher.start() +offset);
            if (digit == '\r'
                    || digit == '\n')
                offset++;
            content = content.substring(0, matcher.start() +offset);
            matcher = Pattern.compile("(?s)\\R").matcher(content);
            int count = 1;
            while (matcher.find())
                count++;
            return count;
        }

        /**
         * Locates the line position of the first character of the match.
         * @param  matcher matcher
         * @param  content content
         * @param  offset  offset
         * @return the line position of the first character of the match
         */
        private static int locateMatchCharacter(Matcher matcher, String content, int offset) {
            char digit = content.charAt(matcher.start() +offset);
            if (digit == '\r'
                    || digit == '\n')
                return 1;
            content = content.substring(0, matcher.start() +offset);
            int loop = content.length();
            while (loop > 0)
                if (content.charAt(--loop) == '\r'
                        || content.charAt(loop) == '\n')
                    break;
            return (content.length() -loop) +1;
        }

        /**
         * Performs the review task for a file.
         * All found files will be processed.
         * The filter filters decide whether a review must be performed.
         * @param file file
         */
        private void perform(File file) {
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintStream output = new PrintStream(stream);
            
            try {
                if (!this.filter.accept(file))
                    return; 

                String compare = "";
                String content = "";
                
                while (true) {

                    Review.reviews++;
                    
                    for (int offset = 0; true; offset++) {
                        
                        content = new String(Files.readAllBytes(file.toPath()));
                        Review.volume += content.length();

                        if (offset >= content.length())
                            break;
                        
                        Matcher matcher = this.conditions[0].pattern.matcher(content.substring(offset));
                        if (!matcher.find())
                            break;
                        
                        String match = content.substring(matcher.start() +offset, matcher.end() +offset);
                        if (this.conditions.length > 1) {
                            boolean relevant = true;
                            for (Condition condition : Arrays.copyOfRange(this.conditions, 1, this.conditions.length)) {
                                Matcher submatcher = condition.pattern.matcher(match);
                                boolean exists = submatcher.find();
                                if ((condition instanceof Exclude && exists)
                                        || (condition instanceof Include && !exists)) {
                                    relevant = false;
                                    break;
                                }
                            }
                            
                            if (!relevant) {
                                offset += matcher.start();
                                continue;
                            }
                        }

                        Review.founds++;

                        if (stream.size() <= 0) {
                            output.printf("%n");
                            output.printf("%s%n", file);
                            output.printf("Match from line %s%n", this.number);
                            String rule = this.conditions[0].rule;
                            if (rule.trim().isEmpty())
                                rule = "-- no output, white spaces only";
                            if (rule.length() > 72)
                                rule = rule.substring(0, 69) + "...";
                            output.printf("Pattern %s%n", rule);
                        }
                        
                        String location = String.format("line %s from character %s",
                                Task.locateMatchLine(matcher, content, offset), Task.locateMatchCharacter(matcher, content, offset));

                        if (this.command.matches("^DETECT")
                                || (this.command.matches("^PATCH|REMOVE$") 
                                        && !Options.replace)) {
                            String message = content.substring(matcher.start() +offset, matcher.end() +offset)
                                    .replaceAll("\\s", " ").trim();
                            if (!message.isEmpty())
                                message = "DETECTED " + location + ": " + message;
                            else message = "DETECTED " + location;
                            if (message.length() > 74)
                                message = message.substring(0, 71) + "...";
                            output.println(message);
                            offset += matcher.end();

                            if (this.command.matches("^DETECT")
                                    && !this.action.trim().isEmpty())
                                output.println(this.action.trim());
                            
                        } else if (this.command.matches("^REMOVE$")
                                || (this.command.matches("^PATCH$")
                                        && this.action.isEmpty())) {
                            content = content.substring(0, matcher.start() +offset) + content.substring(matcher.end() +offset);
                            Review.writeFile(file, content.getBytes());
                            Review.corrections++;
                            output.println("PATCHED " + location);

                        } else if (this.command.matches("^PATCH$")) {
                            match = match.replaceAll(this.conditions[0].rule, this.action);
                            content = content.substring(0, matcher.start() +offset) + match + content.substring(matcher.end() +offset);
                            Review.writeFile(file, content.getBytes());
                            Review.corrections++;
                            output.println("PATCHED " + location);
                            offset += matcher.start() +match.length() -1;
                        }
                    }
                    
                    if (compare.equals(content)
                            || !this.command.matches("^PATCH$")
                            || !Options.replace) 
                        break;
                    compare = content;
                }
                
                if (stream.size() > 0)
                    Review.print(stream.toString());
                
           } catch (Throwable throwable) {
                Review.print("%nERROR: Occurred in section #%s%n", String.valueOf(this.number));
                Review.print(throwable);
                Review.errors++;
            }
        }
    }
}