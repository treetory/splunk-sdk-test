package com.treetory.test.common.custom.view;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonViewResolver implements ViewResolver
{
	
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception
	{
		ObjectMapper om = new ObjectMapper();
		om.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		om.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		
		MappingJackson2JsonView view = new MappingJackson2JsonView(om);
		view.setPrettyPrint(true);
		
		return view;
	}
}
