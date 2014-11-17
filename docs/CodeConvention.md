#Code Convention 
+ use PascalCasing for class names and method names.
+ use camelCasing for packages, method arguments and local variables.
+ As much as pratically possible: Make packages isolated from one another. ie, avoid cross package dependencies.
+ SmartTabs: This is a combination of both tabs and spaces
+ Code Documentation: Every public function must have its respective documentation. (Markdown preferred, but not enforced).
+ dOxygen documentation should be enclosed in "///" blocks
+ Function / Variables without documentation must have self-descriptive names.
+ Aim for 0 build warnings. View warnings using ant lint
+ All required files are needed to be inside the repository. Except for Java 7 JDK/JRE files, and dOxygen build files.
+ picodedX is reserved for experimental, and not fully formalized / tested classes.
+ picodedTests is for test cases, and should follow the same class structure as picoded. However all test files classes ends with _test suffix