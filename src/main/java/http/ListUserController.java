package http;

import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class ListUserController extends AbstractController {
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		boolean logined = false;
        if (request.getHeader("Cookie") != null) {
            logined = isLogin(request.getHeader("Cookie"));
        }
        
		if (!logined) {
            response.forward("/user/login.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        response.forwardBody(sb.toString());
	}
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		
	}
	
	private boolean isLogin(String value) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(value);
        String logined = cookies.get("logined");
        if (logined == null) {
            return false;
        }
        return Boolean.parseBoolean(logined);
    }
}
