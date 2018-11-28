package com.treetory.test.mvc.model;

import com.splunk.Args;
import com.splunk.JobArgs;
import com.splunk.JobResultsArgs;

public class SplunkJobCommand {

    public static enum JOB_RESULT_TYPE {
        results, events, preview, searchlog, summary, timeline
    }

    private String query;
    private JobArgs jobArgs;
    private JOB_RESULT_TYPE type;
    private Args outputArgs;

    public String getQuery() {
        return this.query;
    }
    public JobArgs getJobArgs() {
        return this.jobArgs;
    }
    public Args getOutputArgs() {
        return this.outputArgs;
    }
    public JOB_RESULT_TYPE getType() {
        return this.type;
    }

    public static SplunkJobCommand create(String query, JobArgs jobArgs, Args outputArgs, JOB_RESULT_TYPE type) {
        SplunkJobCommand job = new SplunkJobCommand();
        job.query = String.format("search %s", query);
        job.jobArgs = jobArgs;
        job.outputArgs = outputArgs;
        job.type = type;
        return job;
    }
}
