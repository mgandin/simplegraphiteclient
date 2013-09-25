package com.zanox.lib.simplegraphiteclient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple client for writing one shot metrics to graphite.
 * No socket reuse or other optimisations are implemented. 
 * 
 * @author Helmut Zechmann 
 */
public class SimpleGraphiteClient {

	private final String graphiteHost;
	private final int graphitePort;
	
	/**
	 * Create a new Graphite client.
	 * 
	 * @param graphiteHost Host name to write to.
	 * @param graphitePort Graphite socket. Default is 2003
	 */
	public SimpleGraphiteClient(String graphiteHost, int graphitePort) {
		this.graphiteHost = graphiteHost;
		this.graphitePort = graphitePort;
	}

	/**
	 * Send a set of metrics with the current time as timestamp to graphite.
	 *  
	 * @param metrics the metrics as key-value-pairs
	 */
	public void sendMetrics(Map<String, Integer> metrics) {
		sendMetrics(metrics, getCurrentTimestamp());
	}

	/**
	 * Send a set of metrics with a given timestamp to graphite.
	 *  
	 * @param metrics the metrics as key-value-pairs
	 * @param timeStamp the timestamp
	 */
	public void sendMetrics(Map<String, Integer> metrics, long timeStamp) {
		try {
			Socket socket = createSocket();
			OutputStream s = socket.getOutputStream();
			PrintWriter out = new PrintWriter(s, true);
			for (Map.Entry<String, Integer> metric: metrics.entrySet()) {
				out.printf("%s %d %d%n", metric.getKey(), metric.getValue(), timeStamp);	
			}			
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			throw new GraphiteException("Unknown host: " + graphiteHost);
		} catch (IOException e) {
			throw new GraphiteException("Error while writing data to graphite: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Send a set of metrics with a given timestamp to graphite.
	 *  
	 * @param metrics the metrics as key-value-pairs
	 * @param timeStamp the timestamp
	 */
	public void sendMetricsPrecise(Map<String, Double> metrics, long timeStamp) {
		try {
			Socket socket = createSocket();
			OutputStream s = socket.getOutputStream();
			PrintWriter out = new PrintWriter(s, true);
			for (Map.Entry<String, Double> metric: metrics.entrySet()) {
				out.printf("%s %e %d%n", metric.getKey(), metric.getValue(), timeStamp);	
			}			
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			throw new GraphiteException("Unknown host: " + graphiteHost);
		} catch (IOException e) {
			throw new GraphiteException("Error while writing data to graphite: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Send a single metric with the current time as timestamp to graphite. 
	 * 
	 * @param key The metric key
	 * @param value the metric value
	 * 
	 * @throws GraphiteException if writing to graphite fails
	 */
	public void sendMetric(String key, int value) {
		sendMetric(key, value, getCurrentTimestamp());
	}
	
	public void sendMetric(String key, double value) {
		sendMetric(key, value, getCurrentTimestamp());
	}
	
	/**
	 * Send a single metric with a given timestamp to graphite.
	 * 
	 * @param key The metric key
	 * @param value The metric value
	 * @param timeStamp the timestamp to use
	 * 
	 * @throws GraphiteException if writing to graphite fails 
	 */
	@SuppressWarnings("serial")
	public void sendMetric(final String key, final int value, long timeStamp) {		
		sendMetrics(new HashMap<String, Integer>() {{
			put(key, value);
		}}, timeStamp);
	}
	
	@SuppressWarnings("serial")
	public void sendMetric(final String key, final double value, long timeStamp) {		
		sendMetricsPrecise(new HashMap<String, Double>() {{
			put(key, value);
		}}, timeStamp);
	}
	
	protected Socket createSocket() throws UnknownHostException, IOException {
		return new Socket(graphiteHost, graphitePort);
	}
	 
	/***
	 * Compute the current graphite timestamp.
	 * 
	 * @return Seconds passed since 1.1.1970
	 */
	protected long getCurrentTimestamp() {
		return System.currentTimeMillis() / 1000;
	}
}
