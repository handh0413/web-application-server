package http;

import db.DataBase;
import model.User;

public class LoginController extends AbstractController {
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		
	}
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		User user = DataBase.findUserById(request.getParameter("userId"));
        if (user != null) {
            if (user.login(request.getParameter("password"))) {
                response.addHeader("Cookie", "logined=true");
                response.sendRedirect("/index.html");
            } else {
                response.forward("/user/login_failed.html");
            }
        } else {
        	response.forward("/user/login_failed.html");
        }
	}
}
