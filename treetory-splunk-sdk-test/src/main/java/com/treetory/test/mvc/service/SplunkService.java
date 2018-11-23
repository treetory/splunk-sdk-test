package com.treetory.test.mvc.service;

import com.treetory.test.mvc.model.SplunkRequest;

public interface SplunkService {

	public Object getLogByNormalSearch(SplunkRequest model);

	public Object getLogByBlockingSearch(SplunkRequest model);
	
	public Object getLogByOneshotSearch(SplunkRequest model);
	
	public Object getLogByRealtimeSearch(SplunkRequest model);
	
	public Object getLogByExportSearch(SplunkRequest model);
}
