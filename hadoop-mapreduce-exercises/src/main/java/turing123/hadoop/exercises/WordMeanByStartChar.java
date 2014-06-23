package turing123.hadoop.exercises;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordMeanByStartChar extends Configured implements Tool {

  private double mean = 0;

  private final static Text COUNT = new Text("count");
  private final static Text LENGTH = new Text("length");
  private final static LongWritable ONE = new LongWritable(1);

  /**
   * Maps words from line of text into 2 key-value pairs; one key-value pair for
   * counting the word, another for counting its length.
   */
  public static class WordMeanMapper extends
      Mapper<Object, Text, Text, MapWritable> {

    private LongWritable wordLen = new LongWritable();
    private MapWritable wordData = new MapWritable();
    private Text word = new Text();
    /**
     * Emits 2 key-value pairs for counting the word and its length. Outputs are
     * (Text, LongWritable).
     * 
     * @param value
     *          This will be a line of text coming in from our input file.
     */
    public void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        String string = itr.nextToken();
        char startChar = string.charAt(0);
        if ( (startChar >= 'a' && startChar <= 'z') || (startChar >= 'A' && startChar <= 'Z') ) {
          this.word.set(String.valueOf(startChar));
          this.wordLen.set(string.length());
          this.wordData.put(LENGTH, this.wordLen);
          this.wordData.put(COUNT, ONE);
          context.write(this.word, this.wordData);
        }        
      }
    }
  }

  /**
   * Performs integer summation of all the values for each key.
   */
  public static class WordMeanReducer extends
      Reducer<Text, MapWritable, Text, DoubleWritable> {

    private DoubleWritable mean = new DoubleWritable();

    /**
     * Sums all the individual values within the iterator and writes them to the
     * same key.
     * 
     * @param key
     *          This will be one of 2 constants: LENGTH_STR or COUNT_STR.
     * @param values
     *          This will be an iterator of all the values associated with that
     *          key.
     */
    public void reduce(Text key, Iterable<MapWritable> values, Context context)
        throws IOException, InterruptedException {

      long countSum = 0;
      long lengthSum = 0;
      for (MapWritable val : values) {
        long count = ((LongWritable) val.get(COUNT)).get();
        long length = ((LongWritable) val.get(LENGTH)).get();
        countSum += count;
        lengthSum += length * count;
      }
      this.mean.set((double) lengthSum/countSum);
      context.write(key, mean);
    }
  }

  public static void main(String[] args) throws Exception {
    ToolRunner.run(new Configuration(), new WordMeanByStartChar(), args);
  }

  public int run(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: wordmean <in> <out>");
      return 0;
    }

    Configuration conf = getConf();

    @SuppressWarnings("deprecation")
    Job job = new Job(conf, "my word mean");
    job.setJarByClass(WordMeanByStartChar.class);
    job.setMapperClass(WordMeanMapper.class);
    job.setReducerClass(WordMeanReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setMapOutputValueClass(MapWritable.class);
    job.setOutputValueClass(DoubleWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    Path outputpath = new Path(args[1]);
    FileOutputFormat.setOutputPath(job, outputpath);
    boolean result = job.waitForCompletion(true);

    return (result ? 0 : 1);
  }
}