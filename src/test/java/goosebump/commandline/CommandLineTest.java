// Copyright (c) 2021 Goosebump Designs
package goosebump.commandline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.goosebumpdesigns.cmdline.CmdLine;
import com.goosebumpdesigns.cmdline.HelpFormatter;
import com.goosebumpdesigns.cmdline.exception.CmdLineException;
import com.goosebumpdesigns.cmdline.model.Option;
import com.goosebumpdesigns.cmdline.model.Options;
import com.goosebumpdesigns.cmdline.model.Parameter;
import com.goosebumpdesigns.cmdline.service.CmdLineService;

/**
 * @author Rob
 *
 */
class CommandLineTest {
  private static final String DIR_OP_DESC =
      "Enter the directory in which to search. (Default is current directory.)";
  private static final String DIR_LONG_OP = "directory";
  private static final Character DIR_OP = 'd';

  private static final String CLASS_OP_DESC =
      "Enter the class to search for without the .class extension. This is a "
          + "glob that can be wildcarded with * or ?. If you include the package name, it will be used to narrow the search."
          + " The package name can contain wildcards as well.";
  private static final String CLASS_LONG_OP = "class";
  private static final Character CLASS_OP = 'c';

  private static final String HELP_OP_DESC = "Print these instructions.";
  private static final String HELP_LONG_OP = "help";
  private static final Character HELP_OP = '?';

  private static final String INST =
      "This utility searches JAR files looking for a class. If the class is found, the "
          + "JAR file containing the class is printed, along with its path. You can specify a path to start the search. "
          + "Directories are searched recursively.";

  private static final String P1_LONG_OP = "subdirectory";
  private static final String P1_DESC =
      "This is the directory that is searched for the class. If the directory does "
          + "not exist it is created. If it does exist it is deleted.";

  private static final String P2_LONG_OP = "cantaloupe";
  private static final String P2_DESC = "If I can't elope what's the use of getting married?";

  private CmdLineService service;

  @BeforeEach
  void setup() {
    service = new CmdLineService();
  }

  /**
   * 
   */
  @Test
  void assertThatLongOptionWithFollowingParameterIsParsedCorrectly() {
    // Given: some command line arguments
    String[] args = {"--name", "Rob"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(true)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("name")).isTrue();
    assertThat(cmd.getValue("name").get()).isEqualTo("Rob");
  }

  /**
   * 
   */
  @Test
  void assertThatLongOptionWithNoFollowingParameterThrowsException() {
    // Given: a command-line argument
    String[] args = {"--name"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(true)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatLongOptionWithFollowingOptionThrowsException() {
    // Given: a command-line argument
    String[] args = {"--name", "--Rob"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(true)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  @Test
  void assertThatShortOptionfollowedByValueIsParsedCorrectly() {
    // Given: some command line arguments
    String[] args = {"-n", "Rob"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(true)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("name")).isTrue();
    assertThat(cmd.getValue("name").get()).isEqualTo("Rob");
    assertThat(cmd.hasValue('n')).isTrue();
    assertThat(cmd.getValue('n').get()).isEqualTo("Rob");
  }

  /**
   * 
   */
  @Test
  void assertThatShortOptionFollowedByShortOptionIsCorrect() {
    // Given: some command line arguments and some options
    String[] args = {"-ab"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("airplane")
            .multiple(false)
            .followedByValue(false)
            .required(true)
            .build())
        .addOption(Option.builder()
            .name("building")
            .multiple(false)
            .followedByValue(false)
            .required(true)
            .build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("airplane")).isTrue();
    assertThat(cmd.hasValue("building")).isTrue();
    assertThat(cmd.hasValue('a')).isTrue();
    assertThat(cmd.hasValue('b')).isTrue();
  }

  /**
   * 
   */
  @Test
  void assertThatShortOptionWithParamFollowedByAnotherOptionThrowsException() {
    // Given: some command line arguments and some options
    String[] args = {"-ab", "bonzo"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("airplane")
            .followedByValue(true)
            .required(true)
            .build())
        .addOption(Option.builder()
            .name("building")
            .followedByValue(false)
            .required(true)
            .build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatShortOptionWithParamFollowedAtEndOfListIsOK() {
    // Given: some command line arguments and some options
    String[] args = {"-ab", "bonzo"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("airplane")
            .followedByValue(false)
            .required(true)
            .build())
        .addOption(Option.builder()
            .name("building")
            .followedByValue(true)
            .required(true)
            .build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the line is parsed correctly
    assertThat(cmd.hasValue("airplane")).isTrue();
    assertThat(cmd.hasValue("building")).isTrue();
    assertThat(cmd.getValue("building").get()).isEqualTo("bonzo");

    assertThat(cmd.hasValue('a')).isTrue();
    assertThat(cmd.hasValue('b')).isTrue();
    assertThat(cmd.getValue('b').get()).isEqualTo("bonzo");
  }

  /**
   * 
   */
  @Test
  void assertThatOptionThatRequiresValueButDoesNotHaveItThrowsException() {
    // Given: some command line arguments and some options
    String[] args = {"--name"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .followedByValue(true)
            .build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatShortOptionThatRequiresValueButDoesNotHaveItThrowsException() {
    // Given: some command line arguments and some options
    String[] args = {"-n"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .followedByValue(true)
            .build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  @Test
  void assertThatMissingRequiredOptionThrowsException() {
    // Given: an empty argument string
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .followedByValue(true)
            .required(true)
            .build());
    // @formatter:on

    // When: the command line is parsed an exception is thrown.
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  @Test
  void assertThatHelpHelps() {
    int max = 70;

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name(CLASS_LONG_OP)
            .shortName(CLASS_OP)
            .followedByValue(true)
            .help(CLASS_OP_DESC)
            .required(true)
            .build())
        .addOption(Option.builder()
            .name(DIR_LONG_OP)
            .shortName(DIR_OP)
            .followedByValue(true)
            .help(DIR_OP_DESC)
            .build())
        .addOption(Option.builder()
            .name(HELP_LONG_OP)
            .shortName(HELP_OP)
            .help(HELP_OP_DESC)
            .build())
        .addParameter(Parameter.builder()
            .name(P1_LONG_OP)
            .help(P1_DESC)
            .build())
        .addParameter(Parameter.builder()
            .name(P2_LONG_OP)
            .help(P2_DESC)
            .build())
        .addDescription(INST);
    // @formatter:on

    String content = new HelpFormatter().maxWidth(max).formatHelp("commandline", options);
    
    System.out.println(content);

    assertThat(content).contains("--" + CLASS_LONG_OP);
    assertThat(content).contains("--" + DIR_LONG_OP);
    assertThat(content).contains("--" + HELP_LONG_OP);

    List<String> lines = Arrays.asList(content.replace("\r\n", "\n").split("\\n"));

    for(String line : lines) {
      if(line.length() > max) {
        fail("Line '" + line + "' is more than " + max + " characters.");
      }
    }
  }

  /**
   * 
   */
  @Test
  void assertThatOptionInOptionListThrowsException() {
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("duplicate")
            .build())
        .addOption(Option.builder().name("duplicate").build());
    // @formatter:on

    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatOptionInParameterListThrowsException() {
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addParameter(Parameter.builder()
            .name("duplicate")
            .build())
        .addOption(Option.builder()
            .name("duplicate")
            .build());
    // @formatter:on

    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatParameterInParameterListThrowsException() {
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addParameter(Parameter.builder()
            .name("duplicate")
            .build())
        .addParameter(Parameter.builder()
            .name("duplicate")
            .build());
    // @formatter:on

    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatParameterInOptionListThrowsException() {
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("duplicate")
            .build())
        .addParameter(Parameter.builder()
            .name("duplicate")
            .build());
    // @formatter:on

    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  @Test
  void assertThatLongOptionWithNoFollowingValueHasCorrectValue() {
    // Given: some command line arguments
    String[] args = {"--name"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(false)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("name")).isTrue();
    assertThat(cmd.getValue("name").get()).isEqualTo("name");
  }

  @Test
  void assertThatShortOptionWithNoFollowingValueHasCorrectValue() {
    // Given: some command line arguments
    String[] args = {"-n"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(false)
            .followedByValue(false)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("name")).isTrue();
    assertThat(cmd.getValue("name").get()).isEqualTo("n");
  }

  @Test
  void assertThatMultipleValuesAreRead() {
    // Given: some command line arguments
    String[] args = {"--name", "Rob", "--name", "Phil"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .multiple(true)
            .followedByValue(true)
            .required(true).build());
    // @formatter:on

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: the command line is correct
    assertThat(cmd.hasValue("name")).isTrue();
    assertThat(cmd.getValues("name")).isEqualTo(List.of("Rob", "Phil"));
  }

  @Test
  void assertThatParametersWithoutNamesAreReadCorrectly() {
    // Given: some command line arguments
    String[] args = {"Big", "Bad", "Bug"};
    Options options = new Options();

    // When: the command line is parsed
    CmdLine cmd = service.parse(options, args);

    // Then: parameters are read correctly
    assertThat(cmd.getParameterValues()).isEqualTo(List.of("Big", "Bad", "Bug"));
  }

  @Test
  void assertThatParameterFoundBeforeOptionThrowsException() {
    // Given: some command line arguments
    String[] args = {"Rob", "-name"};

    // @formatter:off
    Options options = new Options()
        .addOption(Option.builder()
            .name("name")
            .build());
    // @formatter:on

    // When: the command line is parsed
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatRequiredParameterAfterOtherParameterThrowsException() {
    // Given: some command line arguments
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addParameter(Parameter.builder()
            .name("parameter-1")
            .required(false)
            .build())
        .addParameter(Parameter.builder()
            .name("parameter-2")
            .required(true)
            .build());
    // @formatter:on

    // When: the parameter is added to the options an exception is thrown.
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  /**
   * 
   */
  @Test
  void assertThatMissingRequiredParameterThrowsException() {
    // Given: some command line arguments
    String[] args = {};

    // @formatter:off
    Options options = new Options()
        .addParameter(Parameter.builder()
            .name("parameter-1")
            .required(true)
            .build());
    
    // When: the command line is parsed an exception is thrown
    assertThatThrownBy(() -> service.parse(options, args)).isInstanceOf(CmdLineException.class);
  }

  @Test
  void assertThatSpringBootAnsiArgIsIgnored() {
    String[] args = {"--spring.output.ansi.enabled=always"};
    Options options = new Options();
    
    CmdLine cmd = service.parse(options, args);
    
    assertThat(cmd.getParameterValues()).isEqualTo(List.of());
  }
}
