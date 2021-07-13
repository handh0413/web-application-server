package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Map<String, String> headers = new HashMap<>();
	DataOutputStream dos;
	
	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void forward(String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        if (url.endsWith(".css")) {
        	response200CssHeader(body.length);
        } else {
        	response200Header(body.length);
        }
        responseBody(body);
	}
	
	public void forwardBody(byte[] body) throws IOException {
		response200Header(body.length);
		responseBody(body);
	}
	
	public void sendRedirect(String url) throws IOException {
		dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
        for (String key : headers.keySet()) {
        	dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
        dos.writeBytes("Location: " + url + " \r\n");
        dos.writeBytes("\r\n");
	}
	
	private void response200Header(int lengthOfBodyContent) throws IOException {
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        for (String key : headers.keySet()) {
        	dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
        dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("\r\n");
    }

    private void response200CssHeader(int lengthOfBodyContent) throws IOException {
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        for (String key : headers.keySet()) {
        	dos.writeBytes(key + ": " + headers.get(key));
        }
        dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("\r\n");
    }
    
    private void responseBody(byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.writeBytes("\r\n");
        dos.flush();
    }
}
