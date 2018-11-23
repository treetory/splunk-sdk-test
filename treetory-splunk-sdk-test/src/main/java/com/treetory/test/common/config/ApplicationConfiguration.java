package com.treetory.test.common.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.treetory.test.common.custom.view.JsonViewResolver;
import com.treetory.test.common.properties.SplunkProperties;

import nz.net.ultraq.thymeleaf.LayoutDialect;

@ComponentScan(basePackages = {"com.treetory.test.mvc", "com.treetory.test.common.util"}, 
useDefaultFilters = false,
includeFilters = {
            @ComponentScan.Filter(value = Controller.class),
            @ComponentScan.Filter(value = Service.class),
            @ComponentScan.Filter(value = Repository.class),
            @ComponentScan.Filter(value = Component.class)
            }
)
@Configuration
@EnableAsync
@EnableScheduling
@EnableWebMvc
//@EnableAspectJAutoProxy
@Import(value={SplunkProperties.class})
public class ApplicationConfiguration implements InitializingBean, ApplicationListener<ApplicationEvent>, WebMvcConfigurer {

    @Autowired
    private ApplicationContext appContext;
    
    @Bean(name = "templateResolver")
    @Description("Thymeleaf template resolver serving HTML")
    public ITemplateResolver templateResolver() {
    	// SpringResourceTemplateResolver automatically integrates with Spring's own
        // resource resolution infrastructure, which is highly recommended.
    	ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        // HTML is the default value, added here for the sake of clarity.
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        // Template cache is true by default. Set to false if you want
        // templates to be automatically updated when modified.
        templateResolver.setCacheable(false);
        return templateResolver;
    }
    
    @Bean(name = "templateEngine")
    @Description("Thymeleaf template engine with Spring integration")
    public ISpringTemplateEngine templateEngine() {
    	SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    	templateEngine.setTemplateResolver((ITemplateResolver)appContext.getBean("templateResolver"));
    	templateEngine.addDialect(new LayoutDialect());
    	return templateEngine;
    }
    
    @Bean(name = "thymeleafViewResolver")
    @Description("Thymeleaf view resolver")
    public ViewResolver thymeleafViewResolver() {
    	ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    	viewResolver.setCharacterEncoding("UTF-8");
    	viewResolver.setTemplateEngine((ISpringTemplateEngine)appContext.getBean("templateEngine"));
    	return viewResolver;
    }
    
    @Bean(name = "jsonViewResolver")
    public JsonViewResolver jsonViewResolver() {
        JsonViewResolver jvr = new JsonViewResolver();
        return jvr;

    }

    @Bean(name = "contentNegotiatingViewResolver")
    @Description("To use content nogotiation view resolving strategy.")
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver() {
        ContentNegotiatingViewResolver cnvr = new ContentNegotiatingViewResolver();
        cnvr.setOrder(1);
        cnvr.setContentNegotiationManager(
                (ContentNegotiationManager) appContext.getBean("mvcContentNegotiationManager"));

        List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();
        //viewResolvers.add((InternalResourceViewResolver) appContext.getBean("internalResourceViewResolver"));
        viewResolvers.add((ViewResolver)appContext.getBean("thymeleafViewResolver"));
        viewResolvers.add((JsonViewResolver) appContext.getBean("jsonViewResolver"));

        cnvr.setViewResolvers(viewResolvers);

        return cnvr;
    }

    @Bean(name = "taskExecutor")
    @Description("Every excutable tasks is executed by this ThreadPoolTaskExecutor.")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(20);
        te.setMaxPoolSize(100);
        te.setQueueCapacity(1000);
        te.setThreadGroupName("task");
        te.setThreadNamePrefix("task");
        te.setWaitForTasksToCompleteOnShutdown(true);
        
        return te;
    }
    
    @Bean(name = "objectMapper")
    @Description("This bean is used by MappingJackson2HttpMessageConverter.")
    public ObjectMapper objectMapper() {
    	ObjectMapper om = new ObjectMapper();
    	return om;
    }
    
    @Bean(name = "gson")
    @Description("This bean is used to handle(read and write) json String.")
    public Gson gson() {
    	return new Gson();
    }
    
    /**
     * REST 요청 시, 한글로 된 body 를 받을 때 한글 깨짐 방지
     */
    @Bean
    @Description("Prevent the broken euckr character set.")
    public Filter charactertEncodingFilter() {
    	CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
    	characterEncodingFilter.setEncoding("UTF-8");
    	characterEncodingFilter.setForceEncoding(true);
    	return characterEncodingFilter;
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        /**
         * 유의사항
         * 
         * 아래 설정은 mediaType을 통해 contentNegotiation 을 수행하는 바, defaultContentType 은 text/html 이고, 그 외
         * application/json 이 추가되어 있다. 즉, 두가지 mediaType을 가지고 negotiation 한다. ignoreAcceptHeader 는
         * 반드시 false 값 이어야 한다. 그 이유는 AcceptHeader 를 무시하도록 설정해버리면 ajax 요청 시, 설정한 accept 값이 response
         * header 에 바뀌어 들어가는 경우가 있다.
         * 
         * 각 RequestMapping 에는 반드시 produces, consumes 를 Accept 받은 것과 동일하게 명시하여 사용하도록 한다.
         */
        configurer.favorPathExtension(false).parameterName("mediaType").ignoreAcceptHeader(false)
                .defaultContentType(MediaType.TEXT_HTML)
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("json", MediaType.APPLICATION_JSON_UTF8)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Override
    @Description("The message converters for using content negotiation.")
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    	
    	MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter((ObjectMapper) appContext.getBean("objectMapper"));
    	MediaType jsonMediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
    	
    	List<MediaType> mediaTypeList = new ArrayList<MediaType>();
    	mediaTypeList.add(jsonMediaType);
    	
    	jacksonMessageConverter.setSupportedMediaTypes(mediaTypeList);
    	
    	converters.add(jacksonMessageConverter);
    	converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
    	converters.add(new ByteArrayHttpMessageConverter());
    	converters.add(new SourceHttpMessageConverter<>());
    	    	
    	WebMvcConfigurer.super.configureMessageConverters(converters);
    }
    
    @Override
    @Description("Every URI for requesting view is registerd in here.")
    public void addViewControllers(ViewControllerRegistry registry) {
    	registry.addViewController("/").setViewName("login");
    }
    
    @Override
    @Description("Every resources for requesting from view is registerd in here.")
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	registry.addResourceHandler("/resources/**", "/css/**", "/images/**", "/js/**", "/lib/**", "/fonts/**")
    	.addResourceLocations(
    			"classpath:/resources/",
    			"classpath:/static/css/",
    			"classpath:/static/images/",
    			"classpath:/static/js/",
    			"classpath:/static/lib/",
    			"classpath:/static/fonts/"
    			)
    	.setCachePeriod(600).resourceChain(true).addResolver(new PathResourceResolver());
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
    	
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
           	
    	switch (event.getClass().getSimpleName()) {
    	
    	case "ContextRefreshedEvent" :
    		break;
    	case "ServletWebServerInitializedEvent" :
    		break;
    	case "ApplicationStartedEvent" :
    		break;
    	case "ApplicationReadyEvent":
    		
    		break;
    	case "ContextClosedEvent" :
    		
    		break;
    	}
    	
    }
    
}
