package de.uniulm.omi.ycsb.result;

/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public enum ResultType {

    TIMESERIES("TIMESERIES"),
    HISTOGRAM("HISTOGRAM");

    private final String resultText;


    private ResultType(final String resultText){
        this.resultText = resultText;
    }

    @Override public String toString() {
        return resultText;
    }
}
