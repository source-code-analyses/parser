package org.codeontology.buildsystems.maven;

import org.codeontology.CodeOntology;
import org.codeontology.buildsystems.ClasspathLoader;
import org.codeontology.buildsystems.DependenciesLoader;
import org.codeontology.buildsystems.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

public class MavenLoader extends DependenciesLoader<MavenProject> {
    private static final String PATH_TO_DEPENDENCIES = "/target/dependency/";
    private final File output;
    private final File error;
    private static boolean m2Loaded = false;

    public MavenLoader(MavenProject project) {
        super(project);
        error = new File(project.getPath() + "/error");
        output = new File(project.getPath() + "/output");
    }

    @Override
    public void loadDependencies() {
        System.out.println("Loading dependencies with Maven");
        try {
            if (CodeOntology.downloadDependencies()) {
                downloadDependencies();
                jarModules();
            }

            ProcessBuilder builder = new ProcessBuilder("mvn", "dependency:build-classpath", "-Dmdep.outputFile=.cp");
            builder.directory(getProject().getProjectDirectory());
            builder.redirectError(error);
            builder.redirectOutput(output);
            int exitStatus = builder.start().waitFor();

            if (exitStatus == 0) {
                File classpath = new File(getProject().getPath() + "/.cp");
                Scanner reader = new Scanner(classpath);
                reader.useDelimiter("\\Z");
                if (reader.hasNext()) {
                    getLoader().loadClasspath(reader.next());
                }
                reader.close();
                classpath.deleteOnExit();
            } else {
                getLoader().loadAllJars(getProject().getProjectDirectory());
                if (!m2Loaded) {
                    ClasspathLoader loader = getLoader();
                    loader.lock();
                    loader.loadAllJars(System.getProperty("user.home") + "/.m2");
                    loader.release();
                    m2Loaded = true;
                }
            }


            Collection<Project> modules = getProject().getSubProjects();
            for (Project module : modules) {
                System.out.println("Running on module " + module.getPath());
                module.getLoader().loadDependencies();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get dependencies for maven project in
     * folder {@code projectRoot}, and save them in
     * projectRoot/target/dependency/.
     */
    public void downloadDependencies() {
        try {
            File downloadDirectory = new File(getProject().getPath() + PATH_TO_DEPENDENCIES);

            if (!downloadDirectory.exists()) {
                if (!downloadDirectory.mkdirs()) {
                    throw new IOException("Could not create download directory for dependencies");
                }
            }

            System.out.println("Downloading dependencies...");
            ProcessBuilder builder = new ProcessBuilder("mvn", "dependency:copy-dependencies");
            builder.directory(getProject().getProjectDirectory());
            builder.redirectError(error);
            builder.redirectOutput(output);

            builder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create jars for every dependency in {@code projectRoot}.
     * Output folder is the project root, name goes as:
     * {@code dependencyName.jar}.
     */
    public void jarModules() {
        getProject().getSubProjects().forEach(module -> {
            if (!module.getProjectDirectory().toPath().equals(getProject().getProjectDirectory().toPath())) {
                System.out.println("Preparing module " + module.getPath());
                try {
                    ProcessBuilder builder = new ProcessBuilder("mvn", "jar:jar");
                    builder.directory(module.getProjectDirectory());
                    builder.redirectError(error);
                    builder.redirectOutput(output);

                    builder.start().waitFor();

                    //Runtime.getRuntime().exec("mvn jar:jar", new String[]{}, module).waitFor();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
