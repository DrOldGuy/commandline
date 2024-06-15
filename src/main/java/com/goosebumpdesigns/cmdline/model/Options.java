// Copyright (c) 2021 Goosebump Designs
package com.goosebumpdesigns.cmdline.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import com.goosebumpdesigns.cmdline.CmdLine;
import com.goosebumpdesigns.cmdline.exception.CmdLineException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * This is the list of options. It may contain parameters and options. Both Options and Parameters
 * have names. When a new Parameter or Option is added, the options and parameter lists are checked
 * for duplicate names. For Options, both the name list and the short name lists are checked for
 * duplicated.
 * 
 * This class has {@link #add(Option)} methods for both Options and Parameters.
 * 
 * @author Rob
 *
 */
@ToString
@EqualsAndHashCode
public class Options {
  public static final int SPACE_LENGTH = 3;

  private Map<String, Option> nameMap = new TreeMap<>();
  private Map<Character, Option> shortNameMap = new HashMap<>();
  private Map<String, Parameter> parameters = new LinkedHashMap<>();

  /*
   * These are used to store the options as they are added so that options and parameters can be
   * added without throwing exceptions. The options and parameters are moved to the map by the
   * validate method. Note that validate() can be called repeatedly with no penalty.
   */
  private List<Option> addedOptions = new LinkedList<>();
  private List<Parameter> addedParameters = new LinkedList<>();

  /* This is the description for the application/utility */
  @Getter
  private String description;

  /**
   * Returns the name map.
   * 
   * @return The name map. This is a map of option name -> option. If you call
   *         {@link #getNameMap()}.values(), you will get the list of options supplied by the caller
   *         sorted by option name.
   */
  public Map<String, Option> getNameMap() {
    return Collections.unmodifiableMap(nameMap);
  }

  /**
   * Returns the short name map.
   * 
   * @return The short name map. This is a map of option short name -> option.
   */
  public Map<Character, Option> getShortNameMap() {
    return Collections.unmodifiableMap(shortNameMap);
  }

  /**
   * Add an option to the option list. The option list is sorted by option name. This uses a fluent
   * API that can be chained with other methods.
   * 
   * @param option The option to add.
   * @return This object.
   */
  public Options addOption(Option option) {
    addedOptions.add(option);
    return this;
  }

  /**
   * Validate and store the option. This allows the options to be added without the possibility of
   * having an exception thrown.
   * 
   * @param option The option to validate and store.
   * @throws CmdLineException Thrown if the option name is duplicated.
   */
  private void validateAndStore(Option option) {
    Objects.requireNonNull(option, "Option must not be null.");

    if(nameMap.containsKey(option.getName())) {
      throw new CmdLineException("Duplicate option " + option.getName());
    }

    if(parameters.containsKey(option.getName())) {
      throw new CmdLineException(
          "Option " + option.getName() + " is already in the parameter list.");
    }

    nameMap.put(option.getName(), option);

    if(Objects.nonNull(option.getShortName())) {
      if(shortNameMap.containsKey(option.getShortName())) {
        throw new CmdLineException("Duplicate option " + option.getShortName());
      }

      shortNameMap.put(option.getShortName(), option);
    }
  }

  /**
   * Validate the options and parameters during the parse operation. This allows options and
   * parameters to be added without the possibility of an exception being thrown.
   */
  public void validate() {
    while(!addedOptions.isEmpty()) {
      validateAndStore(addedOptions.remove(0));
    }

    while(!addedParameters.isEmpty()) {
      validateAndStore(addedParameters.remove(0));
    }
  }

  /**
   * Add a named parameter to the parameter list. Parameters always follow the options in the
   * arguments. Parameters have an order but no limit, which means that extra parameters that are
   * found on the command line are simply added onto the end of the parameter list. This method is
   * implemented using a fluent API so that methods can be chained together.
   * 
   * @param parameter The parameter to add.
   * @return This object.
   */
  public Options addParameter(Parameter parameter) {
    addedParameters.add(parameter);
    return this;
  }

  /**
   * Add the application description that is printed when
   * {@link CmdLine#printInstructions(String, String) CmdLine.printInstructions} is called.
   * 
   * @param description The description to add.
   * @return This object.
   */
  public Options addDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Validate and store the parameter. This is called during the parse operation, so there is no
   * possibility of having an exception thrown as the parameter is added.
   * 
   * @param parameter The parameter to validate.
   * @throws CmdLineException Thrown if a parameter name is duplicated.
   */
  private void validateAndStore(Parameter parameter) {
    Objects.requireNonNull(parameter, "Parameter must not be null.");

    if(parameters.containsKey(parameter.getName())) {
      throw new CmdLineException("Duplicate parameter " + parameter.getName());
    }

    if(nameMap.containsKey(parameter.getName())) {
      throw new CmdLineException(
          "Parameter " + parameter.getName() + " is already in the option list.");
    }

    if(parameter.isRequired()) {
      for(Parameter p : parameters.values()) {
        if(!p.isRequired()) {
          throw new CmdLineException(
              "Required parameters must come before non-required parameters. Parameter "
                  + parameter.getName() + " must come before " + p.getName() + ".");
        }
      }
    }

    parameters.put(parameter.getName(), parameter);
  }

  /**
   * This returns the length of the longest option name/parameter name given the current contents of
   * the lists. It is package scope to hide this method from non-package callers.
   * 
   * @return The length of the longest parameter or option name.
   */
  public int findMaxLength() {
    int max = 0;

    for(Option option : nameMap.values()) {
      max = Math.max(max, option.getName().length());
    }

    for(Parameter parameter : parameters.values()) {
      max = Math.max(max, parameter.getName().length());
    }

    return max;
  }

  /**
   * Return the parameters as a list. This is package scope to limit visibility to package methods.
   * There is a public method {@link CmdLine#getParameterValues()} that can be called externally.
   * 
   * @return The list of parameters.
   */
  public List<Parameter> getParameters() {
    validate();
    return parameters.values().stream().toList();
  }
}
