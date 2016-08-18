/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Review, text based code analyzer
 *  Copyright (C) 2016 Seanox Software Solutions
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of version 2 of the GNU General Public License as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Review, a text based code analyzer.<br>
 *  <br>
 *  Review 1.3.4 20160818<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.3.4 20160818
 */
public class Review {
    
    /** list/queue of tasks */
    private volatile static List<Task> tasks;

    /** list of workers*/
    private volatile static List<Worker> workers;
    
    /** number of sites */
    private static long founds;
    
    /** number of corrections */
    private static long corrections;
    
    /** start time */
    private static long timing;

    /** amount of processed data (bytes) */
    private static long volume;
    
    /** amount of processed files */
    private static long files;
    
    /** number of occurring errors */
    private static long errors;
    
    /** number of performed reviews */
    private static long reviews;
    
    /** (De)Activation of the replacement */
    private static boolean performReplacement;
    
    /** (De)Activation of the info output */
    private static boolean verboseInfos;

    /** (De)Activation of the help output */
    private static boolean showHelp;

    /** number of workers/threads */
    private static final int THREADS = 25;
    
    /** pattern for line break (cross plattform) */
    private static final String LINE_BREAK = "(?:(?:\\r\\n)|(?:\\n\\r)|[\\r\\n])";
    
    /** pattern for line white spaces */
    private static final String LINE_WHITE_SPACES = "[\\x00-\\x09\\x0B-\\x0C\\x0E-\\x1F]";
    
    /**
     *  Reads the contents of a file.
     *  @param  file file
     *  @return the contents of a file as byte array
     *  @throws IOException
     *      In the case of the failed file access.
     */
    private static byte[] readFile(File file) throws IOException {
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        Review.files++;
        Review.volume += bytes.length;
        return bytes;
    }
    
    /**
     *  Writes the contents in a file.
     *  @param  file  file
     *  @param  bytes content
     *  @throws IOException
     *      In the case of the failed file access.
     */
    private static void writeFile(File file, byte[] bytes) throws IOException {
        
        Files.write(file.toPath(), bytes);
        Review.volume += bytes.length;
    }
    
    /**
     *  Decodes the text of a task and preparation for use.
     *  @param  text task as text
     *  @return decoded and prepared task
     */
    private static String decodeTaskText(String text) {
        
        if (text == null) return null;
        text = text.replaceAll("\\t", "    ");
        text = text.replaceAll(LINE_WHITE_SPACES, " ").trim();
        text = text.replaceAll("%", "\\x25");
        return text;
    }
    
    /**
     *  Reads all taks from a file.
     *  @param  file anti-pattern file
     *  @return array of created and prepared task
     */    
    private static Task[] readTasks(File file) throws IOException {

        List<Task> tasks = new ArrayList<>();
        String content = new String(Review.readFile(file));
        String pattern = new String();
        int index = 0;
        for (String line : content.split(LINE_BREAK)) {
            index++;
            pattern += (line.matches("^[^\\s].*") ? index + ":" + line : line) + "\n";
        }
        pattern = pattern.replaceAll("\\.{3,}(\\h+)*\n(\\h+)*\\.{3,}", " ");
        pattern = pattern.replaceAll("#.*[\\r\\n]+", "\n");
        pattern = pattern.replaceAll(LINE_WHITE_SPACES, " ");
        pattern = pattern.replaceAll("(\\n)([^\\s])", "$1\00$2");
        String[] rules = pattern.split("\\n\\x00");
        for (String rule : rules) {
            String[] lines = rule.split("\\n");
            if (lines.length < 2) continue;
            Task task = new Task();
            task.lineNumber = Integer.valueOf(lines[0].replaceAll("^(\\d+):(.*)$", "$1")).intValue();
            task.fileFilter = Review.decodeTaskText(lines[0].replaceAll("^(\\d+):(.*)$", "$2"));
            if (lines.length > 1) {
                String expression = lines[1];
                String[] split = expression.split("\\s+!", 2);
                task.contentFilter = Review.decodeTaskText(split[0]);
                if (split.length > 1) task.contentExclude = Review.decodeTaskText(split[1]);
                if (task.contentExclude != null && task.contentExclude.length() == 0)
                    task.contentExclude = null;
            }
            if (lines.length > 2) {
                task.todo = Review.decodeTaskText(lines[2]);
                task.todo = task.todo.replaceAll("\\\\(?i:x)(?i:([0-9a-f]{2}))", "%$1");
                task.todo = task.todo.replaceAll("\\+", "%2B");
                task.todo = URLDecoder.decode(task.todo, "ISO-8859-1");
            }
            if (task.fileFilter == null
                    || task.fileFilter.trim().length() == 0)
                continue;
            if (task.contentFilter == null
                    || task.contentFilter.trim().length() == 0)
                continue;
            
            task.initialize();
            tasks.add(task);
        }
        return tasks.toArray(new Task[0]);
    }
    
    /**
     *  Requests and locks a worker for a file.
     *  @param  file file
     *  @return the established worker, otherwise {@code null}
     */
    private static Worker lockWorker(File file) {
        
        for (Worker worker : Review.workers)
            if (worker.lock(file)) return worker;
        return null;
    }
    
    /**
     *  Checks and searchs active workers.
     *  @return {@code true} when active worker found
     */
    private static boolean activeWorker() {
        
        for (Worker worker : Review.workers)
            if (worker.file != null) return true;
        return false;
    }
    
    /**
     *  Scans the file system for files for the review and established workers
     *  to perform.
     *  @param  path path
     *  @throws Exception
     *      In the case of occurring errors.
     */
    private static void seek(File path) throws Exception {
        
        File[] files = path.listFiles(); 
        if (files == null)
            return;
        
        for (File file : files) {
            if (Thread.interrupted()) break;
            if (!file.isDirectory()) {
                if (!file.isFile()) continue;
                Review.files++;
                while (Review.lockWorker(file) == null)
                    Thread.sleep(25);
            } else Review.seek(file);
        }
    }
    
    /**
     *  Returns the text of a resource file.
     *  @param  resource
     *  @return text of a resource file
     *  @throws IOException
     *      In the case when the access to the resources fails.
     */
    private static String getResourceText(String resource) throws IOException {
        
        ClassLoader classLoader = Review.class.getClassLoader();
        File file = new File(classLoader.getResource("resources/" + resource).getFile());
        String text = new String(Review.readFile(file));
        
        text = text.replaceAll("\\x20{4}", "\t");
        text = text.replaceAll("(?m)\\s+$", System.getProperty("line.separator"));
        text = text.replaceAll("\\s+$", "");
        
        return text;
    }

    /**
     *  Main entry in the application.
     *  @param  options start parameter
     *  @throws Exception
     *      In the case of occurring errors.
     */
    public static void main(String[] options) throws Exception {
        
        File path = null;
        String pattern = null;
        for (int loop = 0; options != null && loop < options.length; loop++) {
            String option = options[loop].trim();
            if (loop < options.length -1 || option.startsWith("-")) {
                if (option.toLowerCase().equals("-x"))
                    Review.performReplacement = true;
                else if (option.toLowerCase().equals("-d")
                        && loop -1 < options.length
                        && !options[++loop].toLowerCase().startsWith("-"))
                    path = new File(options[loop].trim());
                else if (option.toLowerCase().equals("-v"))
                    Review.verboseInfos = true;
                else if (option.toLowerCase().equals("-h"))
                    Review.showHelp = true;                
            } else pattern = option;
        }
        
        if (pattern == null) {
            System.out.println(Review.getResourceText("usage.txt"));
            if (Review.showHelp)
                System.out.println(Review.getResourceText("help.txt"));
            System.out.println("");
            return;
        }
        
        System.out.println("Review [Version #[ant:release-version] #[ant:release-date]]");
        System.out.println("Copyright (C) #[ant:release-year] Seanox Software Solutions");
        System.out.println("Programming Languages Independent Code Review");
        System.out.println();
        Review.timing = System.currentTimeMillis();
        Review.tasks = new ArrayList<>(Arrays.asList(Review.readTasks(new File(pattern))));
        System.out.println("\tfound " + Review.tasks.size() + " tasks");
        Review.workers = new ArrayList<>();
        while (Review.workers.size() < Review.THREADS) {
            Worker worker = new Worker();
            Review.workers.add(worker);
            worker.start();
        }
        System.out.println("\testablished " + Review.workers.size() + " workers");
        System.out.println("\tstarting review");
        
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
        System.out.println(summary);
    }
    
    /** 
     *  Inner class for a Worker. 
     *  Worker are (re)used as thread and perform the analysis of a file.
     *  Review has a fixed number of workers, which reviews are assigned.
     *  A review is a set of tasks.
     */
    private static class Worker extends Thread {
        
        /** file that must be reviewed */
        private volatile File file;
        
        /** time of last interruption */
        private volatile long timing;
        
        /**
         *  locks the worker for a file.
         *  @param  file file
         *  @return {@code true}, if the worker could be established
         */
        private boolean lock(File file) {

            if (this.file != null)
                return false;
            this.file = file;
            return true;
        }
        
        /**
         *  Interrupts the processing with maximum exploitation of the time
         *  slice of the operating system.
         *  @throws InterruptedException
         *       In the case where the current thread is interrupted.
         */
        private void sleepSmart() throws InterruptedException {

            if (this.timing == 0)
                this.timing = System.currentTimeMillis();
            if ((System.currentTimeMillis() -this.timing) < 20) return;
            
            Thread.sleep(25);
            this.timing = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            
            while (true) {
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
     *  Inner class for a Task.
     *  A Task is one part of the code analysis.
     */
    private static class Task {

        /** file filter */
        private volatile String fileFilter;
        
        /** file filter exclude */
        private volatile String fileExclude;
        
        /** file filter exclude pattern */
        private volatile Pattern fileExcludePattern;
        
        /** file filter include */
        private volatile String fileInclude;
        
        /** file filter include pattern */
        private volatile Pattern fileIncludePattern;
        
        /** file line exclude */
        private volatile String fileLineExclude;
        
        /** file line exclude pattern */
        private volatile Pattern fileLineExcludePattern;

        /** content filter */
        private volatile String contentFilter;

        /** content filter pattern */
        private volatile Pattern contentFilterPattern;

        /** content filter pattern flat */
        private volatile String contentFilterPatternFlat;

        /** content exclude */
        private volatile String contentExclude;

        /** content exclude pattern */
        private volatile Pattern contentExcludePattern;

        /** todo */
        private volatile String todo;

        /** line number */
        private long lineNumber;
        
        /** Initializes a review task. */
        private void initialize() {

            this.fileExclude = "";
            this.fileLineExclude = "";
            this.fileInclude = "";
            
            String filter = this.fileFilter.replaceAll("[\\x00-\\x06]", " ");
            filter = filter.replaceAll("\\s+([\\+\\-])\\s+", " \00$1 ");
            for (String pattern : filter.split("\\00")) {
                if (pattern.startsWith("-")) {
                    if (pattern.matches("^.*\\[\\d+(:\\d+)*\\]\\s*$")) {
                        if (this.fileLineExclude.length() > 0) this.fileLineExclude += ", ";
                        this.fileLineExclude += pattern.replaceAll("^[\\+\\-]", "").trim();
                    } else {
                        if (this.fileExclude.length() > 0) this.fileExclude += ", ";
                        this.fileExclude += pattern.replaceAll("^[\\+\\-]", "").trim();
                    }
                } else {
                    if (this.fileInclude.length() > 0) this.fileInclude += ", ";
                    this.fileInclude += pattern.replaceAll("^[\\+\\-]", "").trim();
                }
            }
  
            if (this.fileExclude.length() > 0) {
                this.fileExclude = this.fileExclude.replace('\\', '/');
                this.fileExclude = this.fileExclude.replaceAll("\\/+", "/");
                this.fileExclude = Pattern.quote(this.fileExclude);
                this.fileExclude = this.fileExclude.replaceAll("\\*", "\\\\E.*\\\\Q");
                this.fileExclude = this.fileExclude.replaceAll("\\?", "\\\\E.\\\\Q");
                this.fileExclude = this.fileExclude.replaceAll(",\\s*", "\\\\E\\$)|(?i:\\\\Q");
                this.fileExclude = "(?m)(?i:" + this.fileExclude + ")";
                this.fileExcludePattern = Pattern.compile(this.fileExclude);
            } else this.fileExcludePattern = null;
            
            if (this.fileLineExclude.length() > 0) {
                this.fileLineExclude = this.fileLineExclude.replace('\\', '/');
                this.fileLineExclude = this.fileLineExclude.replaceAll("\\/+", "/");
                this.fileLineExclude = Pattern.quote(this.fileLineExclude);
                this.fileLineExclude = this.fileLineExclude.replaceAll("\\*", "\\\\E.*\\\\Q");
                this.fileLineExclude = this.fileLineExclude.replaceAll("\\?", "\\\\E.\\\\Q");
                this.fileLineExclude = this.fileLineExclude.replaceAll(",\\s*", "\\\\E\\$)|(?i:\\\\Q");
                this.fileLineExclude = "(?m)(?i:" + this.fileLineExclude + ")";
                this.fileLineExcludePattern = Pattern.compile(this.fileLineExclude);
            } else this.fileLineExcludePattern = null;

            if (this.fileInclude.length() > 0) {
                this.fileInclude = this.fileInclude.replace('\\', '/');
                this.fileInclude = this.fileInclude.replaceAll("\\/+", "/");
                this.fileInclude = Pattern.quote(this.fileInclude);
                this.fileInclude = this.fileInclude.replaceAll("\\*", "\\\\E.*\\\\Q");
                this.fileInclude = this.fileInclude.replaceAll("\\?", "\\\\E.\\\\Q");
                this.fileInclude = this.fileInclude.replaceAll(",\\s*", "\\\\E\\$)|(?i:\\\\Q");
                this.fileInclude = "(?m)(?i:" + this.fileInclude + ")";
                this.fileIncludePattern = Pattern.compile(this.fileInclude);
            } else this.fileIncludePattern = null;

            this.contentFilterPatternFlat = this.contentFilter;
            this.contentFilterPatternFlat = this.contentFilter.replaceAll("\\\\{2}", "\00");
            this.contentFilterPatternFlat = this.contentFilterPatternFlat.replaceAll("\\\\R", LINE_BREAK.replaceAll("\\\\", "\\\\\\\\"));
            this.contentFilterPatternFlat = this.contentFilterPatternFlat.replaceAll("\00", "\\\\\\\\");
            this.contentFilterPattern = Pattern.compile("(?m)" + this.contentFilterPatternFlat);
            
            if (this.contentExclude != null) {
                this.contentExclude = this.contentExclude.replaceAll("\\\\{2}", "\00");
                this.contentExclude = this.contentExclude.replaceAll("\\\\R", LINE_BREAK.replaceAll("\\\\", "\\\\\\\\"));
                this.contentExclude = this.contentExclude.replaceAll("\00", "\\\\\\\\");
                this.contentExcludePattern = Pattern.compile("(?m)" + this.contentExclude);
            }

            if (!this.todo.matches("^[A-Z]+\\:.*$")) {
                this.todo = this.todo.replaceAll("\\\\{2}", "\00");
                this.todo = this.todo.replaceAll("\\\\R", System.getProperty("line.separator"));
                this.todo = this.todo.replaceAll("\00", "\\\\\\\\");
            }
        }

        /**
         *  Performs the review task for a file.
         *  All found files will be processed. The filte filters decide whether
         *  a review must be performed.
         *  @param file file
         */
        private void perform(File file) {
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintStream output = new PrintStream(stream);
            PrintStream print = System.out;

            try {
                
                String path = file.getCanonicalPath().replace('\\', '/');
                if ((this.fileIncludePattern == null || !this.fileIncludePattern.matcher(path).find())
                        || (this.fileExcludePattern != null && this.fileExcludePattern.matcher(path).find())) return; 
                
                Review.reviews++;
                
                String contentShadow = "";
                for (boolean search = true; search;) {
                    
                    String content = new String(Review.readFile(file));
                    content = content.replaceAll(LINE_WHITE_SPACES, " ");

                    if (contentShadow.equals(content)) break;
                    contentShadow = content;
                    
                    Matcher matcher = this.contentFilterPattern.matcher(content);

                    while (true) {
                        
                        if (!matcher.find()) return;

                        if (this.contentExcludePattern != null) {
                            String matcherText = content.substring(matcher.start(), matcher.end());
                            Matcher ignorematcher = this.contentExcludePattern.matcher(matcherText);
                            if (ignorematcher.find()) continue;
                        }

                        String before = content.substring(0, matcher.start());
                        before = before.replaceAll("(?m)" + LINE_BREAK, "\00");
                        
                        int line = (before.length() -before.replaceAll("\00", "").length()) +1;
                        
                        before = before.replaceAll("^.*\00", "");
                        int offset = before.replaceAll("^.*\00", "").length() +1;

                        if (this.fileLineExcludePattern != null) {
                            String source = file.toString().replace('\\', '/') + "[" + line + "]";
                            Matcher lineMatcher = this.fileLineExcludePattern.matcher(source);
                            if (lineMatcher.find()) continue;
                            source = file.toString().replace('\\', '/') + "[" + line + ":" + offset + "]";
                            lineMatcher = this.fileLineExcludePattern.matcher(source);
                            if (lineMatcher.find()) continue;
                        }
                        
                        Review.founds++;
                        
                        if (!Review.verboseInfos) output.println(file);
                        
                        output.println("\tfound #" + this.lineNumber + " in line " + line + ", character " + offset);
                        output.println("\t" + this.contentFilterPatternFlat + (this.contentExclude != null ? " !" + this.contentExclude : ""));
                        
                        String preview = content.substring(matcher.start());
                        preview = preview.replaceAll("(?m)" + LINE_BREAK, "\00");
                        preview = before + preview.replaceAll("\00.*$", "");
                        offset -= preview.length() -preview.replaceAll("^\\s+", "").length();
                        String mark = "";
                        while (mark.length() < offset -1)
                            mark += " ";
                        
                        if (!Review.performReplacement || this.todo.matches("^\\s*[A-Z]+\\:.*$")) {
                            print = System.err;
                            String todoOut = this.todo;
                            if (this.todo.matches("^\\s*TEST|ECHO\\:.*$")) {
                                todoOut = content.substring(matcher.start(), matcher.end());
                                todoOut = todoOut.replaceAll(this.contentFilterPatternFlat,
                                    this.todo.replaceAll("\\x00", ""));
                            }
                            output.println("\t" + todoOut);
                            preview = preview.trim();
                            if (preview.length() > 0) {
                                output.println("\t" + preview.trim());
                                output.println("\t" + mark + "^^^");
                            }
                        } else {
                            String contentBefore = content.substring(0, matcher.start());
                            String contentMatch = content.substring(matcher.start(), matcher.end());
                            contentMatch = contentMatch.replaceAll(this.contentFilterPatternFlat,
                                this.todo.replaceAll("\\x00", ""));
                            String contentFollow = content.substring(matcher.end());
                            content = contentBefore + contentMatch + contentFollow;
                            Review.writeFile(file, content.getBytes());
                            Review.corrections++;
                            output.println();
                        }
                        
                        print.print(new String(stream.toByteArray()));
                        stream.reset();
                        
                        search = matcher.find();
                        break;
                    }
                }
           } catch (Throwable throwable) {
                Review.errors++;
                output.println("\terror in #" + this.lineNumber + " " + throwable);
                output.println("\t" + this.contentFilterPatternFlat);
                print = System.err;
            } finally {
                if (stream.size() <= 0 && Review.verboseInfos && Review.tasks.indexOf(this) == 0)
                    print.println(file);
                if (stream.size() > 0) {
                    print.println();
                    print.println(file);
                    print.print(new String(stream.toByteArray()));
                }
            }
        }
    }
}