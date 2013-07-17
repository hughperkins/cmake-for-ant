cmake-for-ant
=============

cmake task for ant.  Tested on Visual Studio 2010, 2012; linux.  Simple, easy to configure.

Example ant file
================

    <project default="testtask">
        <target name="testtask">
            <property name="generator" value="Unix Makefiles" />
            <property name="cmake_home" value="/usr" />
            <taskdef name="cmake" classname="CMake" classpath="jar/cmake-for-ant.jar" />
            <cmake cmakehome="${cmake_home}" releasetype="Release" generator="${generator}" 
                   srcdir="cpp" builddir="cpp/build" />
        </target>
    </project>

Then, on linux, we could build this as follows:

    ant

(That's it!)

On Window, it's slightly more involved, because we need to specify the version of Visual Studio to use, and the location of cmake:

    ant -Dcmake_home="C:\Program Files (x86)\CMake-2.8" -Dgenerator="Visual Studio 10"

or, if we're using Visual Studio 2012:

    ant -Dcmake_home="C:\Program Files (x86)\CMake-2.8" -Dgenerator="Visual Studio 11"

In this build.xml file, we can see two parts.  Firstly, the definition of the cmake task:

    <taskdef name="cmake" classname="CMake" classpath="jar/cmake-for-ant.jar" />

We need to specify the location of the cmake-for-ant.jar file here.  The cmake-for-ant.jar file is cross-platform, portable.

Secondly, we run cmake, stating the location of cmake, the release type ("Release" or "Debug"), the cmake generator name, the srcdir containing the build.xml file, and the build directory.

How to build
============

Clone, eg:

    git clone https://github.com/hughperkins/cmake-for-ant.git

Build using ant:

    cd cmake-for-ant
    ant

You can test it works ok, by building the test project.  On linux:

    ant test 

On Windows, if you're using Visual Studio 2010:

    ant test -Dcmake_home="C:\Program Files (x86)\CMake-2.8" -Dgenerator="Visual Studio 10"

(You will need to modify your cmake home appropriately).  If you have Visual Studio 2011:

    ant test -Dcmake_home="C:\Program Files (x86)\CMake-2.8" -Dgenerator="Visual Studio 11"

How does it compare to cmakeant?
================================

 * it works with Visual Studio 2010 and Visual Studio 2012
 * it's easy to configure it to point to your CMake installation

License
=======

Mozilla Public License v2.0

How easy is it to maintain?
===========================

It's 110 lines.  How hard can it be? :-)


