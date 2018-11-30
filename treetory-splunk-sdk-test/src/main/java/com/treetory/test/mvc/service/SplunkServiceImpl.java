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
import java.util.List;
import java.util.Map;

@Service
public class SplunkServiceImpl implements SplunkService {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkServiceImpl.class);
	
	@Autowired
	private SplunkProperties sProperties;

    /**
     *  splunk job 의 수행결과 inputStream 으로부터 결과를 파싱한다.
     *
     * @param   is
     * @return  List<?>
     * @throws  IOException
     */
    private List<?> readFromInputStream(InputStream is) throws IOException {

        List<Map<String, String>> result = new ArrayList<>();
        ResultsReader resultsReaderNormalSearch = null;

        try {

            resultsReaderNormalSearch = new ResultsReaderJson(is);
            Event event;
            while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
                result.add(event);
            }

        } finally {
            if (resultsReaderNormalSearch != null)
                resultsReaderNormalSearch.close();
            if (is != null)
                is.close();
        }

        return result;
    }

    /**
     * splunk job 으로부터 타입 별, 결과를 생성한다.
     *
     * @param job
     * @param commandJob
     * @return
     */
    private List<?> makeResult(Job job, SplunkJobCommand commandJob) {

        List<?> result = null;
        Args outputArgs = commandJob.getOutputArgs();
        if (!commandJob.getOutputArgs().containsKey("count")) {
            outputArgs.put("count", job.getResultCount());
        }

        LOG.debug("RESULT COUNT : {}", job.getResultCount());

        try {

            switch (commandJob.getType()) {

                case results:

                    result = this.readFromInputStream(job.getResults(commandJob.getOutputArgs()));

                    break;
                case events:

                    result = this.readFromInputStream(job.getEvents(commandJob.getOutputArgs()));

                    break;
                case preview:

                    result = this.readFromInputStream(job.getResultsPreview(commandJob.getOutputArgs()));

                    break;
                case searchlog:

                    result = this.readFromInputStream(job.getSearchLog(commandJob.getOutputArgs()));

                    break;
                case summary:

                    result = this.readFromInputStream(job.getSummary(commandJob.getOutputArgs()));

                    break;
                case timeline:

                    result = this.readFromInputStream(job.getTimeline(commandJob.getOutputArgs()));

                    break;
            }

        } catch (IOException e) {
            LOG.error("{}", e);
        }

        return result;

    }

    /**
     * splunk job 을 생성한다.
     *
     * @param splunkService
     * @param query
     * @param jobArgs
     * @return
     * @throws InterruptedException
     */
    private Job createJob(com.splunk.Service splunkService, String query, Args jobArgs) throws InterruptedException {

        JobCollection jobs = splunkService.getJobs();

        Job job = (jobArgs == null ? jobs.create(query) : jobs.create(query, jobArgs));

        // Wait until results are available.
        boolean didPrintAStatusLine = false;

        while(!job.isReady()) {
            Thread.sleep(100);
        }

        LOG.debug("Is Job ready : {}, done : {}, finalized : {}, saved : {}", job.isReady(), job.isDone(), job.isFinalized(), job.isSaved());

        do {

            if (job.isReady()) {

                float progress = job.getDoneProgress() * 100.0f;
                int scanned = job.getScanCount();
                int matched = job.getEventCount();
                int results = job.getResultCount();

                didPrintAStatusLine = true;

                System.out.format(
                        "\r%03.1f%% is Ready? [%b] is Done? [%b] -- %d scanned -- %d matched -- %d results",
                        progress, job.isReady(), job.isDone(), scanned, matched, results);

            }

            Thread.sleep(1000);

        } while(!job.isDone());

        if (didPrintAStatusLine)
            System.out.println("");

        return job;
    }

    @Override
    public List<?> getLogByNormalSearch(SplunkJobCommand commandJob) {

        SplunkClient client = SplunkClient.withConnectionInfo(sProperties.getHostIp(), sProperties.getUsername(), sProperties.getPassword(), sProperties.getPort(), sProperties.getScheme());

        List<?> result = null;

        try {

            LOG.debug("{}", client.splunkService);
            LOG.debug("{}", commandJob.getQuery());
            LOG.debug("{}", commandJob.getJobArgs().toString());

            Job _job = this.createJob(client.splunkService, commandJob.getQuery(), commandJob.getJobArgs());

            LOG.debug(" ==> EARLIEST TIME : {}", _job.getEarliestTime());
            LOG.debug(" ==> LATEST TIME : {}", _job.getLatestTime());
            LOG.debug(" ==> CURSOR TIME : {}", _job.getCursorTime());

            result = this.makeResult(_job, commandJob);

        } catch (InterruptedException e) {
            LOG.error("{}", e);
        } finally {
            client.disconnect();
        }

        return result;
    }

}
