package http;

public abstract class AbstractController implements Controller {
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		HttpMethod method = request.getMethod();
		
		if (method.isPost()) {
			doPost(request, response);
		} else {
			doGet(request, response);
		}
	}

	public abstract void doGet(HttpRequest request, HttpResponse response);
	public abstract void doPost(HttpRequest request, HttpResponse response);
}
