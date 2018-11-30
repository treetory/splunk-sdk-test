package com.treetory.test.mvc.service;

import com.splunk.Event;
import com.treetory.test.mvc.model.SplunkJobCommand;
import com.treetory.test.mvc.model.SplunkRequest;

import java.util.List;

public interface SplunkExampleService {

    public List<?> getLogByNormalSearch(SplunkJobCommand jobCommand);

    public Object getLogByBlockingSearch(SplunkJobCommand jobCommand);

    public Object getLogByOneshotSearch(SplunkJobCommand jobCommand);

    public Object getLogByRealtimeSearch(SplunkJobCommand jobCommand);

    public Object getLogByExportSearch(SplunkJobCommand jobCommand);

    public void writeLogs(String json);

    public List<Event> getMovies(String query);

}
