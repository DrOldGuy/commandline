**2024-05-12  Rob Hewitt  <drrobhewitt@gmail.com>**

  * Version 1.0.6
  * Updated Spring Boot parent version to 3.2.5
  * FormatPrinter.java: 1) removed references to stdout and stderr. 2) changed method signature *public void print(String jarFileName, Options options)* to *public String formatHelp(String jarFileName, Options options)*.
  * Changed name for *FormatPrinter.java* to *HelpFormatter.java*.