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
    String artifactdirproperty;

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

    String stripQuotes( String value ) {
        if( value.startsWith("\"") && value.endsWith("\"" ) ) {
            value = value.substring(1, value.length() - 1 );
        }
        return value;
    }

    public void setCMakeHome( String cmakeHome ) {
        cmakeHome = stripQuotes( cmakeHome );
        this.cmakeHome = cmakeHome;
    }
    public void setReleaseType( String releaseType ) {
        this.releaseType = releaseType;
    }
    public void setGenerator( String generator ) {
        generator= stripQuotes( generator );
        this.generator = generator;
    }
    public void setSrcdir( String srcdir ) {
        this.srcdir = srcdir;
    }
    public void setBuilddir( String builddir ) {
        this.builddir = builddir;
    }
    public void setArtifactdirproperty( String artifactdirproperty ) {
        this.artifactdirproperty = artifactdirproperty;
    }
    public static final int getJvmBits() {
        if( System.getProperty("os.arch").toLowerCase().equals("x86") ) {
             return 32;
        }
        if( System.getProperty("os.arch").toLowerCase().equals("i386") ) {
             return 32;
        }
        return 64;
    }
    public static final boolean getIsWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }
    public void execute() throws BuildException {
        String basedir = getProject().getBaseDir().getAbsolutePath();
        srcdir = new File( basedir + "/" + srcdir ).getAbsolutePath();
        builddir = new File( basedir + "/" + builddir ).getAbsolutePath();

        if( cmakeHome == null || cmakeHome.equals("") || cmakeHome.equals("${cmake_home}" ) ) {
            if( new File("/usr/bin/cmake").exists() ) {
                cmakeHome = "/usr";
            }
        }
        if( cmakeHome == null || cmakeHome.equals("") || cmakeHome.equals("${cmake_home}" ) ) {
            if( new File("/usr/local/bin/cmake").exists() ) {
                cmakeHome = "/usr/local";
            }
        }
        if( cmakeHome == null || cmakeHome.equals("") || cmakeHome.equals("${cmake_home}" ) ) {
            if( new File("c:\\program files\\cmake").exists() ) {
                cmakeHome = "c:\\program files\\cmake";
            }
        }
        if( cmakeHome == null || cmakeHome.equals("") || cmakeHome.equals("${cmake_home}" ) ) {
            if( new File("c:\\program files (x86)\\cmake").exists() ) {
                cmakeHome = "c:\\program files (x86)\\cmake";
            }
        }

        int jvmBits = getJvmBits();
        boolean isWindows = getIsWindows();
        if( generator == null || generator.equals("") || generator.equals("${generator}" ) ) {
            if( isWindows ) {
                generator = "Visual Studio 12 2013";
                if( new File( "c:\\program files (x86)\\visual studio 10.0" ).exists() ) {
                    generator = "Visual Studio 10 2010";
                }
                if( new File( "c:\\program files\\visual studio 10.0" ).exists() ) {
                    generator = "Visual Studio 10 2010";
                }
                if( new File( "c:\\program files (x86)\\visual studio 11.0" ).exists() ) {
                    generator = "Visual Studio 11 2012";
                }
                if( new File( "c:\\program files\\visual studio 11.0" ).exists() ) {
                    generator = "Visual Studio 11 2012";
                }
                if( new File( "c:\\program files (x86)\\visual studio 12.0" ).exists() ) {
                    generator = "Visual Studio 12 2013";
                }
                if( new File( "c:\\program files\\visual studio 12.0" ).exists() ) {
                    generator = "Visual Studio 12 2013";
                }
                if( new File( "c:\\program files (x86)\\visual studio 14.0" ).exists() ) {
                    generator = "Visual Studio 14 2015";
                }
                if( new File( "c:\\program files\\visual studio 14.0" ).exists() ) {
                    generator = "Visual Studio 14 2015";
                }
                if( jvmBits == 64 ) {
                    generator += " Win64";
                }
            } else {
                generator = "Unix Makefiles";
            }
        }

        System.out.println("Running cmake...");
        System.out.println("CMakeHome: " + cmakeHome);
        System.out.println("ReleaseType: " + releaseType);
        System.out.println("Generator: " + generator);
        System.out.println("Srcdir: " + srcdir);
        System.out.println("Builddir: " + builddir);

        if( !new File( cmakeHome + "/bin/cmake" ).exists() && !new File( cmakeHome + "/bin/cmake.exe" ).exists() ) {
            throw new BuildException( new File( cmakeHome + "/bin/cmake" ).getAbsolutePath() + " doesn't exist.  Please check -Dcmake_home");
        }
        if( !new File( builddir ).exists() ) {
            throw new BuildException( "builddir " + srcdir + " doesn't exist.");
        }
        if( !new File( builddir ).exists() ) {
            throw new BuildException( "srcdir " + builddir + " doesn't exist.");
        }

        try{ 
            execUsingGobbler( new String[]{ cmakeHome + "/bin/cmake", "-G", generator,
                "-D", "CMAKE_BUILD_TYPE:STRING=" + releaseType, srcdir }, builddir );
        } catch( Exception e ) {
            throw new BuildException("failed to run cmake");
        }

        if( generator.equals("Unix Makefiles") ) {
            try {
                execUsingGobbler(new String[]{"make"}, builddir );
                if( artifactdirproperty != null ) {
                    getProject().setProperty(artifactdirproperty, builddir );
                }
            } catch( Exception e ) {
                throw new BuildException("Error running make");
            }   
        } else if( generator.startsWith("Visual Studio") ) {
            try {
                execUsingGobbler(new String[]{"C:/WINDOWS/Microsoft.NET/Framework/v4.0.30319/MSBuild.exe", "ALL_BUILD.vcxproj", "/p:Configuration=" + releaseType }, builddir );
                if( artifactdirproperty != null ) {
                    getProject().setProperty(artifactdirproperty, builddir + "\\" + releaseType );
                }
            } catch( Exception e ) {
                throw new BuildException("Error running msbuild");
            }   
        } else {
            throw new BuildException("Generator " + generator + " not supported by cmake-for-ant.");
        }

    }
}

