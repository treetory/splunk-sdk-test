package com.treetory.test.mvc.service;

import com.splunk.*;
import com.treetory.test.common.properties.SplunkProperties;
import com.treetory.test.common.util.splunk.SplunkClient;
import com.treetory.test.mvc.model.SplunkJobCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SplunkExampleServiceImpl implements SplunkExampleService {

    private static final Logger LOG = LoggerFactory.getLogger(SplunkServiceImpl.class);

    @Autowired
    private SplunkProperties sProperties;

    /**
     * splunk-sdk-java 의 example 을 그대로 따라한 것
     *
     * @param   jobCommand
     * @return  List<?>
     */
    @Override
    public List<?> getLogByNormalSearch(SplunkJobCommand jobCommand) {

        SplunkClient sc = null;

        Job job = null;

        InputStream is = null;
        ResultsReader resultsReaderNormalSearch = null;


        List<Map<String, String>> eventList = new ArrayList<Map<String, String>>();

        try {

            sc = new SplunkClient();
            sc.connect(sProperties.getHostIp(), sProperties.getUsername(), sProperties.getPassword(), sProperties.getPort(), sProperties.getScheme());

            JobCollection jobs = sc.splunkService.getJobs();

            job = jobs.create(jobCommand.getQuery());

            // Wait until results are available.
            boolean didPrintAStatusLine = false;

            while(!job.isReady()) {
                Thread.sleep(100);
            }

            LOG.debug("Is Job ready : {}, done : {}, finalized : {}, saved : {}", job.isReady(), job.isDone(), job.isFinalized(), job.isSaved());

            float progress = 0f;
            int scanned = 0;
            int matched = 0;
            int results = 0;

            do {

                if (job.isReady()) {

                    progress = job.getDoneProgress() * 100.0f;
                    scanned = job.getScanCount();
                    matched = job.getEventCount();
                    results = job.getResultCount();

                    didPrintAStatusLine = true;

                    System.out.format(
                            "\r%03.1f%% is Ready? [%b] is Done? [%b] -- %d scanned -- %d matched -- %d results",
                            progress, job.isReady(), job.isDone(), scanned, matched, results);

                }

                Thread.sleep(1000);

            } while(!job.isDone());

            Args outputArgs = new Args();
            outputArgs.put("count", job.getResultCount());
            outputArgs.put("offset", 0);
            outputArgs.put("output_mode", "json");

            is = job.getResults(outputArgs);

            if (didPrintAStatusLine)
                System.out.println("");

            resultsReaderNormalSearch = new ResultsReaderJson(is);
            HashMap<String, String> event;
            while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
                /*
                System.out.println("EVENT:********");
                for (String key : event.keySet())
                    System.out.println("  " + key + " --> " + event.get(key));
                    */
                eventList.add(event);
            }

            LOG.debug("EVENT COUNT = {} / SCAN COUNT = {} / eventList size = {}", job.getEventCount(), job.getScanCount(), eventList.size());

        } catch (Exception e) {

            job.cancel();

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


    @Override
    public Object getLogByBlockingSearch(SplunkJobCommand jobCommand) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getLogByOneshotSearch(SplunkJobCommand jobCommand) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getLogByRealtimeSearch(SplunkJobCommand jobCommand) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getLogByExportSearch(SplunkJobCommand jobCommand) {
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
