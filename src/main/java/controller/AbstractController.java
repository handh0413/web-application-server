package controller;

import java.io.IOException;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {
	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		String method = request.getMethod();
		if ("GET".equals(method)) {
			doGet(request, response);
		} else if ("POST".equals(method)) {
			doPost(request, response);
		}
	}
	
	public abstract void doGet(HttpRequest request, HttpResponse response) throws IOException;
	public abstract void doPost(HttpRequest request, HttpResponse response) throws IOException;
}
