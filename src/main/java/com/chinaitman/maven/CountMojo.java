package com.chinaitman.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengzhe
 * @date 2019-04-02 21:53
 * @description
 * @goal count
 */

public class CountMojo extends AbstractMojo {

    private static final String[] INCLUDED_DEFAULTS = {"java", "xml", "properties"};
    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File basedir;
    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private File sourceDirecoty;
    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     * @readonly
     */
    private File testSourceDirectory;
    /**
     * @parameter expression="${project.resources}"
     * @required
     * @readonly
     */
    private List<Resource> resources;
    /**
     * @parameter expression="${project.testResources}"
     * @required
     * @readonly
     */
    private List<Resource> testResources;
    /**
     * @parameter
     */
    private String[] includes;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (includes == null || includes.length == 0) {
            includes = INCLUDED_DEFAULTS;
        }

        try {
            countDir(sourceDirecoty);
            countDir(testSourceDirectory);
            for (Resource resource : resources) {
                countDir(new File(resource.getDirectory()));
            }
            for (Resource resource : testResources) {
                countDir(new File(resource.getDirectory()));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to count lines of code", e);
        }
    }

    private void countDir(File dir) throws IOException {
        if (!dir.exists()) {
            return;
        }
        List<File> collected = new ArrayList<File>();
        collectFiles(collected, dir);
        int lines = 0;
        for (File sourceFile : collected) {
            lines += countLine(sourceFile);
        }
        String path = dir.getAbsolutePath().substring(basedir.getAbsolutePath().length());
        getLog().info(path + ":" + lines + "lines of code in" + collected.size() + "files");
    }

    private void collectFiles(List<File> collected, File file) {
        if (file.exists()) {
            for (String include : includes) {
                if (file.getName().endsWith("." + include)) {
                    collected.add(file);
                    break;
                }
            }
        } else {
            for (File sub : file.listFiles()) {
                collectFiles(collected, sub);
            }
        }
    }

    private int countLine(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        int line = 0;
        try {
            while (reader.ready()) {
                reader.readLine();
                line++;
            }
        } finally {
            reader.close();
        }
        return line;
    }
}
