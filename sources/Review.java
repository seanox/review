/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Review, text based code analysis
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

public class Review {
    
    private volatile static List<Task> tasks;
    private volatile static List<Worker> workers;
    
    private static long founds;
    private static long corrections;
    private static long timing;
    private static long volume;
    private static long files;
    private static long errors;
    private static long reviews;
    
    private static boolean performReplacement;
    private static boolean verboseInfos;
    
    private static final int THREADS = 25;
    
    private static byte[] readFile(File file) throws IOException {
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        Review.volume += bytes.length;
        return bytes;
    }
    
    private static void writeFile(File file, byte[] bytes) throws IOException {
        
        Files.write(file.toPath(), bytes);
        Review.files++;
        Review.volume += bytes.length;
    }
    
    private static String decodeTaskText(String text) {
        
        if (text == null) return null;
        text = text.replaceAll("\\t", "    ");
        text = text.replaceAll("[\\x00-\\x06]", " ").trim();
        text = text.replaceAll("%", "\\x25");
        return text;
    }
    
    private static Task[] readTasks(File file) throws IOException {

        List<Task> tasks = new ArrayList<>();
        String content = new String(Review.readFile(file));
        String pattern = new String();
        int index = 0;
        for (String line : content.split("(?:\\r\\n)|(?:\\n\\r)|[\\r\\n]")) {
            index++;
            pattern += (line.matches("^[^\\s].*") ? index + ":" + line : line) + "\n";
        }
        pattern = pattern.replaceAll("\\.{3,} *\n *\\.{3,}", " ");
        pattern = pattern.replaceAll("#.*[\r\n]+", "\n");
        pattern = pattern.replaceAll("[\\x00-\\x06]", " ");
        pattern = pattern.replaceAll("(\n)([^\\s])", "$1\00$2");
        String[] rules = pattern.split("\n\\x00");
        for (String rule : rules) {
            String[] lines = rule.split("\\n");
            if (lines.length < 2) continue;
            Task task = new Task();
            task.index = Integer.valueOf(lines[0].replaceAll("^(\\d+):(.*)$", "$1")).intValue();
            task.filter = Review.decodeTaskText(lines[0].replaceAll("^(\\d+):(.*)$", "$2"));
            if (lines.length > 1) {
                String expression = lines[1];
                String[] split = expression.split("\\s+!", 2);
                task.expression = Review.decodeTaskText(split[0]);
                if (split.length > 1) task.ignore = Review.decodeTaskText(split[1]);
                if (task.ignore != null && task.ignore.length() == 0)
                    task.ignore = null;
            }
            if (lines.length > 2) {
                task.todo = Review.decodeTaskText(lines[2]);
                task.todo = task.todo.replaceAll("\\\\(?i:x)(?i:([0-9a-f]{2}))", "%$1");
                task.todo = task.todo.replaceAll("\\+", "%2B");
                task.todo = URLDecoder.decode(task.todo, "ISO-8859-1");
            }
            
            if (task.filter == null
                    || task.filter.trim().length() == 0)
                continue;
            if (task.expression == null
                    || task.expression.trim().length() == 0)
                continue;
            
            task.initialize();
            tasks.add(task);
        }
        return tasks.toArray(new Task[0]);
    }
    
    private static Worker lockWorker(File file) {
        
        for (Worker worker : Review.workers)
            if (worker.lock(file)) return worker;
        return null;
    }
    
    private static boolean activeWorker() {
        
        for (Worker worker : Review.workers)
            if (worker.file != null) return true;
        return false;
    }
    
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
    
    public static void main(String[] options) throws IOException {
        
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
            } else pattern += "\00" + option;
        }
        
        if (pattern == null) {
            System.out.println("usage: java -cp review.jar <options> <pattern>");
            System.out.println("");
            System.out.println("\t-d directory (default work directory)");
            System.out.println("\t-x perform replacement");
            System.out.println("\t-v verbose infos");
            System.out.println("");
            System.out.println("\tpattern file with (anti) pattern");
            return;
        }
        
        System.out.println("Review #[ant:build-version] #[ant:build-year]#[ant:build-month]#[ant:build-day]");
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
        
        System.out.println();
        System.out.println("\tfound:\t\t" + Review.founds);
        System.out.println("\tfixed:\t\t" + Review.corrections);
        System.out.println("\terrors:\t\t" + Review.errors);
        long time = Math.max((System.currentTimeMillis() -Review.timing) /1000, 1);
        System.out.println("\tduration:\t" + time + " s");
        System.out.println("\treviews:\t" + Review.reviews + " (" + (Review.reviews /time) + " R/s)");
        System.out.println("\tfiles:\t\t" + Review.files + " (" + (Review.files /time) + " F/s)");
        long volume = Review.volume /1024 /1024;
        System.out.println("\tvolume:\t\t" + volume + " MB (" + (volume /time) + " MB/s)");
    }
    
    private static class Worker extends Thread {
        
        volatile File file;
        
        volatile long timing;
        
        private boolean lock(File file) {

            if (this.file != null)
                return false;
            this.file = file;
            return true;
        }
        
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

    private static class Task {

        volatile String  filter;
        volatile String  fileExclude;
        volatile Pattern fileExcludePattern;
        volatile String  fileInclude;
        volatile Pattern fileIncludePattern;
        volatile String  lineExclude;
        volatile Pattern lineExcludePattern;

        volatile String  expression;
        volatile Pattern expressionPattern;
        volatile String  expressionPatternFlat;
        volatile String  ignore;
        volatile Pattern ignorePattern;
        volatile String  todo;

        long index;
        
        private static String decodeLineSeparators(String pattern) {
            
            pattern = pattern.replaceAll("\\r|\\\\r", "\\\\x01");
            pattern = pattern.replaceAll("\\n|\\\\n", "\\\\x02");
            pattern = pattern.replaceAll("\\\\s", "(?:\\\\s|\\\\x01|\\\\x02)");
            return pattern;
        }
        
        private void initialize() {

            this.fileExclude = "";
            this.lineExclude = "";
            this.fileInclude = "";
            
            String filter = this.filter.replaceAll("[\\x00-\\x06]", " ");
            filter = filter.replaceAll("\\s+([\\+\\-])\\s+", " \00$1 ");
            for (String pattern : filter.split("\\00")) {
                if (pattern.startsWith("-")) {
                    if (pattern.matches("^.*\\[\\d+(:\\d+)*\\]\\s*$")) {
                        if (this.lineExclude.length() > 0) this.lineExclude += ", ";
                        this.lineExclude += pattern.replaceAll("^[\\+\\-]", "").trim();
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
                this.fileExclude = "(?i:" + this.fileExclude + ")";
                this.fileExcludePattern = Pattern.compile(this.fileExclude);
            } else this.fileExcludePattern = null;
            
            if (this.lineExclude.length() > 0) {
                this.lineExclude = this.lineExclude.replace('\\', '/');
                this.lineExclude = this.lineExclude.replaceAll("\\/+", "/");
                this.lineExclude = Pattern.quote(this.lineExclude);
                this.lineExclude = this.lineExclude.replaceAll("\\*", "\\\\E.*\\\\Q");
                this.lineExclude = this.lineExclude.replaceAll("\\?", "\\\\E.\\\\Q");
                this.lineExclude = this.lineExclude.replaceAll(",\\s*", "\\\\E\\$)|(?i:\\\\Q");
                this.lineExclude = "(?i:" + this.lineExclude + ")";
                this.lineExcludePattern = Pattern.compile(Task.decodeLineSeparators(this.lineExclude));
            } else this.lineExcludePattern = null;

            if (this.fileInclude.length() > 0) {
                this.fileInclude = this.fileInclude.replace('\\', '/');
                this.fileInclude = this.fileInclude.replaceAll("\\/+", "/");
                this.fileInclude = Pattern.quote(this.fileInclude);
                this.fileInclude = this.fileInclude.replaceAll("\\*", "\\\\E.*\\\\Q");
                this.fileInclude = this.fileInclude.replaceAll("\\?", "\\\\E.\\\\Q");
                this.fileInclude = this.fileInclude.replaceAll(",\\s*", "\\\\E\\$)|(?i:\\\\Q");
                this.fileInclude = "(?i:" + this.fileInclude + ")";
                this.fileIncludePattern = Pattern.compile(this.fileInclude);
            } else this.fileIncludePattern = null;

            this.expressionPatternFlat = this.expression;
            this.expressionPatternFlat = this.expression.replaceAll("\\\\{2}", "\00");
            this.expressionPatternFlat = this.expressionPatternFlat.replaceAll("\\\\R", "(?:(?:\\\\r\\\\n)|(?:\\\\n\\\\r)|[\\\\r\\\\n])");
            this.expressionPatternFlat = this.expressionPatternFlat.replaceAll("\00", "\\\\");
            this.expressionPattern = Pattern.compile(Task.decodeLineSeparators(this.expressionPatternFlat));
            
            if (this.ignore != null) {
                this.ignore = this.ignore.replaceAll("\\\\{2}", "\00");
                this.ignore = this.ignore.replaceAll("\\\\R", "(?:(?:\\\\r\\\\n)|(?:\\\\n\\\\r)|[\\\\r\\\\n])");
                this.ignore = this.ignore.replaceAll("\00", "\\\\");
                this.ignorePattern = Pattern.compile(Task.decodeLineSeparators(this.ignore));
            }

            if (!this.todo.matches("^[A-Z]+\\:.*$")) {
                this.todo = this.todo.replaceAll("\\\\{2}", "\00");
                this.todo = this.todo.replaceAll("\\\\R", System.getProperty("line.separator"));
                this.todo = this.todo.replaceAll("\00", "\\\\");
            }
        }

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
                    content = content.replaceAll("[\\x00-\\x06]", " ");
                    content = content.replace('\r', '\01');
                    content = content.replace('\n', '\02');

                    if (contentShadow.equals(content)) break;
                    contentShadow = content;
                    
                    Matcher matcher = this.expressionPattern.matcher(content);

                    while (true) {
                        
                        if (!matcher.find()) return;

                        if (this.ignorePattern != null) {
                            String matcherText = content.substring(matcher.start(), matcher.end());
                            Matcher ignorematcher = this.ignorePattern.matcher(matcherText);
                            if (ignorematcher.find()) continue;
                        }

                        String before = content.substring(0, matcher.start());
                        before = before.replaceAll("(?:\\x01\\x02)|(?:\\x02\\x01)|[\\x01\\x02]", "\00");
                        
                        int line = (before.length() -before.replaceAll("\00", "").length()) +1;
                        
                        before = before.replaceAll("^.*\00", "");
                        int offset = before.replaceAll("^.*\00", "").length() +1;

                        if (this.lineExcludePattern != null) {
                            String source = file.toString().replace('\\', '/') + "[" + line + "]";
                            Matcher lineMatcher = this.lineExcludePattern.matcher(source);
                            if (lineMatcher.find()) continue;
                            source = file.toString().replace('\\', '/') + "[" + line + ":" + offset + "]";
                            lineMatcher = this.lineExcludePattern.matcher(source);
                            if (lineMatcher.find()) continue;
                        }
                        
                        Review.founds++;
                        
                        if (!Review.verboseInfos) output.println(file);
                        
                        output.println("\tfound #" + this.index + " in line " + line + ", character " + offset);
                        output.println("\t" + this.expressionPatternFlat + (this.ignore != null ? " !" + this.ignore : ""));
                        
                        String preview = content.substring(matcher.start());
                        preview = preview.replaceAll("(?:\\x01\\x02)|(?:\\x02\\x01)|[\\x01\\x02]", "\00");
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
                                todoOut = todoOut.replaceAll(Task.decodeLineSeparators(this.expressionPatternFlat),
                                    this.todo.replaceAll("\\r", "\01").replaceAll("\\n", "\02").replaceAll("\\x00", ""));
                                todoOut = todoOut.replace('\01', '\r');
                                todoOut = todoOut.replace('\02', '\n');
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
                            contentMatch = contentMatch.replaceAll(Task.decodeLineSeparators(this.expressionPatternFlat),
                                this.todo.replaceAll("\\r", "\01").replaceAll("\\n", "\02").replaceAll("\\x00", ""));
                            String contentFollow = content.substring(matcher.end());
                            content = contentBefore + contentMatch + contentFollow;
                            content = content.replace('\01', '\r');
                            content = content.replace('\02', '\n');
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
                output.println("\terror in #" + this.index + " " + throwable);
                output.println("\t" + this.expressionPatternFlat);
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