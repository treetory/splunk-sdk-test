package com.treetory.test.mvc.service;

import com.splunk.Event;
import com.treetory.test.mvc.model.SplunkJobCommand;
import com.treetory.test.mvc.model.SplunkRequest;

import java.util.List;

public interface SplunkService {

	public List<?> getLogByNormalSearch(SplunkJobCommand commandJob);

}
