package org.codeontology;

import com.martiansoftware.jsap.JSAPException;
import org.apache.commons.io.FileUtils;
import org.codeontology.extraction.*;
import org.codeontology.projects.DependenciesLoader;
import org.codeontology.projects.Project;
import org.codeontology.projects.ProjectFactory;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import spoon.Launcher;
import spoon.compiler.ModelBuildingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class CodeOntology {
    private static CodeOntology codeOntology;
    private static int status = 0;
    private boolean downloadDependencies;
    private CodeOntologyArguments arguments;
    private Launcher spoon;
    private boolean exploreJarsFlag;
    private Project project;
    private ProjectEntity<?> projectEntity;
    private DependenciesLoader<? extends Project> loader;
    private PeriodFormatter formatter;
    private int tries;
    private String[] directories = {"test", "examples", "debug", "androidTest", "samples", "sample", "example", "demo", ".*test.*", ".*demo.*", ".*sample.*", ".*example.*"};

    private CodeOntology(String[] args) {
        try {
            spoon = new Launcher();
            arguments = new CodeOntologyArguments(args);
            exploreJarsFlag = arguments.exploreJars() || (arguments.getJarInput() != null);
            ReflectionFactory.getInstance().setParent(spoon.createFactory());
            RDFLogger.getInstance().setOutputFile(arguments.getOutput());
            downloadDependencies = arguments.downloadDependencies();
            formatter = new PeriodFormatterBuilder()
                    .appendHours()
                    .appendSuffix(" h ")
                    .appendMinutes()
                    .appendSuffix(" min ")
                    .appendSeconds()
                    .appendSuffix(" s ")
                    .appendMillis()
                    .appendSuffix(" ms")
                    .toFormatter();

            setUncaughtExceptionHandler();

        } catch (JSAPException e) {
            System.out.println("Could not process arguments");
        }
    }

    public static void main(String[] args) {
        codeOntology = new CodeOntology(args);
        try {
            codeOntology.processSources();
            codeOntology.processProjectStructure();
            codeOntology.processJars();
            codeOntology.postCompletionTasks();
        } catch (Exception | Error e) {
            codeOntology.handleFailure(e);
        }
        exit(status);
    }

    private void processSources() {
        try {
            if (isInputSet()) {
                System.out.println("Running on " + getArguments().getInput());

                project = ProjectFactory.getInstance().getProject(getArguments().getInput());

                loadDependencies();

                if (!getArguments().doNotExtractTriples()) {
                    spoon();
                    extractAllTriples();
                }
            }
        } catch (Exception e) {
            handleFailure(e);
        }
    }

    private void processProjectStructure() {
        if (getArguments().extractProjectStructure() && project != null) {
            getProjectEntity().extract();
            RDFLogger.getInstance().writeRDF();
        }
    }

    public void handleFailure(Throwable t) {
        System.out.println("It was a good plan that went awry.");
        if (t != null) {
            if (t.getMessage() != null) {
                System.out.println(t.getMessage());
            }
            if (getArguments().stackTraceMode()) {
                t.printStackTrace();
            }
        }
        status = -1;
    }

    private void spoon() {
        checkInput();
        try {
            long start = System.currentTimeMillis();
            spoon.addInputResource(getArguments().getInput());
            System.out.println("Building model...");
            spoon.buildModel();
            long end = System.currentTimeMillis();
            Period period = new Period(start, end);
            System.out.println("Model built successfully in " + formatter.print(period));

        } catch (ModelBuildingException e) {
            if (getArguments().removeTests() && tries < directories.length) {
                boolean result;
                do {
                    result = removeDirectoriesByName(directories[tries]);
                    tries++;
                } while (!result && tries < directories.length);

                if (result) {
                    spoon = new Launcher();
                    spoon();
                    return;
                }
            }
            throw e;
        }
    }

    private void loadDependencies() {
        long start = System.currentTimeMillis();
        loader = project.getLoader();
        loader.loadDependencies();

        String classpath = getArguments().getClasspath();

        if (classpath != null) {
            loader.loadClasspath(classpath);
        }
        long end = System.currentTimeMillis();
        System.out.println("Dependencies downloaded in " + formatter.print(new Period(start, end)) + ".");
    }

    private void extractAllTriples() {
        long start = System.currentTimeMillis();

        System.out.println("Extracting triples...");
        spoon.addProcessor(new SourceProcessor());
        spoon.process();
        RDFLogger.getInstance().writeRDF();

        long end = System.currentTimeMillis();

        Period period = new Period(start, end);
        System.out.println("Triples extracted successfully in " + formatter.print(period) + ".");
        spoon = new Launcher();
    }

    private void processJars() {
        long start = System.currentTimeMillis();
        String path = getArguments().getJarInput();
        if (path != null) {
            JarProcessor processor = new JarProcessor(path);
            processor.process();
        }

        if (getArguments().exploreJars() && loader != null) {
            Set<File> jars = loader.getJarsLoaded();
            for (File jar : jars) {
                new JarProcessor(jar).process();
            }

            long end = System.currentTimeMillis();
            Period period = new Period(start, end);
            System.out.println("Jar files processed successfully in " + formatter.print(period) + ".");
        }
    }

    private void postCompletionTasks() {
        if (getInstance().getArguments().shutdownFlag()) {
            Thread shutdownThread = new Thread(() -> {
                try {
                    System.out.println("Shutting down...");
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "sleep 3; shutdown -h now");
                    processBuilder.start();
                } catch (Exception e) {
                    System.out.println("Shutdown failed");
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
    }

    private void checkInput() {
        File input = new File(getArguments().getInput());
        if (!input.exists()) {
            System.out.println("File " + input.getPath() + " doesn't seem to exist.");
            System.exit(-1);
        }
        if (!input.canRead() && !input.setReadable(true)) {
            System.out.println("File " + input.getPath() + " doesn't seem to be readable.");
            System.exit(-1);
        }
    }

    public static CodeOntology getInstance() {
        return codeOntology;
    }

    public CodeOntologyArguments getArguments() {
        return arguments;
    }

    public static boolean downloadDependencies() {
        return getInstance().downloadDependencies;
    }

    public static void signalDependenciesDownloaded() {
        getInstance().downloadDependencies = true;
    }

    public static boolean verboseMode() {
        return getInstance().getArguments().verboseMode();
    }

    public static boolean isJarExplorationEnabled() {
        return getInstance().exploreJarsFlag;
    }

    private boolean isInputSet() {
        return getArguments().getInput() != null;
    }

    private boolean removeDirectoriesByName(String name) {
        try {
            Path[] tests = Files.walk(Paths.get(getArguments().getInput()))
                    .filter(path -> match(path, name) && path.toFile().isDirectory())
                    .toArray(Path[]::new);

            if (tests.length == 0) {
                return false;
            }

            for (Path testPath : tests) {
                System.out.println("Removing " + testPath.toFile().getAbsolutePath());
                FileUtils.deleteDirectory(testPath.toFile());
            }
        } catch (IOException e) {
            showWarning(e.getMessage());
        }

        return true;
    }

    private boolean match(Path path, String name) {
        if (!name.contains("*")) {
           return path.toFile().getName().equals(name);
        } else {
            Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            return pattern.matcher(path.toFile().getName()).matches();
        }
    }

    public static void showWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    private void setUncaughtExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) ->  exit(-1));
    }

    private static void exit(final int status) {
        try {
            // setup a timer, so if nice exit fails, the nasty exit happens
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Runtime.getRuntime().halt(status);
                }
            }, 30000);

            // try to exit nicely
            System.exit(status);

        } catch (Throwable t) {
            try {
                Thread.sleep(30000);
                Runtime.getRuntime().halt(status);
            } catch (Exception | Error e) {
                Runtime.getRuntime().halt(status);
            }
        }

        Runtime.getRuntime().halt(status);
    }

    public static ProjectEntity<?> getProject() {
        return codeOntology.getProjectEntity();
    }

    public static boolean extractProjectStructure() {
        return codeOntology.getArguments().extractProjectStructure();
    }

    public ProjectEntity<?> getProjectEntity() {
        if (projectEntity == null) {
            ProjectVisitor visitor = new ProjectVisitor();
            project.accept(visitor);
            projectEntity = visitor.getLastEntity();
        }

        return projectEntity;
    }

    public static boolean processStatements() {
        return codeOntology.getArguments().processStatements();
    }
}
