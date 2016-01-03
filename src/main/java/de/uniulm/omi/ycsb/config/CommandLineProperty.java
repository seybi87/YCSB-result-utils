package de.uniulm.omi.ycsb.config;

import de.uniulm.omi.ycsb.result.ResultType;

/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public class CommandLineProperty {

    public static CommandLineProperty[] commandLineProperties = {
            new CommandLineProperty("ResultType", "ResultType", "Specify the YCSB result: timeseries or histogram", ResultType.TIMESERIES.toString())
    };

    private final String name;
    private final String longOpt;
    private final String desc;
    private final String defaultValue;

    private CommandLineProperty(String name, String longOpt, String desc, String defaultValue) {
        this.name = name;
        this.longOpt = longOpt;
        this.desc = desc;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public String getDesc() {
        return desc;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
