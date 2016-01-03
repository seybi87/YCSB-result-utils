package de.uniulm.omi.ycsb.start;

import de.uniulm.omi.ycsb.config.CommandLinePropertiesAccessor;
import de.uniulm.omi.ycsb.config.CommandLinePropertiesAccessorImpl;
import de.uniulm.omi.ycsb.result.ResultType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.uniulm.omi.ycsb.result.ResultType.HISTOGRAM;


/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);


    public static void main (String args[]){


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
                LOGGER.info("Timeseries: not yet implemented");
                break;
        }
    }
}
