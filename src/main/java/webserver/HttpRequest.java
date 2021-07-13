package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	
	private String method;
	private String path;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	
	public HttpRequest(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String requestLine = br.readLine();
		String[] tokens = requestLine.split(" ");
		this.method = tokens[0];
		String uri = tokens[1];
		
		while (true) {
			String line = br.readLine();
			if (line == null || line.equals("")) {
				break;
			}
			String field = line.split(": ")[0].trim();
			String value = line.split(": ")[1].trim();
			headers.put(field, value);
		}
		
		if ("GET".equals(method)) {
			int index = uri.indexOf("?");
			if (index > -1) {
				this.path = uri.substring(0, index);
				String queryString = uri.substring(index + 1);
				String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");
				params = HttpRequestUtils.parseQueryString(decodedQueryString);
			} else {
				this.path = uri;
			}
		} else if ("POST".equals(method)) {
			this.path = uri;
			String queryString = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
			String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");
			params = HttpRequestUtils.parseQueryString(decodedQueryString);
		}
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setpath(String path) {
		this.path = path;
	}
	
	public String getHeader(String field) {
		return headers.get(field);
	}
	
	public String getParameter(String field) {
		return params.get(field);
	}
}
