package de.uniulm.omi.ycsb.config;


import de.uniulm.omi.ycsb.result.ResultType;
import org.apache.commons.cli.*;

/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public class CommandLinePropertiesAccessorImpl implements CommandLinePropertiesAccessor {


    private final Options options;
    private CommandLine commandLine;
    private final static DefaultParser parser = new DefaultParser();
    private final static HelpFormatter helpFormatter = new HelpFormatter();

    public CommandLinePropertiesAccessorImpl(String[] args) {
        this.options = new Options();
        this.generateOptions(this.options);

        try {
            this.commandLine = this.parser.parse(options, args);
        } catch (ParseException e) {
            this.commandLine = null;
        }
    }

    private void generateOptions(Options options) {
        for(CommandLineProperty clp : CommandLineProperty.commandLineProperties){
            options.addOption(
                    Option.builder(clp.getName()).
                            longOpt(clp.getLongOpt()).
                            desc(clp.getDesc()).
                            hasArg()
                            .build());
        }
    }

    public void printHelp() {
        helpFormatter.printHelp("java -jar [args] ycsb-utils-jar-with-dependencies.jar", options);
    }


    protected String getCommandLineOption(String name) {
        if (commandLine != null && commandLine.hasOption(name)) {
            String result = commandLine.getOptionValue(name);
            if(result == null){
                return getDefaultValue(name);
            } else {
                return result;
            }
        } else {
            return getDefaultValue(name);
        }
    }

    private String getDefaultValue(String name) {
        for(CommandLineProperty clp : CommandLineProperty.commandLineProperties){
            if(clp.getName().equals(name)){
                return clp.getDefaultValue();
            }
        }

        return null;
    }


    @Override
    public ResultType getResultType() {
        return ResultType.valueOf(this.getCommandLineOption("ResultType"));
    }
}
