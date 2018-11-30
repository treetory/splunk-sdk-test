package com.treetory.test.service;

import com.google.gson.Gson;
import com.splunk.Event;
import com.splunk.JobArgs;
import com.splunk.JobResultsArgs;
import com.treetory.test.common.util.http.HttpClient;
import com.treetory.test.mvc.model.SplunkJobCommand;
import com.treetory.test.mvc.service.SplunkExampleService;
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

import java.util.Calendar;
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
    private SplunkExampleService seService;

	@Autowired
	private HttpClient httpClient;

	@Autowired
    private Gson gson;

	@Test
	public void testDoSearch() throws Exception {

		String[] queries = {
                "index=\"moca_result\" category=* | top limit=4 category \n" +
                        "| eval action=if( category == 0, 3.5, if( category == 1, 2.5, if( category == 2, 1.5, 0.5 ) ) )\n" +
                        "| sort -action\n" +
                        "| head 1\n" +
                        "| table action"
                 ,
                "index=\"moca_system\" | spath | rename {}.cpu{}.usage_rate as cpu | top limit=1 cpu | table cpu \n" +
                        "| appendcols [  search index=\"moca_system\" | spath output=thres path={}.threshold{0}.threshold | top limit=1 thres | table thres ]\n" +
                        "| rangemap field=cpu default=\"안전\"\n" +
                        "| head ( cpu < thres)\n" +
                        "| appendpipe [ stats count | eval \"cpu_danger\"=0 | where count=0 | table \"cpu_danger\" ]\n" +
                        "| rangemap field=cpu_danger default=\"확인 필요\""
                ,
                "index=\"moca_system\" | spath | rename {}.disk{}.usage_rate as disk | top limit=1 disk | table disk \n" +
                        "| appendcols [  search index=\"moca_system\" | spath output=thres path={}.threshold{2}.threshold | top limit=1 thres | table thres ]\n" +
                        "| rangemap field=disk default=\"안전\"\n" +
                        "| head ( disk < thres)\n" +
                        "| appendpipe [ stats count | eval \"disk_danger\"=0 | where count=0 | table \"disk_danger\" ]\n" +
                        "| rangemap field=disk_danger default=\"확인 필요\""
                ,
                "index=\"moca_system\" | spath | rename {}.memory{}.usage_rate as mem | top limit=1 mem | table mem \n" +
                        "| appendcols [  search index=\"moca_system\" | spath output=thres path={}.threshold{1}.threshold | top limit=1 thres | table thres ]\n" +
                        "| rangemap field=mem default=\"안전\"\n" +
                        "| head ( mem < thres)\n" +
                        "| appendpipe [ stats count | eval \"mem_danger\"=0 | where count=0 | table \"mem_danger\" ]\n" +
                        "| rangemap field=mem_danger default=\"확인 필요\""
                ,
                "index=\"moca_log\" signature_name=* | top limit=5 signature_name showperc=false | appendpipe [ stats count | eval \"signature_name\"=\"-\" | where count=0 | table \"signature_name\", \"count\" ]"
                ,
                "index=\"moca_result\" rule=* | top limit=5 rule showperc=false | appendpipe [ stats count | eval \"rule\"=\"-\" | where count=0 | table \"rule\", \"count\" ]"
                ,
                "index=\"moca_log\" s_ip=* | top limit=5 s_ip showperc=false | appendpipe [ stats count | eval \"s_ip\"=\"-\" | where count=0 | table \"s_ip\", \"count\" ]"
                ,
                "index=\"moca_log\" d_ip=* | top limit=5 d_ip showperc=false | appendpipe [ stats count | eval \"d_ip\"=\"-\" | where count=0 | table \"d_ip\", \"count\" ]"
                ,
                "index=\"moca_result\" rule=* | table \"create_ts\", \"s_ip\", \"d_ip\", \"message\" | appendpipe [ stats count | eval \"create_ts\"=\"-\", s_ip=\"-\", \"d_ip\"=\"-\", \"message\"=\"-\" | where count=0 | table \"create_ts\", \"s_ip\", \"d_ip\", \"message\" ]"
                }
                ;

		JobArgs jobArgs = new JobArgs();
        jobArgs.setExecutionMode(JobArgs.ExecutionMode.NORMAL);
        jobArgs.setSearchMode(JobArgs.SearchMode.NORMAL);

        for (String query : queries) {

            JobResultsArgs resultsArgs = new JobResultsArgs();
            resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

            SplunkJobCommand command = SplunkJobCommand.create(query, jobArgs, resultsArgs, SplunkJobCommand.JOB_RESULT_TYPE.results);
            List<?> result = sService.getLogByNormalSearch(command);

            LOG.debug("{}", result);

            assertNotNull(result);

        }

	}

	@Test
	public void testNormalSearch() throws Exception {

		String query = "index=\"moca_result\" rule=* | table \"create_ts\", \"s_ip\", \"d_ip\", \"message\" | appendpipe [ stats count | eval \"create_ts\"=\"-\", s_ip=\"-\", \"d_ip\"=\"-\", \"message\"=\"-\" | where count=0 | table \"create_ts\", \"s_ip\", \"d_ip\", \"message\" ]";

        JobArgs jobArgs = new JobArgs();
        jobArgs.setExecutionMode(JobArgs.ExecutionMode.ONESHOT);
        jobArgs.setSearchMode(JobArgs.SearchMode.NORMAL);
        long current = System.currentTimeMillis();
        jobArgs.setEarliestTime("2018-11-28");
        jobArgs.setLatestTime("2018-11-28");
        jobArgs.setTimeFormat("yyyy-MM-dd");

        JobResultsArgs resultsArgs = new JobResultsArgs();
        resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

		SplunkJobCommand jobCommand = SplunkJobCommand.create(query, jobArgs, resultsArgs, SplunkJobCommand.JOB_RESULT_TYPE.results);

	    List<?> result = seService.getLogByNormalSearch(jobCommand);
        LOG.debug("{}", result.size());

        assertNotNull(result);
    }

	@Test
	public void testGetMovieFromOpenAPI() throws Exception {

	    String url = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=430156241533f1d058c603178cc3ca0e&targetDt=20181125";

	    Object obj = httpClient.get(url);

        seService.writeLogs(gson.toJson(obj));

    }

    @Test
    public void testGetMovieFromSplunk() throws Exception {

	    String query = "search index = \"movie\"";
	    List<Event> result = seService.getMovies(query);

	    LOG.debug("{}", result);

		assertNotNull(result);

    }
}
