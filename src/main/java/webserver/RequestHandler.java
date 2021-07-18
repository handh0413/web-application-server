package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;
import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	HttpRequest request = new HttpRequest(in);
        	HttpResponse response = new HttpResponse(out);
        	
        	Map<String, Controller> requestMapping = new HashMap<>();
        	requestMapping.put("/user/create", new CreateUserController());
        	requestMapping.put("/user/login", new LoginController());
        	requestMapping.put("/user/list", new ListUserController());

            String url = getDefaultUrl(request.getPath());
            
            Controller controller = requestMapping.get(url);
            if (controller != null) {
            	controller.service(request, response);
            } else {
            	response.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getDefaultUrl(String url) {
        if (url.equals("/")) {
            url = "/index.html";
        }
        return url;
    }
}
