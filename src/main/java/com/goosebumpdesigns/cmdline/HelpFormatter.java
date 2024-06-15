// Copyright (c) 2023 Goosebump Designs LLC

package com.goosebumpdesigns.cmdline;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.goosebumpdesigns.cmdline.exception.CmdLineException;
import com.goosebumpdesigns.cmdline.model.Option;
import com.goosebumpdesigns.cmdline.model.Options;
import com.goosebumpdesigns.cmdline.model.Parameter;

/**
 * This class is the console print utility for the command line package. It prints to sysout and
 * syserr by default but can be set to something else. Utility packages can call the methods in this
 * class to print to a common console printer.
 */
@Service
public class HelpFormatter {
  private static final int MAX_WIDTH = 80;
  private static final int MAX_WIDTH_MIN = 40;
  private static final int MAX_WIDTH_MAX = 200;

  private int maxWidth = MAX_WIDTH;

  /**
   * Set the maximum width of each printed line. The default is 80 characters.
   * 
   * @param maxWidth The maximum line width.
   * @throws CmdLineException Thrown if the line width is less than the minimum or greater than the
   *         maximum allowed line width.
   */
  public HelpFormatter maxWidth(int maxWidth) {
    if(maxWidth < MAX_WIDTH_MIN || maxWidth > MAX_WIDTH_MAX) {
      throw new CmdLineException("Max width must be between " + MAX_WIDTH_MIN + " and "
          + MAX_WIDTH_MAX + " characters in length.");
    }

    this.maxWidth = maxWidth;
    return this;
  }

  /**
   * @return The maximum width of the line to print when the formatter formats the lines for
   *         printing.
   */
  public int maxWidth() {
    return maxWidth;
  }

  /**
   * Splits the line by the given length and return a list of split lines.
   * 
   * @param line The line to split.
   */
  public List<String> split(String line, int width) {
    List<String> lines = new LinkedList<>();

    if(Objects.nonNull(line)) {
      if(line.isBlank()) {
        lines.add("");
      }
      else {
        while(line.length() > 0) {
          int splitPoint = findSplitPoint(line, width);
          lines.add(line.substring(0, splitPoint));

          if(splitPoint >= line.length()) {
            line = "";
          }
          else {
            line = line.substring(splitPoint + 1);
          }
        }
      }
    }

    return lines;
  }

  public List<String> split(String line) {
    return split(line, maxWidth);
  }

  /**
   * Find the point at which to split a line. Lines are split on spaces.
   * 
   * @param line The line to split.
   * @return The split point.
   */
  private int findSplitPoint(String line, int max) {
    if(line.length() <= max) {
      return line.length();
    }

    int splitPoint = max;

    while(splitPoint > 0) {
      char ch = line.charAt(splitPoint);

      if(ch == ' ') {
        break;
      }

      splitPoint -= 1;
    }

    if(splitPoint == 0) {
      splitPoint = max;
    }

    return splitPoint;
  }

  /**
   * Print the help. This prints the general usage instructions as well as instructions for each
   * option and parameter.
   * 
   * @param instructions These are the instructions for the utility (i.e., general usage
   *        instructions).
   * @param jarFileName The name of the Java JAR file. If not {@code null}, this will be prepended
   *        with "java -jar ".
   * @param options The command line options.
   */
  public String formatHelp(String jarFileName, Options options) {
    options.validate();

    /* Add 2 to the length for the "--" on each option name. */
    int maxOptionLength = options.findMaxLength() + 2;
    int maxWidth = maxWidth();
    List<String> lines = new LinkedList<>();

    lines.addAll(generateInstruction(options.getDescription()));
    lines.addAll(generateUsageInstructions(jarFileName));
    lines.addAll(generateOptionsInstructions(options, maxOptionLength, maxWidth));
    lines.addAll(generateParameterInstructions(options, maxOptionLength));
    
    List<String> split = new LinkedList<>();
    
    for(String line : lines) {
      split.addAll(split(line, maxWidth));
    }

    return split.stream().collect(Collectors.joining(System.lineSeparator()));
  }

  /**
   * Prints the parameters, if any, to the currently print stream truncated to the maximum line
   * length.
   * 
   * @param options The list of {@link Option}s and {@link Parameter}s.
   * @param maxOption The longest parameter or option name length.
   */
  private List<String> generateParameterInstructions(Options options, int maxOption) {
    List<String> lines = new LinkedList<>();

    if(!options.getParameters().isEmpty()) {
      lines.add("Parameters:");

      for(Parameter parameter : options.getParameters()) {
        String name = parameter.getName();
        String useType = parameter.isRequired() ? "[Required] " : "[Optional] ";
        List<String> help =
            split(useType + parameter.getHelp(), maxWidth() - maxOption - Options.SPACE_LENGTH * 2);

        lines.add(format(name, maxOption, help));

        while(!help.isEmpty()) {
          lines.add(format("", maxOption, help));
        }

        lines.add("");
      }
    }

    return lines;
  }

  /**
   * @param options
   * @param maxOptionLength
   * @return
   */
  private List<String> generateOptionsInstructions(Options options, int maxOption, int maxWidth) {
    List<String> lines = new LinkedList<String>();

    if(Objects.nonNull(options) && !options.getNameMap().isEmpty()) {
      lines.add("Options:");

      for(Option option : options.getNameMap().values()) {
        String name = "--" + option.getName();
        String shortName =
            Objects.isNull(option.getShortName()) ? "" : "-" + option.getShortName().toString();
        String useType = option.isRequired() ? "[Required] " : "[Optional] ";
        List<String> help =
            split(useType + option.getHelp(), maxWidth - maxOption - Options.SPACE_LENGTH * 2);

        lines.add(format(name, maxOption, help));
        lines.add(format(shortName, maxOption, help));

        while(!help.isEmpty()) {
          lines.add(format("", maxOption, help));
        }

        lines.add("");
      }
    }

    return lines;
  }

  /**
   * @param jarFileName
   * @return
   */
  private List<String> generateUsageInstructions(String jarFileName) {
    if(Objects.nonNull(jarFileName) && !jarFileName.isBlank()) {
      return List.of("java -jar " + jarFileName.trim(), "");
    }

    return List.of();
  }

  /**
   * @param instructions
   * @return
   */
  private List<String> generateInstruction(String instructions) {
    if(Objects.nonNull(instructions) && !instructions.isBlank()) {
      return List.of(instructions, "");
    }

    return List.of();
  }

  /**
   * Format a line indented using the default {@link #SPACE_LENGTH}.
   * 
   * @param opName The long or short name of the option.
   * @param maxOptionLength The longest option or parameter name.
   * @param help The help lines.
   * @return The formatted line.
   */
  private String format(String opName, int maxOptionLength, List<String> help) {
    return format(opName, maxOptionLength, help, Options.SPACE_LENGTH);
  }

  /**
   * Format a line indented using the given length.
   * 
   * @param opName The long or short name of the option.
   * @param maxOptionLength The longest option or parameter name.
   * @param help The help lines.
   * @param indent The number of spaces at which to indent the given name.
   * @return The formatted line.
   */
  private String format(String opName, int maxOptionLength, List<String> help, int indent) {
    StringBuilder buf = new StringBuilder();

    buf.append(" ".repeat(maxOptionLength + Options.SPACE_LENGTH * 2));

    if(Objects.nonNull(opName) && !opName.isBlank()) {
      buf.replace(indent, indent + opName.length(), opName);
    }

    if(!help.isEmpty()) {
      buf.append(help.remove(0));
    }

    return buf.toString();
  }

}
