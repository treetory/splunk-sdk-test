package com.treetory.test.mvc.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.splunk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.treetory.test.common.properties.SplunkProperties;
import com.treetory.test.common.util.splunk.SplunkClient;
import com.treetory.test.mvc.model.SplunkRequest;

@Service
public class SplunkServiceImpl implements SplunkService {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkServiceImpl.class);
	
	@Autowired
	private SplunkProperties sProperties;

	@Override
	public Object getLogByNormalSearch(SplunkRequest model) {
		
		SplunkClient sc = null;
		InputStream is = null;
		ResultsReader resultsReaderNormalSearch = null;
		
		try {
		
			sc = new SplunkClient();
			sc.connect(sProperties.getHostIp(), sProperties.getUsername(), sProperties.getPassword(), sProperties.getPort(), sProperties.getScheme());
			
			JobCollection jobs = sc.splunkService.getJobs();
			
			Job job = jobs.create(model.getQuery());
			
			// Wait until results are available.
	        boolean didPrintAStatusLine = false;
			while(!job.isDone()) {
				if (job.isReady()) {
	                float progress = job.getDoneProgress() * 100.0f;
	                int scanned = job.getScanCount();
	                int matched = job.getEventCount();
	                int results = job.getResultCount();
	                System.out.format(
	                    "\r%03.1f%% done -- %d scanned -- %d matched -- %d results",
	                    progress, scanned, matched, results);
	                didPrintAStatusLine = true;
	            }

	            Thread.sleep(1000);
			}
			
			Args outputArgs = new Args();
	        outputArgs.put("count", job.getResultCount());
	        outputArgs.put("offset", 0);
	        outputArgs.put("output_mode", "json");
			
			is = job.getResults(outputArgs);
			
			if (didPrintAStatusLine)
	            System.out.println("");
			
			resultsReaderNormalSearch = new ResultsReaderJson(is);
			List<Map<String, String>> eventList = new ArrayList<Map<String, String>>();
		    HashMap<String, String> event;
		    while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
		    	
		    	System.out.println("EVENT:********");
                for (String key : event.keySet())
                    System.out.println("  " + key + " --> " + event.get(key));
                
		    	eventList.add(event);
		    }
			
		    is = job.getEvents();
			
		    LOG.debug("EVENT COUNT = {} / SCAN COUNT = {} / eventList size = {}", job.getEventCount(), job.getScanCount(), eventList.size());
			
		    job.cancel();
		    
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sc != null) {
				sc.disconnect();
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (resultsReaderNormalSearch != null) {
				try {
					resultsReaderNormalSearch.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	@Override
	public Object getLogByBlockingSearch(SplunkRequest model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLogByOneshotSearch(SplunkRequest model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLogByRealtimeSearch(SplunkRequest model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLogByExportSearch(SplunkRequest model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeLogs(String json) {

		SplunkClient sc = null;

		try {

			sc = new SplunkClient();
            sc.connect(sProperties.getHostIp(), sProperties.getUsername(), sProperties.getPassword(), sProperties.getPort(), sProperties.getScheme());

            sc.splunkService.getReceiver().log("movie", json);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    sc.disconnect();
        }

	}

    @Override
    public List<Event> getMovies(String query) {

		List<Event> eventList = new ArrayList<Event>();

		SplunkClient sc = null;
        InputStream is = null;
        ResultsReader resultsReaderNormalSearch = null;

        try {

            sc = new SplunkClient();
            sc.connect(sProperties.getHostIp(), sProperties.getUsername(), sProperties.getPassword(), sProperties.getPort(), sProperties.getScheme());

            JobCollection jobs = sc.splunkService.getJobs();

            Job job = jobs.create(query);

            // Wait until results are available.
            boolean didPrintAStatusLine = false;
            while(!job.isDone()) {
                if (job.isReady()) {
                    float progress = job.getDoneProgress() * 100.0f;
                    int scanned = job.getScanCount();
                    int matched = job.getEventCount();
                    int results = job.getResultCount();
                    System.out.format(
                            "\r%03.1f%% done -- %d scanned -- %d matched -- %d results",
                            progress, scanned, matched, results);
                    didPrintAStatusLine = true;
                }

                Thread.sleep(1000);
            }

            Args outputArgs = new Args();
            outputArgs.put("count", job.getResultCount());
            outputArgs.put("offset", 0);
            outputArgs.put("output_mode", "json");

            is = job.getResults(outputArgs);

            if (didPrintAStatusLine)
                System.out.println("");

            resultsReaderNormalSearch = new ResultsReaderJson(is);
            Event event;
            while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
                eventList.add(event);
            }

            LOG.debug("{}", eventList);

            is = job.getEvents();

            LOG.debug("EVENT COUNT = {} / SCAN COUNT = {} / eventList size = {}", job.getEventCount(), job.getScanCount(), eventList.size());

            job.cancel();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sc != null) {
                sc.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (resultsReaderNormalSearch != null) {
                try {
                    resultsReaderNormalSearch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return eventList;
    }

}
