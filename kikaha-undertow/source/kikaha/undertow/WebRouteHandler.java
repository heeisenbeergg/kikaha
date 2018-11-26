package kikaha.undertow;

public interface WebRouteHandler extends RequestHandler {

    String getHttpMethod();

    String getRelativePath();
}
