// Copyright (c) 2021 Goosebump Designs

package com.goosebumpdesigns.cmdline;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import com.goosebumpdesigns.cmdline.exception.CmdLineException;
import com.goosebumpdesigns.cmdline.model.Option;
import com.goosebumpdesigns.cmdline.model.Options;
import com.goosebumpdesigns.cmdline.model.Parameter;
import lombok.Getter;

/**
 * This class parses, validates, and reports on command line options. Use the {@link Options} class
 * to add {@link Option}s and {@link Parameter}s. Options are unordered. Options must have a long
 * name and may have a short name. Long names in the command line arguments are indicated by
 * --option-name. Short names are single characters and are indicated by a single dash followed by
 * the short name (i.e., "-s").
 * <p>
 * Parameters are ordered values and must follow all options on the command line.
 * <p>
 * Both options and parameters have builders.
 * <p>
 * Help can be printed using the {@link #printInstructions(String, String)} convenience method or
 * using {@link FormatPrinter#print(String, String, Options).
 * <p>
 * A convenience service is provided that creates a new {@link CmdLine} object and calls the
 * {@link CmdLine#parse(String...)} method. This service is eligible for autowiring (injecting) by
 * Spring.
 * <p>
 * The {@link CmdLine#parse(String...)} method can throw a {@link CmdLineException} if something is
 * not correct.
 * 
 * @author Rob
 *
 */
public class CmdLine {
  private static final String SPRING_OUTPUT_ANSI_ENABLED = "spring.output.ansi.enabled";

  private Options options;

  @Getter
  private List<String> parameterValues = new LinkedList<>();

  private enum ArgType {
    LONG, SHORT, PARM
  }

  @Autowired
  private HelpFormatter printer;

  /**
   * Populate with the list of {@link Option}s.
   * 
   * @param options The options to use. This must not be {@code null}.
   * @throws NullPointerException Thrown if <em>options</em> is {@code null}.
   */
  public CmdLine options(Options options) {
    this.options = Objects.requireNonNull(options);
    return this;
  }

  /**
   * Returns the printer used by the command line parser.
   * 
   * @return The printer.
   */
  public HelpFormatter printer() {
    if(Objects.isNull(printer)) {
      printer = new HelpFormatter();
    }

    return printer;
  }

  /**
   * Validates the options and parses the command line.
   * 
   * @param arguments The command line arguments.
   * @return The {@link CmdLine} object.
   * @throws CmdLineException Thrown if one of the options or parameters are invalid or if the
   *         command-line arguments are invalid.
   */
  @SuppressWarnings("java:S127") // Don't update index within loop body
  public CmdLine parse(String... arguments) {
    options.validate();

    List<String> args = convertArguments(arguments);

    for(int index = 0; index < args.size(); index++) {
      String arg = args.get(index);
      ArgType argType;

      if(arg.startsWith("--")) {
        arg = arg.substring(2);
        argType = ArgType.LONG;
      }
      else if(arg.startsWith("-") || arg.startsWith("/")) {
        arg = arg.substring(1);
        argType = ArgType.SHORT;
      }
      else {
        argType = ArgType.PARM;
      }

      switch(argType) {
        case LONG:
          index = processLongArg(index, arg, args);
          break;

        case PARM:
          parameterValues.add(arg);
          break;

        case SHORT:
          index = processShortArg(index, arg, args);
          break;

        default:
          throw new CmdLineException("Unhandled argument type: " + argType);
      }
    }

    checkRequiredArguments();

    return this;
  }

  /**
   * @param arguments
   * @return
   */
  private List<String> convertArguments(String[] arguments) {
    List<String> args = new LinkedList<>();

    if(Objects.nonNull(arguments)) {
      for(int index = 0; index < arguments.length; index++) {
        String arg = arguments[index];

        if(arg.startsWith("-")) {
          int pos = arg.indexOf('=');

          if(pos != -1) {
            args.add(arg.substring(0, pos).trim());
            args.add(arg.substring(pos + 1).trim());
          }
          else {
            args.add(arg);
          }
        }
        else {
          args.add(arg);
        }
      }
    }

    return args;
  }

  /**
   * Check that all required arguments are present in the command-line arguments.
   */
  private void checkRequiredArguments() {
    for(Option option : options.getNameMap().values()) {
      if(option.isRequired() && option.getValues().isEmpty()) {
        throw new CmdLineException("Required option " + option.getName() + " is missing.");
      }
    }

    List<Parameter> parameters = options.getParameters();

    for(int index = 0; index < parameters.size(); index++) {
      Parameter parameter = parameters.get(index);

      if(parameter.isRequired() && index >= parameterValues.size()) {
        throw new CmdLineException("Required parameter " + parameter.getName() + " is missing.");
      }
    }
  }

  /**
   * Process the short command-line argument (-short).
   * 
   * @param index The argument index. If the option is followed by a value, this number is
   *        incremented and returned so that the caller will skip over the value.
   * @param arg The command-line argument
   * @param args The argument list
   * @return The new index value.
   */
  private int processShortArg(int index, String arg, List<String> args) {
    throwExceptionIfParameterExists(arg);

    while(!arg.isEmpty()) {
      Character shortArg = arg.charAt(0);

      arg = arg.substring(1);

      Option op = options.getShortNameMap().get(shortArg);

      if(Objects.isNull(op)) {
        throw new CmdLineException("Short option -" + arg + " not found!");
      }

      if(arg.length() > 0 && op.isFollowedByValue()) {
        throw new CmdLineException("Short option '" + shortArg
            + "' is followed by short option(s) '" + arg + "' but option '" + shortArg
            + "' requires a following parameter. It must be last in the short argument list.");
      }

      if(op.isFollowedByValue()) {
        if(args.size() <= index + 1) {
          throw new CmdLineException(
              "Required option -" + shortArg + " must have a value but it does not!");
        }

        index = extractFollowingValue(index, op, args);
      }
      else {
        op.addValue(shortArg.toString());
      }
    }

    return index;
  }

  /**
   * Extract the value following the option and add it to the option values list.
   * 
   * @param index The index to check.
   * @param op The option on which to add the value.
   * @param args The command-line arguments list.
   * @return The new index if successful.
   * @throws CmdLineException Thrown if a following value is not found.
   */
  private int extractFollowingValue(int index, Option op, List<String> args) {
    String value = args.get(++index);

    if(value.startsWith("-") || value.startsWith("?")) {
      throw new CmdLineException("Option " + op.getName() + " is not followed by a value!");
    }

    op.addValue(value);
    return index;
  }

  /**
   * Process the long (--long) argument.
   * 
   * @param index The current argument index.
   * @param arg The argument.
   * @param args The list of command-line arguments.
   * @return index + 1 if the option is followed by a value.
   */
  private int processLongArg(int index, String arg, List<String> args) {
    if(SPRING_OUTPUT_ANSI_ENABLED.equals(arg)) {
      return index + 1;
    }

    throwExceptionIfParameterExists(arg);

    Option op = options.getNameMap().get(arg);

    if(Objects.isNull(op)) {
      throw new CmdLineException("Unknown option --" + arg);
    }
    else if(op.isFollowedByValue()) {
      if(args.size() <= index + 1) {
        throw new CmdLineException("Required option --" + op.getName() + " does not have a value!");
      }

      index = extractFollowingValue(index, op, args);
    }
    else {
      op.addValue(arg);
    }

    return index;
  }

  /**
   * Throws an exception if any parameter value exists. This means that an option was found after a
   * parameter, which is illegal.
   * 
   * @param arg The argument to check.
   * @throws CmdLineException Thrown if a parameter value has been found.
   */
  private void throwExceptionIfParameterExists(String arg) {
    if(!parameterValues.isEmpty()) {
      throw new CmdLineException("Option " + arg
          + " found after reading a parameter. All options must come before any parameters.");
    }
  }

  /**
   * Returns {@code true} if the option or parameter with the given long name has a value.
   * 
   * @param optionName The option long name or parameter name.
   * @return {@code true} if a value exists.
   */
  public boolean hasValue(String optionName) {
    return getValue(optionName).isPresent();
  }

  /**
   * Returns {@code true} if the option with the given short name has a value.
   * 
   * @param shortName The option short name.
   * @return {@code true} if a value exists.
   */
  public boolean hasValue(Character shortName) {
    Option option = findOption(shortName);

    return !option.getValues().isEmpty();
  }

  /**
   * Returns the option value as an {@link Optional} object given the short name.
   * 
   * @param shortName The option short name.
   * @return The value as an Optional, or an empty Optional.
   */
  public Optional<String> getValue(Character shortName) {
    Option option = findOption(shortName);

    if(option.getValues().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(option.getValues().get(0));
  }

  /**
   * Returns the option value as an {@link Optional} object given the long name. This also searches
   * parameter values if a match can be found on the parameter name.
   * 
   * @param name The option long name.
   * @return The value as an Optional, or an empty Optional.
   * @throws CmdLineException Thrown if the option or parameter name is not found.
   */
  public Optional<String> getValue(String name) {
    Option option = options.getNameMap().get(name);

    if(Objects.nonNull(option)) {
      if(!option.getValues().isEmpty()) {
        return Optional.of(option.getValues().get(0));
      }

      return Optional.empty();
    }

    boolean parameterFound = false;

    for(int index = 0; index < options.getParameters().size(); index++) {
      Parameter parameter = options.getParameters().get(index);

      if(parameter.getName().equals(name)) {
        parameterFound = true;

        if(index < parameterValues.size()) {
          return Optional.of(parameterValues.get(index));
        }
      }
    }

    if(!parameterFound) {
      throw new CmdLineException("Option or parameter with name '" + name + "' is not found.");
    }

    return Optional.empty();
  }

  /**
   * Returns a list of values for the given option short name. The list may be empty but it will not
   * be null.
   * 
   * @param shortName The option short name.
   * @return A list of options.
   * @throws CmdLineException Thrown if the option is not found.
   */
  public List<String> getValues(Character shortName) {
    return findOption(shortName).getValues();
  }

  /**
   * Returns a list of values for the given option long name or parameter name. The list may be
   * empty but it will not be {@code null}.
   * 
   * @param name The option long name or parameter name.
   * @return A list of options.
   * @throws CmdLineException Thrown if the option or parameter name is not found.
   */
  public List<String> getValues(String name) {
    Option option = options.getNameMap().get(name);

    if(Objects.nonNull(option)) {
      return option.getValues();
    }

    boolean parameterFound = false;

    for(int index = 0; index < options.getParameters().size(); index++) {
      Parameter parameter = options.getParameters().get(index);

      if(parameter.getName().equals(name)) {
        parameterFound = true;

        if(index < parameterValues.size()) {
          return List.of(parameterValues.get(index));
        }

        break;
      }
    }

    if(parameterFound) {
      return List.of();
    }

    throw new CmdLineException("Option or parameter with name '" + name + "' was not found.");
  }

  /**
   * Returns the option with the given short name if it is found.
   * 
   * @param shortName The option short name.
   * @return The option matching the short name.
   * @throws CmdLineException Thrown if the option isn't found.
   */
  private Option findOption(Character shortName) {
    Option option = options.getShortNameMap().get(shortName);

    if(Objects.isNull(option)) {
      throw new CmdLineException("-" + shortName + " is not a valid option.");
    }

    return option;
  }

  /**
   * Print the instructions. This is a convenience method for
   * {@link CmdLineHelp#printInstructions(String, String, Options)}.
   * 
   * @param jarFileName The name of the JAR file. If this is {@code null}, no usage instructions are
   *        printed.
   */
  public void printInstructions(String jarFileName) {
    printer().formatHelp(jarFileName, options);
  }
}
