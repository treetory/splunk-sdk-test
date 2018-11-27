package com.treetory.test.mvc.service;

import com.splunk.Event;
import com.treetory.test.mvc.model.SplunkRequest;

import java.util.List;

public interface SplunkService {

	public Object getLogByNormalSearch(SplunkRequest model);

	public Object getLogByBlockingSearch(SplunkRequest model);
	
	public Object getLogByOneshotSearch(SplunkRequest model);
	
	public Object getLogByRealtimeSearch(SplunkRequest model);
	
	public Object getLogByExportSearch(SplunkRequest model);

	public void writeLogs(String json);

	public List<Event> getMovies(String query);
}
