package controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
	
	@Override
	public void doGet(HttpRequest request, HttpResponse response) throws IOException {

	}
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response) throws IOException {
		User user = new User(
    			request.getParameter("userId"), 
    			request.getParameter("password"), 
    			request.getParameter("name"), 
    			request.getParameter("email"));
    	log.debug("user : {}", user);
    	DataBase.addUser(user);
		response.sendRedirect("/index.html");
	}
}
