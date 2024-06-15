// Copyright (c) 2021 Goosebump Designs
package com.goosebumpdesigns.cmdline.exception;

/**
 * This unchecked exception class is used by classes in the goosebump.commandline package.
 * 
 * @author Rob
 *
 */
@SuppressWarnings("serial")
public class CmdLineException extends RuntimeException {

  /**
   * Creates an exception with a message.
   * 
   * @param message The message.
   */
  public CmdLineException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a cause.
   * 
   * @param cause The cause.
   */
  public CmdLineException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates an exception with both a message and a cause.
   * 
   * @param message The message.
   * @param cause The cause.
   */
  public CmdLineException(String message, Throwable cause) {
    super(message, cause);
  }
}
