package com.treetory.test.common.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class HttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    private RestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    public HttpClient() {
    }

    public HttpClient(String[] supportedProtocols) {
        this.initialize(supportedProtocols);
    }

    @PostConstruct
    public void initialize() {
        this.initialize(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
    }

    private void initialize(String[] supportedProtocols) {

        try {
            /**
             * RestTemplate 을 생성하고, 사용하는데 필요한 것이
             * 	1. ClientHttpRequestFactory	: SSLContext 기반의 SSLConnectionSocketFactory 를 통해 build 된 HttpClient 를 setting 한 HttpComponentsClientHttpRequestFactory
             * 	2. HttpMessageConverter : Http(s) protocol 을 통해 주고받는 메시지를 Converting 하기 위한 메시지 컨버터 목록
             *
             * 	-> no suitable HttpMessageConverter found for response type [XXX]  and content type [application/octet-stream] Exception 발생 시 참고할 것
             * 		application/octet-stream 으로 받은 response 를 변환할 적합한 컨버터가 없다는 것 이므로,
             * 		컨버팅을 담당할 컨버터에 해당 미디어 타입을 지원하도록 등록한다.
             * 		아래는 MappingJackson2HttpMessageConverter 에 application/octet-stream 을 등록하는 것으로 해결했다.
             *
             */
            this.rest = new RestTemplate();

            /**
             * SSLContext 를 default 로 생성하면, 신뢰할 수 있는 인증서(공인인증서) 일 때만 통신이 가능하다.
             * 아래 처럼 TrustStrategy 를 생성할 때, X509 인증서를 모두 신뢰하도록 해버리면, 공인되지 않은 사설 인증서도 통신이 가능하도록 한다.
             */
            // SSLContext sslContext = SSLContexts.createDefault();			// default 로 생성
            /*
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
            {
               @Override
               public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
               {
                  return true;
               }
            }).build();
            */

            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    return true;
                }

            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, supportedProtocols, null, new NoopHostnameVerifier());

            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectTimeout(10 * 1000);
            requestFactory.setReadTimeout(10 * 1000);

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

            MappingJackson2HttpMessageConverter mj2hmConverter = new MappingJackson2HttpMessageConverter(objectMapper);
            mj2hmConverter.setSupportedMediaTypes(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_OCTET_STREAM}));

            messageConverters.add(mj2hmConverter);
            messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
            messageConverters.add(new ByteArrayHttpMessageConverter());
            messageConverters.add(new FormHttpMessageConverter());

            this.rest.setRequestFactory(requestFactory);
            this.rest.setMessageConverters(messageConverters);

        } catch (Exception e) {
            LOG.error("{}", e.getMessage());
        }

    }

    private HttpHeaders getHttpHeaders(MediaType mediaType) {
        HttpHeaders httpHeaders = new HttpHeaders();

        switch (mediaType.getType()) {
            case MediaType.APPLICATION_JSON_UTF8_VALUE:

                httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
                httpHeaders.add(HttpHeaders.ACCEPT_ENCODING, "UTF-8");
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

                break;
            default:
                break;
        }
        return httpHeaders;
    }

    public Object get(String url) {

        HttpEntity<Object> httpEntity = new HttpEntity<Object>(new HttpHeaders());

        ResponseEntity<?> response = rest.exchange(url, HttpMethod.GET, null, Object.class);

        return response.getBody();
    }

}
