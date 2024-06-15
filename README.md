# Command line parsing library

This library provides classes and methods to validate and parse command-line arguments. You provide a template of options and parameters to the library. They are then compared to the actual command-line arguments. If there is a problem, an exception is thrown. Problems that can occur are:

* Duplicate option or parameter names
* Missing required option or parameter
* Unknown option
* Incorrect option or parameter positioning

## Usage instructions

To use, do something like the following:

```
@Autowired
private CommandLineService commandLineService;

private Options options;

@PostConstruct
public void init() {
  // @formatter:off
  options = new Options()
      .add(Option.builder()
          .name(CLASS_LONG_OP)
          .shortName(CLASS_OP)
          .followedByValue(true)
          .help(CLASS_OP_DESC)
          .build())
      .add(Parameter.builder()
          .name(DIR_LONG_OP)
          .required(true)
          .help(DIR_OP_DESC)
          .build())
      .add(Option.builder()
          .name(HELP_LONG_OP)
          .shortName(HELP_OP)
          .help(HELP_OP_DESC)
          .build());
  // @formatter:on
}

@Override
public void run(String... args) {
  try {
    if(cmd.hasOption(HELP_LONG_OP)) {
      printHelp();
    }
    else {
      // other methods
    }
  }
  catch(Exception e) {
    printHelp();
  }
}

private void printHelp() {
  String content = new HelpFormatter().maxWidth(max).formatHelp("myjar.jar", options);
  log.info(content);
}


```