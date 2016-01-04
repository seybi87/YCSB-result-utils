package de.uniulm.omi.ycsb.start;

import java.io.File;

/**
 * Created by Daniel Seybold on 03.01.2016.
 */
abstract class ResultWorker {

    protected final String inputPath;
    protected final String mergedPath;
    protected final String outputPath;

    public ResultWorker(String inputPath, String mergedPath, String outputPath){

        this.inputPath = inputPath;
        this.mergedPath = mergedPath;
        this.outputPath = outputPath;

    }

    protected int getNumberOfInputFiles(){

        return new File(inputPath).list().length;
    }

    protected int getNumberOfMergedFiles(){

        return new File(mergedPath).list().length;
    }
}
