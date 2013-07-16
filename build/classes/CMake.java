// Copyright Hugh Perkins 2013, hughperkins -at- gmail
//
// This Source Code Form is subject to the terms of the Mozilla Public License, 
// v. 2.0. If a copy of the MPL was not distributed with this file, You can 
// obtain one at http://mozilla.org/MPL/2.0/.

import org.apache.tools.ant.*;
import java.util.*;
import java.io.*;

public class CMake extends Task {
    String cmakeHome;
    String releaseType;
    String generator;
    String srcdir;
    String builddir;

    // from http://stackoverflow.com/questions/14165517/processbuilder-capturing-stdout-and-stderr-of-started-processes-to-another-stre
    class StreamGobbler extends Thread {
        InputStream is;
        String type;

        private StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    System.out.println(type + "> " + line);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    void execUsingGobbler( String[] cmdstrings, String directory ) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmdstrings);
        for( String string : cmdstrings ) {
            System.out.println("cmdstring: " + string );
        }
        System.out.println("working directory: " + directory );
        pb.directory(new File( directory ) );
        Process process = pb.start();
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");

        outputGobbler.start();
        errorGobbler.start();
        process.waitFor();
    }

    public void setCMakeHome( String cmakeHome ) {
        this.cmakeHome = cmakeHome;
    }
    public void setReleaseType( String releaseType ) {
        this.releaseType = releaseType;
    }
    public void setGenerator( String generator ) {
        this.generator = generator;
    }
    public void setSrcdir( String srcdir ) {
        this.srcdir = srcdir;
    }
    public void setBuilddir( String builddir ) {
        this.builddir = builddir;
    }
    public void execute() throws BuildException {
        String basedir = getProject().getBaseDir().getAbsolutePath();
        srcdir = new File( basedir + "/" + srcdir ).getAbsolutePath();
        builddir = new File( basedir + "/" + builddir ).getAbsolutePath();

        System.out.println("Running cmake...");
        System.out.println("CMakeHome: " + cmakeHome);
        System.out.println("ReleaseType: " + releaseType);
        System.out.println("Generator: " + generator);
        System.out.println("Srcdir: " + srcdir);
        System.out.println("Builddir: " + builddir);

        try{ 
            execUsingGobbler( new String[]{ cmakeHome + "/bin/cmake", "-G", generator,
                "-D", "CMAKE_BUILD_TYPE:STRING=" + releaseType, srcdir }, builddir );
        } catch( Exception e ) {
            throw new BuildException("failed to run cmake");
        }

        if( generator.equals("Unix Makefiles") ) {
            try {
                execUsingGobbler(new String[]{"make"}, builddir );
            } catch( Exception e ) {
                throw new BuildException("Error running make");
            }   
        } else if( generator.startsWith("Visual Studio") ) {
            try {
                execUsingGobbler(new String[]{"msbuild", "Project.sln", "/p:Configuration=" + releaseType }, builddir );
            } catch( Exception e ) {
                throw new BuildException("Error running msbuild");
            }   
        } else {
            throw new BuildException("Generator " + generator + " not supported by CmakeFromAnt");
        }

    }
}

