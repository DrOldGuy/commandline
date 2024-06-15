// Copyright (c) 2021 Goosebump Designs
package com.goosebumpdesigns.cmdline.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import com.goosebumpdesigns.cmdline.CmdLine;
import com.goosebumpdesigns.cmdline.exception.CmdLineException;
import com.goosebumpdesigns.cmdline.model.Option;
import com.goosebumpdesigns.cmdline.model.Options;
import com.goosebumpdesigns.cmdline.model.Parameter;

/**
 * This is a convenience service that creates a {@link CmdLine} object and then calls the
 * {@link CmdLine#parse(String...)} method. It can be autowired by Spring.
 * 
 * @author Rob
 *
 */
@Service
public class CmdLineService {
  private CmdLine cmdLine;

  /**
   * Creates a new {@link CmdLine} object and calls the {@link CmdLine#parse(String...)} method.
   * Either the CommandLine constructor or the parse method may throw a {@link CmdLineException}.
   * 
   * @param options The options to load (includes {@link Option}s and {@link Parameter}s).
   * @param args The command-line arguments supplied by the user.
   * @return A {@link CmdLine} object.
   */
  public CmdLine parse(Options options, String... args) {
    return cmdLine().options(options).parse(args);
  }

  /**
   * Print the instructions
   * 
   * @param jarName
   * @param options
   */
  public void printInstructions(String jarName, Options options) {
    cmdLine().printer().formatHelp(jarName, options);
  }
  
  /**
   * 
   * @return
   */
  private CmdLine cmdLine() {
    if(Objects.isNull(cmdLine)) {
      cmdLine = new CmdLine();
    }

    return cmdLine;
  }
}
