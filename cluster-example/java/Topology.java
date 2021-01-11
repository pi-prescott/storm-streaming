package piprescott;

import backtype.storm.Config; 
import backtype.storm.LocalCluster; 
import backtype.storm.StormSubmitter;
import backtype.storm.task.OutputCollector; 
import backtype.storm.task.TopologyContext; 
import backtype.storm.testing.TestWordSpout; 
import backtype.storm.topology.OutputFieldsDeclarer; 
import backtype.storm.topology.TopologyBuilder; 
import backtype.storm.topology.base.BaseRichBolt; 
import backtype.storm.tuple.Fields; 
import backtype.storm.tuple.Tuple; 
import backtype.storm.tuple.Values; 
import backtype.storm.utils.Utils; 
import java.util.Map; 

public class Topology { 
	public static class ExclamationBolt extends BaseRichBolt { 
		OutputCollector _collector; 
		
		@Override 
		public void prepare(Map conf, TopologyContext context, OutputCollector collector) { 
			_collector = collector; 
		} 

		@Override 
		public void execute(Tuple tuple) { 
			String s = new String(tuple.getString(0) + "!!!");
			_collector.emit(tuple, new Values(s)); 
			_collector.ack(tuple); 

			System.out.println( s );
		} 
		
		@Override 
		public void declareOutputFields(OutputFieldsDeclarer declarer) { 
			declarer.declare(new Fields("word")); 
		} 
	} 
	
	public static void main(String[] args) throws Exception { 
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("word", new TestWordSpout()); 
		builder.setBolt("exclaim1", new ExclamationBolt()).shuffleGrouping("word"); 
		builder.setBolt("exclaim2", new ExclamationBolt()).shuffleGrouping("exclaim1"); 
		
		Config conf = new Config(); 
		conf.setNumWorkers(20);
		conf.setMaxSpoutPending(5000);
		StormSubmitter.submitTopology("mytopology", conf, builder.createTopology());
	} 
}
