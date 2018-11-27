package com.treetory.test.service;

import com.google.gson.Gson;
import com.splunk.Event;
import com.treetory.test.common.util.http.HttpClient;
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

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
		@ContextConfiguration(classes = TestApplicationConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class) })
public class SplunkServiceTests {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkServiceTests.class);
	
	@Autowired
	private SplunkService sService;

	@Autowired
	private HttpClient httpClient;

	@Autowired
    private Gson gson;

	//@Test
	public void testDoSearch() throws Exception {
		sService.getLogByNormalSearch(new SplunkRequest(9));
	}

	@Test
	public void testGetMovieFromOpenAPI() throws Exception {

	    String url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=430156241533f1d058c603178cc3ca0e&targetDt=20181125";

	    Object obj = httpClient.get(url);

        sService.writeLogs(gson.toJson(obj));

    }

    @Test
    public void testGetMovieFromSplunk() throws Exception {

	    String query = "search index = \"movie\"";
	    List<Event> result = sService.getMovies(query);
		assertNotNull(result);

    }
}
