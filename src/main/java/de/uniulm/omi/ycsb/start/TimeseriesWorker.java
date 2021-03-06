package de.uniulm.omi.ycsb.start;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by Daniel Seybold on 03.01.2016.
 */
public class TimeseriesWorker extends ResultWorker {

    private static final Logger LOGGER = LogManager.getLogger(TimeseriesWorker.class);

    private final String outputFile;
    private final String mergedFile;

    public TimeseriesWorker(String inputPath, String mergedPath, String outputPath) {
        super(inputPath, mergedPath, outputPath);

        this.mergedFile = this.mergedPath + System.getProperty("file.separator") + "merged_" + System.currentTimeMillis()+".txt";
        this.outputFile = this.outputPath + System.getProperty("file.separator") + "output_" + System.currentTimeMillis()+".txt";
    }

    /**
     * parses and merges multiple client files and creates one merged result file out of it
     * Format:
     * runtime inb sec , current ops per second
     */
    public void mergeClients(){

        PrintWriter pWriter = null;
        int numberOfFiles = this.getNumberOfInputFiles();

        String row = null;

        try{
            String [] filenames = new File(this.inputPath).list();
            Vector<BufferedReader> readers = new Vector<BufferedReader>();
            Vector<Vector<Integer>> clientDataVectors = new Vector<Vector<Integer>>();

            pWriter = new PrintWriter(new FileWriter(this.mergedFile), true);

            for(int i = 0; i<numberOfFiles; i++){
                //open file i
                LOGGER.debug("opening file: " + filenames[i]);
                readers.add(new BufferedReader(new FileReader(this.inputPath + System.getProperty("file.separator") + filenames[i])));
                clientDataVectors.add(new Vector<Integer>());
            }


            //collection of one row per client file
            Vector<String> rows = new Vector<String>();

            String output;


            //read file line by line, files have to have same length
            while(readers.get(0).ready()){

                //read one lines of all files and extract current ops per s
                for(int i=0; i<readers.size(); i++) {

                    row = readers.get(i).readLine();
                    rows.add(row);
                    //LOGGER.debug(row);
                }

                output = this.parseRows(rows);

                if(output != null){
                    pWriter.println(output);
                }

                //clear rows vector
                rows.clear();


            }

            pWriter.close();



        }catch (Exception e){
            LOGGER.error("Error row: " + row);
            LOGGER.error("An error occured during merging the timeseries data", e);
        }

    }

    private String parseRows(Vector<String> rows){

        String output;
        //check pattern only for one row because file must have the same structure
        if(Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.*", rows.get(0))){

            output = this.parseTimeseries(rows);
            return output;

        }else if(Pattern.matches("\\[OVERALL\\], RunTime.*",rows.get(0))){

            output = this.parseClientRuntimes(rows);
            return output;
        }else if(Pattern.matches("\\[OVERALL\\], Throughput.*",rows.get(0))){

            output = this.parseOverallThroughput(rows);
            return output;
        }


        return null;
    }

    private String parseTimeseries(Vector<String> rows){

        String output;
        double currentOps = 0;
        int runtime=0;
        String extractedCurrentOps;

            for (String row : rows) {

                String[] splittedRow = row.split(";");

                //check for first timeseries row with only 5 fields
                if(splittedRow.length == 5){
                    output = "Timeseries, current ops/s\n0,0";
                    return output;
                }


                //extract runtime
                String extractedRuntime = splittedRow[1];
                extractedRuntime = extractedRuntime.trim();

                if(rows.size() > 1 && runtime > 0 && (Integer.valueOf(extractedRuntime) != runtime)){
                    throw new RuntimeException("Unequal runtimes in files! Current runtime: " + runtime + " extracted runtime: " +  extractedRuntime);
                }
                runtime = Integer.valueOf(extractedRuntime);

                //check for first row
                if (runtime == 0) {
                    currentOps = 0;

                } else {
                    //extract measurements: 0=date, 1=runtime, 2=unit, 3=finished operations 4=current ops/s
                    extractedCurrentOps = splittedRow[4];
                    extractedCurrentOps = extractedCurrentOps.replace("current ops/sec", "");
                    extractedCurrentOps = extractedCurrentOps.trim();

                    currentOps += Double.valueOf(extractedCurrentOps);
                }

            }

            output = runtime + "," + currentOps;
            return output;

    }

    /**
     * Parses the runtimes on per client base
     * @param rows
     * @return
     */
    private String parseClientRuntimes(Vector<String> rows){
        String output = "\n";

        double runtime;
        int clientFileCounter = 1;

        for (String row : rows) {

            //extract measurements: 0=[OVERALL] tag, 1=Runtime(ms), 2=totaloperations
            String[] splittedRow = row.split(",");

            //check for runtime
            runtime = Double.valueOf(splittedRow[2].trim());
            output += "Client" + clientFileCounter + ": " + splittedRow[1] + ": " + runtime + "\n";

            clientFileCounter++;
        }


        return output;
    }

    /**
     * Parses the overall Throughput and sums it up
     * @param rows
     * @return
     */
    private String parseOverallThroughput(Vector<String> rows){
        String output = "";

        double totalOps = 0;
        String totalOpsLabel="";

        for (String row : rows) {

            //extract measurements: 0=[OVERALL] tag, 1=Runtime(ms), 2=totaloperations
            String[] splittedRow = row.split(",");

            //extract and sum up total Throughput(ops/sec)
            totalOps += Double.valueOf(splittedRow[2].trim());
            totalOpsLabel = splittedRow[1].trim();

        }

        output += totalOpsLabel + ": " + totalOps;

        return output;
    }



    /**
     * combines multiple result files together, e.g. merged client files
     */
    public void mergeData(){

        int numberOfFiles = this.getNumberOfInputFiles();
        LOGGER.info("processing " + numberOfFiles + " result files..");

        PrintWriter pWriter = null;
        int rowCounter = 0;

        try{
            String [] filenames = new File(this.inputPath).list();
            Vector<BufferedReader> readers = new Vector<BufferedReader>();

            for(int i = 0; i<numberOfFiles; i++){
                //open file i
                LOGGER.debug("opening file: " + filenames[i]);
                readers.add(new BufferedReader(new FileReader(filenames[i])));
            }

            pWriter = new PrintWriter(new FileWriter(this.outputFile), true);

            String output = "";
            int latencyAmout = 0;
            int  divisiorCounter = 0;
            int latencyAverage = 0;

            Vector<Double> measurements = new Vector<Double>();

            Vector<String> rows = new Vector<String>();

            double sum = 0;

            while (readers.size() > 0) {

                for(int i = 0; i<readers.size();i++){

                    String line = readers.get(i).readLine();

                    if(line != null){
                        rows.add(line);
                    }else{
                        readers.remove(i);
                    }


                }



                output = "";
                latencyAmout = 0;


                for(int i = 0; i<rows.size();i++){


                    System.out.println(rows.get(i));

                    String [] rowSplitted = rows.get(i) != null ? rows.get(i).split(";") : new String[0];

                    if(rowSplitted.length == 2){
                        latencyAmout += Integer.parseInt(rowSplitted[1].trim()) ;
                        divisiorCounter++;
                    }

                }

                if(divisiorCounter > 0 ){
                    latencyAverage = latencyAmout / divisiorCounter;
                    output = (rowCounter*2) + ", " +  latencyAverage;

                    measurements.add((double) latencyAverage);
                    sum += (double) latencyAverage;


                    pWriter.println(output);
                }



                divisiorCounter = 0;
                latencyAmout = 0;
                latencyAverage = 0;
                rowCounter++;

                rows.clear();

                //calculate standard deviation
                double value;

                double sumVariance = 0;

                //median
                double median = sum / measurements.size();
                output = "median, " + (int)median;
                pWriter.println(output);

                for(int i=0; i<measurements.size();i++){

                    value = measurements.get(i);

                    sumVariance += (value - median) * (value - median);

                }


                //Varianz
                double variance = (1.0d / (measurements.size() - 1.0d)) * sumVariance;

                output = "variance, " + (int)variance;
                pWriter.println(output);
                System.out.println((int)variance);
                //standard deviation
                double standardDeviation = Math.sqrt(variance);
                System.out.println((int)standardDeviation);


                output = "standardDeviation, " + (int)standardDeviation;
                pWriter.println(output);

                pWriter.close();


            }

        }catch (Exception e){
            LOGGER.error("An error occured during merging the timeseries data", e);
        }




    }
}
