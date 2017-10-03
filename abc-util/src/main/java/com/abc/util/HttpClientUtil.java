package com.abc.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 *
 */
public class HttpClientUtil {

	private static PoolingHttpClientConnectionManager poolMgr;

	private static final String UTF_8 = "UTF-8";

	private static final Logger logger = LoggerFactory
			.getLogger(HttpClientUtil.class);

	private static void init() {
		if (poolMgr != null)
			return;
		poolMgr = new PoolingHttpClientConnectionManager();
		poolMgr.setMaxTotal(200);
		poolMgr.setDefaultMaxPerRoute(20);
	}

	private static CloseableHttpClient getHttpClient() {
		init();
		return HttpClients.custom().setConnectionManager(poolMgr).build();
	}

	public static String httpGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet);
	}

	public static String httpPostRequest(String url) {
		HttpPost httpPost = new HttpPost(url);
		return getResult(httpPost);
	}

	public static String httpGetRequest(String url, Map<String, Object> params) {
		HttpGet httpGet = new HttpGet(buildURI(url, params));
		return getResult(httpGet);
	}

	public static String httpPostRequest(String url, Map<String, Object> params) {
		return getResult(buildHttpPost(url, params));
	}

	public static <T> T httpGetRequest(String url,
			final ResponseHandler<? extends T> responseHandler) {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet, responseHandler);
	}

	public static <T> T httpPostRequest(String url,
			final ResponseHandler<? extends T> responseHandler) {
		HttpPost httpPost = new HttpPost(url);
		return getResult(httpPost, responseHandler);
	}

	public static <T> T httpGetRequest(String url, Map<String, Object> params,
			final ResponseHandler<? extends T> responseHandler) {
		HttpGet httpGet = new HttpGet(buildURI(url, params));
		return getResult(httpGet, responseHandler);
	}

	public static <T> T httpPostRequest(String url, Map<String, Object> params,
			final ResponseHandler<? extends T> responseHandler) {
		return getResult(buildHttpPost(url, params), responseHandler);
	}

	private static URI buildURI(String url, Map<String, Object> params) {
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);
		ArrayList<NameValuePair> pairs = covertParams(params);
		ub.setParameters(pairs);
		URI uri = null;
		try {
			uri = ub.build();
		} catch (URISyntaxException e) {
			logger.error("{}", e);
		}
		return uri;
	}

	private static HttpPost buildHttpPost(String url, Map<String, Object> params) {
		HttpPost httpPost = new HttpPost(url);
		ArrayList<NameValuePair> pairs = covertParams(params);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
		} catch (UnsupportedEncodingException e) {
			logger.error("{}", e);
		}
		return httpPost;
	}

	private static ArrayList<NameValuePair> covertParams(
			Map<String, Object> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			pairs.add(new BasicNameValuePair(param.getKey(), String
					.valueOf(param.getValue())));
		}
		return pairs;
	}

	private static <T> T getResult(HttpRequestBase request,
			final ResponseHandler<? extends T> responseHandler) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			return httpClient.execute(request, responseHandler);
		} catch (ClientProtocolException e) {
			logger.error("{}", e);
		} catch (IOException e) {
			logger.error("{}", e);
		}
		return null;
	}

	private static String getResult(HttpRequestBase request) {
		return getResult(request, new BasicResponseHandler());
	}

}
