/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Review, text based code analyzer
 * Copyright (C) 2021 Seanox Software Solutions
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Review, a text based code analyzer.<br>
 * <br>
 * Review 1.5.0 202112115<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.5.0 20211211
 */
public class Review {
    
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
    
    /** pattern for line break (cross plattform) */
    private static final String LINE_BREAK = "(?:(?:\\r\\n)|(?:\\n\\r)|[\\r\\n])";
    
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
    private static void print(Object message) {
        
        if (message instanceof Throwable) {
            StringWriter writer = new StringWriter();
            ((Throwable)message).printStackTrace(new PrintWriter(writer));
            message = writer.toString();
        } else message = String.valueOf(message);
        synchronized (Review.class) {
            System.out.println(((String)message).replaceAll(LINE_BREAK + "$", ""));
        }
    }

    /**
     * Writes a formated message to the system output stream.
     * @param message message
     * @param values  value(s) 
     */
    private static void print(String message, Object... values) {
        Review.print(String.format(message, values));
    }
    
    /**
     * Reads the contents of a file.
     * @param  file file
     * @return the contents of a file as byte array
     * @throws IOException
     *     In the case of the failed file access.
     */
    private static byte[] readFile(File file)
            throws IOException {
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        Review.files++;
        Review.volume += bytes.length;
        return bytes;
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
     * Reads all taks from a file.
     * @param  file anti-pattern file
     * @return array of created and prepared task
     * @throws IOException
     *     In the case of the failed file access.
     * @throws ReviewParserException
     *     In case of invalid syntax or structure.
     */    
    private static Task[] readTasks(File file)
            throws IOException, ReviewParserException {

        String content = new String();
        int index = 0;
        for (String line : new String(Review.readFile(file)).split(LINE_BREAK)) {
            index++;
            content += (line.matches("^[^\\s#].*") ? index + ":" + line : line) + "\n";
        }
        content = content.replaceAll("[\\x00-\\x09\\x0B-\\x20]+", " ");
        content = content.replaceAll("(?m)^ *#[^\n]*?(\n|$)", "");
        content = content.trim();
        
        List<Task> tasks = new ArrayList<>();
        for (String section : content.split("((\n\\s*){2,})|(\n+(?!\\s))")) {
            String line = section.replaceAll("(?s)^(\\d+):.*", "$1");
            section = section.replaceAll("^(\\d+):\\s*", "#$1\n");
            try {tasks.add(Task.parse(section));
            } catch (ReviewParserException exception) {
                throw new ReviewParserException(String.format("%s in the section from line %s", exception.getMessage(), line));
            }
        }
        
        return tasks.toArray(new Task[0]);            
    }
    
    /**
     * Requests and locks a worker for a file.
     * @param  file file
     * @return the established worker, otherwise {@code null}
     */
    private static Worker lockWorker(File file) {
        
        for (Worker worker : Review.workers)
            if (worker.lock(file))
                return worker;
        return null;
    }
    
    /**
     * Checks and searchs active workers.
     * @return {@code true} when active worker found
     */
    private static boolean activeWorker() {
        
        for (Worker worker : Review.workers)
            if (worker.file != null)
                return true;
        return false;
    }
    
    /**
     * Scans the file system for files to review and available workers.
     * @param  path path
     * @throws Exception
     *     In the case of occurring errors.
     */
    private static void seek(File path)
            throws Exception {
        
        File[] files = path.listFiles(); 
        if (files == null)
            return;
        
        for (File file : files) {
            if (Thread.interrupted())
                break;
            if (!file.isDirectory()) {
                if (!file.isFile())
                    continue;
                Review.files++;
                while (Review.lockWorker(file) == null)
                    Thread.sleep(25);
            } else Review.seek(file);
        }
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
            for (int length = 0; (length = inputStream.read(bytes)) >= 0;)
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

        //for testing: 
        //options = new String[] {"-x", "-d", "./test", "./test/anti-pattern.txt"};
        //options = new String[] {"-d", "./test", "./test/anti-pattern.txt"};
        //options = new String[] {"-h"};
        
        File path = null;
        String pattern = null;
        for (int loop = 0; options != null && loop < options.length; loop++) {
            String option = options[loop].trim();
            if (loop < options.length -1
                    || option.startsWith("-")) {
                if (option.toLowerCase().equals("-x"))
                    Options.replace = true;
                else if (option.toLowerCase().equals("-d")
                        && loop -1 < options.length
                        && !options[++loop].toLowerCase().startsWith("-"))
                    path = new File(options[loop].trim());
                else if (option.toLowerCase().equals("-h"))
                    Options.help = true;                
            } else pattern = option;
        }
        
        if (pattern == null) {
            System.out.println(Review.getResourceText("usage.txt"));
            if (Options.help)
                System.out.println(Review.getResourceText("help.txt"));
            System.out.println("");
            return;
        }
        
        System.out.println("Review [Version #[ant:release-version] #[ant:release-date]]");
        System.out.println("Copyright (C) #[ant:release-year] Seanox Software Solutions");
        System.out.println("Expression Based Static Code Analysis");
        Review.timing = System.currentTimeMillis();
        
        try {
            
            Review.tasks = new ArrayList<>(Arrays.asList(Review.readTasks(new File(pattern))));
            
            System.out.println();
            System.out.printf("\tfound %s tasks%n", String.valueOf(Review.tasks.size()));
            Review.workers = new ArrayList<>();
            while (Review.workers.size() < Review.THREADS) {
                Worker worker = new Worker();
                Review.workers.add(worker);
                worker.start();
            }
            System.out.printf("\testablishing %s workers%n", String.valueOf(Review.workers.size()));
            System.out.printf("\tstarting review%n");
            
            try {Review.seek(path == null ? new File(".").getCanonicalFile() : path.getCanonicalFile());
            } catch (InterruptedException exception) {
                System.out.println();
                System.out.println("\tabort");
            } catch (Throwable throwable) {
                System.out.println();
                System.out.println("\terror occurred");
                throwable.printStackTrace(System.out);
            }
            
            while (Review.activeWorker()) {
                try {Thread.sleep(25);
                } catch (InterruptedException exception) {
                    break;
                }
            }
            
            for (Worker worker : Review.workers)
                worker.interrupt();
            
            long time = Math.max((System.currentTimeMillis() -Review.timing) /1000, 1);
            long volume = Review.volume /1024 /1024;
            String summary = Review.getResourceText("summary.txt");
            summary = String.format(summary, Long.valueOf(Review.founds),
                    Long.valueOf(Review.corrections),
                    Long.valueOf(Review.errors),
                    Long.valueOf(time),
                    Long.valueOf(Review.reviews),
                    Long.valueOf(Review.reviews /time),
                    Long.valueOf(Review.files),
                    Long.valueOf(Review.files /time),
                    Long.valueOf(volume),
                    Long.valueOf(volume /time));
            System.out.println();
            System.out.println(summary);
            
        } catch (Exception exception) {
            Review.print("%nERROR: %s", exception.getMessage());
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
        
        /** file that must be reviewed */
        private volatile File file;
        
        /** time of last interruption */
        private volatile long timing;
        
        /**
         * locks the worker for a file.
         * @param  file file
         * @return {@code true}, if the worker could be established
         */
        private boolean lock(File file) {

            if (this.file != null)
                return false;
            this.file = file;
            return true;
        }
        
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
                
                if (this.file == null) {
                    try {Thread.sleep(25);
                    } catch (InterruptedException exception) {
                        break;
                    }
                    continue;
                }
                
                for (Task task : Review.tasks) {
                    try {this.sleepSmart();
                    } catch (InterruptedException exception) {
                        break;
                    }
                    task.perform(this.file);
                }
                
                this.file = null;
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
        private Type type;

        /** rule (pattern as plain text) */
        private String rule;

        /** pattern (compiled rule) */
        private Pattern pattern;
        
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
        private static enum Type {
            
            FILE,
            
            CONTENT;
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
            return;
        }
        
        /**
         * Decodes hexadecimal characters in a text.
         * @param  text text to be decoded
         * @return the decods text
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
            List<Condition> conditions = new ArrayList<>();
            for (String rule : expression.split("\\s+")) {
                if (conditions.size() == 0)
                    conditions.add(new Include(Condition.Type.CONTENT, Task.decode(rule)));
                else if (rule.startsWith("+"))
                    conditions.add(new Include(Condition.Type.CONTENT, Task.decode(rule.substring(1))));
                else if (rule.startsWith("-"))
                    conditions.add(new Exclude(Condition.Type.CONTENT, Task.decode(rule.substring(1))));
                else throw new ReviewParserException("Invalid file condition found");
                //the first condition must be an include.
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
                    c -> Condition.Type.FILE.equals(c.type)
                            && c instanceof Include).map(c -> c.rule).toArray(String[]::new)).trim();
            final Pattern include = !expression.isEmpty() ? Pattern.compile("(?i)^" + expression + "$") : null;
            
            expression = String.join("|", Arrays.stream(conditions).filter(
                    c -> Condition.Type.FILE.equals(c.type)
                            && c instanceof Exclude).map(c -> c.rule).toArray(String[]::new)).trim();
            final Pattern exclude = !expression.isEmpty() ? Pattern.compile("(?i)^" + expression + "$") : null;

            return new FileFilter() {
                
                public boolean accept(File file) {
                    
                    String name = null;
                    try {name = file.getCanonicalPath().replaceAll("\\\\", "/");
                    } catch (IOException exception) {
                        return false;
                    }
                    return (include == null
                                || include.matcher(name).matches())
                            && (exclude == null
                                    || !exclude.matcher(name).matches());
                }
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
            
            section = section.replaceAll("(?s)\\s*[\r\n]\\s+\\.{3}", "");
            String[] lines = section.split("\\s*" + LINE_BREAK + "\\s*");
            if (lines.length < 4)
                throw new ReviewParserException("Invalid task structure found");
            
            List<Condition> conditions = new ArrayList<>();
            try {conditions.addAll(Arrays.asList(Task.parseFileConditions(lines[1])));
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
            task.number = Integer.valueOf(lines[0].replaceAll("^#(\\d+)$", "$1")).intValue();
            task.conditions = conditions.toArray(new Condition[0]);
            task.filter = Task.createFileFilter(task.conditions);
            task.conditions = Arrays.stream(task.conditions).filter(c -> Condition.Type.CONTENT.equals(c.type)).toArray(Condition[]::new);
            if (task.conditions.length <= 0)
                throw new ReviewParserException("Invalid task structure found");
            try {task.action = Task.decode(lines[lines.length -1]);
            } catch (UnsupportedEncodingException exception) {
                throw new ReviewParserException("Invalid encoded action found");
            }
            String pattern = "(?i)^(TEST|PRINT|PATCH|REMOVE)(?:\\s+(.*)\\s*)*$";
            if (!task.action.matches(pattern))
                throw new ReviewParserException("Invalid task action found");
            task.command = task.action.replaceAll(pattern, "$1").toUpperCase();
            task.action = task.action.replaceAll(pattern, "$2");
            return task;            
        }
        
        /**
         * Creates a preview/snapshot of the place of the match.
         * @param  matcher matcher
         * @param  content content
         * @param  offset  offset
         * @return a preview/snapshot of the place of the match
         */
        private static String previewMatch(Matcher matcher, String content, int offset) {
            
            //localization of the matching line
            String match = content.substring(matcher.start() +offset);
            match = match.replaceAll("(?s)([^\r\n]*).*$", "$1");
            
            //calculate the preceding sequence
            //  - if it is not separated from the match by a line break
            //  - use max. 20 characters
            //  - trim left white spaces
            //  - add ... if preview longer than 20 characters
            String preview = content.substring(0, matcher.start() +offset);
            int loop = preview.length();
            while (loop > 0) {
                if (preview.charAt(loop -1) == '\r'
                        || preview.charAt(loop -1) == '\n')
                    break;
                loop--;
            }
            preview = preview.substring(loop);
            preview = preview.replaceAll("^\\s+", "");
            char digit = content.charAt(matcher.start() +offset);
            if (digit == '\r'
                    || digit == '\n'
                    || match.matches("(?s)^(\\s*[\r\n])+"))
                preview = "";
            if (!preview.isEmpty()) {
                digit = preview.charAt(preview.length() -1);
                if (digit == '\r'
                        || digit == '\n')
                    preview = "";
            }
            
            if (preview.isEmpty())
                match = match.replaceAll("(?s)^\\s+", "");
            
            if (preview.length() > 40
                    && match.length() > 40) {
                preview = "..." + preview.substring(Math.max(0, preview.length() -40 -3), preview.length());
                match = match.substring(0, Math.min(match.length(), 40 -3)) + "...";
            } else if (preview.length() + match.length() > 80) {
                if (preview.length() > 40) {
                    preview = "..." + preview.substring(preview.length() -(80 -3 -match.length()), preview.length());
                } else if (match.length() > 40) {
                    match = match.substring(0, 80 -3 -preview.length()) + "...";
                }
            }

            if (match.matches("^\\s*$"))
                return null;

            String indenting = "";
            while (indenting.length() < preview.length())
                indenting += " ";
            return (preview + match).replaceAll("\\s", " ") + System.lineSeparator() + indenting + "|--->";
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
            matcher = Pattern.compile("(?s)" + LINE_BREAK).matcher(content);
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
            return content.length() -loop;
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
                        
                        content = new String(Review.readFile(file));
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
                        
                        output.printf("%n");
                        output.printf("%s%n", file);
                        output.printf("section #%s matches line %s, character %s%n", String.valueOf(this.number),
                                String.valueOf(Task.locateMatchLine(matcher, content, offset)), String.valueOf(Task.locateMatchCharacter(matcher, content, offset)));
                        String rule = this.conditions[0].rule;
                        if (rule.length() > 72)
                            rule = rule.substring(0, 69) + "...";
                        output.printf("pattern %s%n", rule);
                        String preview = Task.previewMatch(matcher, content, offset);
                        output.println(preview == null ? "Preview not available" : preview);
                        
                        if (this.command.matches("^REMOVE$")
                                || (this.command.matches("^PATCH$")
                                        && this.action.isBlank())) {
                            content = content.substring(0, matcher.start() +offset) + content.substring(matcher.end() +offset);
                            Review.writeFile(file, content.getBytes());
                            Review.corrections++;
                            output.println("CORRECTED");
                        } else if (this.command.matches("^TEST$")
                                || (this.command.matches("^PATCH$")
                                        && !Options.replace)) {
                            match = match.replaceAll(this.conditions[0].rule, this.action);
                            if (preview == null) {
                                match = match.replaceAll("\\s", " ");
                                if (match.length() > 74)
                                    match = match.substring(0, 71) + "...";
                                output.println("TEST " + match);
                            } else {
                                String[] lines = preview.split(LINE_BREAK);
                                String test = lines[0].substring(0, (lines[1].split("\\|")[0]).length()) + match + content.substring(matcher.end() +offset);
                                test = test.replaceAll("(?s)^\\s+", "");
                                test = test.replaceAll("(?s)([^\r\n]*).*$", "$1");
                                test = test.replaceAll("\\s", " ").trim();
                                if (test.length() > 80)
                                    test = test.substring(0, 77) + "...";
                                output.println(test);
                            }                       
                            offset += matcher.end();
                        } else if (this.command.matches("^PRINT$")) {
                            output.println(!this.action.isBlank() ? this.action : "Output not available");
                            offset += matcher.end();
                        } else if (this.command.matches("^PATCH$")) {
                            match = match.replaceAll(this.conditions[0].rule, this.action);
                            content = content.substring(0, matcher.start() +offset) + match + content.substring(matcher.end() +offset);
                            Review.writeFile(file, content.getBytes());
                            Review.corrections++;
                            output.println("CORRECTED");
                            offset += matcher.start() +match.length() -1;
                        }
                        
                        Review.print(stream.toString());
                        stream.reset();
                    }
                    
                    if (compare.equals(content)
                            || !this.command.matches("^PATCH$")
                            || !Options.replace) 
                        break;
                    compare = content;
                }
           } catch (Throwable throwable) {
                Review.print("%nERROR: Occured in section #%s%n", String.valueOf(this.number));
                Review.print(throwable);
                Review.errors++;
            }
        }
    }
}