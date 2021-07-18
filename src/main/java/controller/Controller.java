package controller;

import java.io.IOException;

import webserver.HttpRequest;
import webserver.HttpResponse;

public interface Controller {
	void service(HttpRequest request, HttpResponse response) throws IOException;
}
