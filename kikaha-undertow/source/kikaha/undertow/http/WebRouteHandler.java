package kikaha.undertow.http;

public interface WebRouteHandler extends RequestHandler {

    String getHttpMethod();

    String getRelativePath();
}
