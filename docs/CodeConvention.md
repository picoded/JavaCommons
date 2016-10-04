#Code Convention 
+ use PascalCasing for class names
+ use camelCasing for method names, method arguments and local variables.
+ use lowercase names for packages
+ As much as practically possible: Make packages isolated from one another. ie, avoid cross package dependencies.
+ SmartTabs: This is a combination of both tabs and spaces. Treat tabs as 3 spaces.
+ Code Documentation: Every public function must have its respective documentation. (Markdown preferred, but not enforced).
+ dOxygen documentation should be enclosed in "///" blocks
+ Function / Variables without documentation must have self-descriptive names.
+ Aim for 0 build warnings. View warnings using ant lint
+ All required files are needed to be inside the repository. Except for Java 7 JDK/JRE files, and dOxygen build files.
+ All test files classes ends with _test suffix
