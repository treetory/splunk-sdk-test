package com.treetory.test.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.treetory.test.mvc.model.SplunkRequest;
import com.treetory.test.mvc.service.SplunkService;

@RestController
@RequestMapping(value = "/log")
public class SplunkController {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkController.class);
	
	@Autowired
	private SplunkService sService;
	
	@RequestMapping(value = "/list", method = { RequestMethod.POST }, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE }, consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Object enrollCollector(
            HttpServletRequest req, 
            HttpServletResponse res, 
            @RequestBody SplunkRequest model) throws Exception {
        
        return null;
        
    }
	
}
