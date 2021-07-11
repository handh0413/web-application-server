package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    Map<String, String> requestHeaders = new HashMap<>();
    Map<String, String> responseHeaders = new HashMap<>();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	
        	String line = br.readLine();
        	String[] token = line.split(" ");
        	String method = token[0];
        	String url = token[1];
        	String requestPath = "";
        	String params = "";
        	
        	int index = url.indexOf("?");
        	if (index > -1) {
        		requestPath = url.substring(0, index);
        		params = url.substring(index + 1);
        	} else {
        		requestPath = url;
        	}
        	
        	while (true) {
        		line = br.readLine();
        		if (line == null || "".equals(line)) {
        			break;
        		}
        		String[] headerToken = line.split(": ");
        		String key = headerToken[0].trim();
        		String value = headerToken[1].trim();
        		requestHeaders.put(key, value);
        	}
        	
        	if (requestPath.startsWith("/user/create") && "POST".equals(method)) {
        		String body = IOUtils.readData(br, Integer.parseInt(requestHeaders.get("Content-Length")));
        		Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);
        		String userId = URLDecoder.decode(paramMap.get("userId"), "UTF-8");
        		String password = URLDecoder.decode(paramMap.get("password"), "UTF-8");
        		String name = URLDecoder.decode(paramMap.get("name"), "UTF-8");
        		String email = URLDecoder.decode(paramMap.get("email"), "UTF-8");
        		User user = new User(userId, password, name, email);
        		DataBase.addUser(user);
        		
        		DataOutputStream dos = new DataOutputStream(out);
        		response302Header(dos, "/index.html");
        	} else if (requestPath.startsWith("/user/login") && "POST".equals(method)) {
        		String body = IOUtils.readData(br, Integer.parseInt(requestHeaders.get("Content-Length")));
        		Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);
        		String inputUserId = paramMap.get("userId");
        		String inputPassword = paramMap.get("password");
        		
        		User user = DataBase.findUserById(inputUserId);
        		DataOutputStream dos = new DataOutputStream(out);
        		if (user != null && user.getUserId().equals(inputUserId) && user.getPassword().equals(inputPassword)) {
        			responseHeaders.put("Set-Cookie", "logined=true");
            		response302Header(dos, "/index.html");
        		} else {
        			responseHeaders.put("Set-Cookie", "logined=false");
            		response302Header(dos, "/user/login_failed.html");
        		}
        	} else if (requestPath.startsWith("/user/list") && "GET".equals(method)) {
        		String cookie = requestHeaders.get("Cookie");
        		Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookie);
        		boolean isLogin = Boolean.parseBoolean(cookieMap.get("logined"));
        		DataOutputStream dos = new DataOutputStream(out);
        		if (isLogin) {
        			StringBuffer buffer = new StringBuffer();
        			buffer.append("<table style='border: 1px solid #444444;'><tr><th>아이디</th><th>이름</th><th>이메일</th></tr>");
        			Collection<User> userList = DataBase.findAll();
        			Iterator<User> iter = userList.iterator();
        			while (iter.hasNext()) {
        				User user = iter.next();
        				buffer.append("<tr><td>" + user.getUserId() + "</td><td>" + user.getName() + " </td><td>" + user.getEmail() + "</td>");
        			}
        			buffer.append("</tr>");
        			byte[] body = buffer.toString().getBytes();
        			response200Header(dos, body.length);
        			responseBody(dos, body);
        		} else {
        			response302Header(dos, "/user/login_failed.html");
        		}
        	} else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                if (url.endsWith(".css")) {
                	response200CssHeader(dos, body.length);
                } else {
                	response200Header(dos, body.length);
                }
                responseBody(dos, body);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            for (String key : responseHeaders.keySet()) {
            	dos.writeBytes(key + ": " + responseHeaders.get(key) + "\r\n");
            }
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            for (String key : responseHeaders.keySet()) {
            	dos.writeBytes(key + ": " + responseHeaders.get(key) + "\r\n");
            }
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String url) {
    	try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            for (String key : responseHeaders.keySet()) {
            	dos.writeBytes(key + ": " + responseHeaders.get(key) + "\r\n");
            }
            dos.writeBytes("Location: " + url);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
