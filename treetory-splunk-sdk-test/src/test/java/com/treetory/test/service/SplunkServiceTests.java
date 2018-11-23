package com.treetory.test.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.treetory.test.config.TestApplicationConfiguration;
import com.treetory.test.mvc.model.SplunkRequest;
import com.treetory.test.mvc.service.SplunkService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
		@ContextConfiguration(classes = TestApplicationConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class) })
public class SplunkServiceTests {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkServiceTests.class);
	
	@Autowired
	private SplunkService sService;
	
	@Test
	public void testDoSearch() throws Exception {
		sService.getLogByNormalSearch(new SplunkRequest(9));
	}
}
