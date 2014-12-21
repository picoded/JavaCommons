#The following covers the basic setup

+ Setup java beautifier pre-commit hook: ln -s ./pre-commit.sh .git/hooks/pre-commit

#The following covers the ANT build commands

+---------------+-------------------------------------------------------------------------------+
| clean         | Cleanup all the various build files, built docs, and caches                   |
| compile-lib   | Copies all the various required lib files into the build area                 |
| compile-src   | Compiles all the various source code under 'src/picoded'                      |
| compile-srcX  | Compiles all the various experimental source code under 'src/picodedx'        |
| compile-tests | Compiles all the various test cases                                           |
| build         | Package the current build (or compile-src, if not called previously)          |
| src-beautify  | Applies the java beautifier script with current project convention            |
| compile       | Alias to compile-src                                                          |
| source        | Alias to compile-src                                                          |
+---------------+-------------------------------------------------------------------------------+