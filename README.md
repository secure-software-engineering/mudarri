# Source code of Mudarri

## Installation manual

- Download the code from [https://github.com/secure-software-engineering/mudarri.git](https://github.com/secure-software-engineering/mudarri.git)
- Have Java 8 running on the machine.
- Install the Android SDK.
- Import the modules Automaton and TaintAnalysis in an IntelliJ project, as Java applications. Set their JDKs to Java 8.
- Import the module SC1_Plugin in the same IntelliJ project, as an IntelliJ Plugin. Set its JDKs to the default JDK for IntelliJ Plugins.
- Replace ANDROID_PLATFORMS to the path to the Android SDK in the file TaintAnalysis/src/main/java/main/Config.java

- The unit tests of TaintAnalysis should run. They are located in TaintAnalysis/src/test/analysis.

- Run SC1_Plugin an IntelliJ plugin.
- In the new IntelliJ application, create a new project and import one or more of the applications in the target/ directory.
- Make sure the applications compile correctly.
- Run the analysis using the top menu: Analysis > Run SC1 analysis. The analysis results should show in the bottom SC1 view.

