package com.telecom.fraud.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Driver class to configure and run the Hadoop MapReduce Telecom Fraud Detection Job.
 */
public class FraudDetectionDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: FraudDetectionDriver <input_path> <output_path> [opts...]");
            return -1;
        }

        Configuration conf = getConf();
        if (conf == null) {
            conf = new Configuration();
        }

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        FileSystem fs = outputPath.getFileSystem(conf);
        if (fs.exists(outputPath)) {
            System.out.println("Output directory " + outputPath + " already exists. Removing it...");
            fs.delete(outputPath, true);
        }

        Job job = Job.getInstance(conf, "TELECOM - Fraud Detection MapReduce Analysis");
        job.setJarByClass(FraudDetectionDriver.class);

        // Mapper & Reducer setup
        job.setMapperClass(CDRFraudMapper.class);
        job.setReducerClass(CDRFraudReducer.class);

        // Map output key/value
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CDRRecordWritable.class);

        // Job output key/value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Input & Output formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Configuration(), new FraudDetectionDriver(), args);
        System.exit(exitCode);
    }
}
