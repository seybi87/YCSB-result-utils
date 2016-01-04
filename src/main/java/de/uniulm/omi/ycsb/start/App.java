package de.uniulm.omi.ycsb.start;

import de.uniulm.omi.ycsb.config.CommandLinePropertiesAccessor;
import de.uniulm.omi.ycsb.config.CommandLinePropertiesAccessorImpl;
import de.uniulm.omi.ycsb.result.ResultType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;


/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);


    public static void main (String args[]){

        LOGGER.debug(Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.*", "2015-12-28 16:44:44:856 10 sec: 6645 operations; 664.43 current ops/sec; es"));
        // Process config
        LOGGER.info("Process config...");
        CommandLinePropertiesAccessor config = new CommandLinePropertiesAccessorImpl(args);

        ResultType resultType = config.getResultType();

        LOGGER.debug(config.getInputPath());

        switch (resultType){
            case HISTOGRAM:
                LOGGER.info("Historgram: not yet implemented");
                break;
            case TIMESERIES:
                processTimeseriesData(config);
                break;
        }
    }

    public static void processTimeseriesData(CommandLinePropertiesAccessor config){
        LOGGER.info("starting to process TIMESERIES data...");
        TimeseriesWorker timeseriesWorker = new TimeseriesWorker(config.getInputPath(), config.getMergedPath(), config.getOutputPath());
        timeseriesWorker.mergeClients();
    }
}
