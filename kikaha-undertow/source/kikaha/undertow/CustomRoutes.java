package kikaha.undertow;

import injector.AllOf;
import injector.Singleton;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HttpString;
import io.undertow.util.PathTemplateMatcher;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Singleton
@NoArgsConstructor
public class CustomRoutes implements RequestHandler {

    static final AttachmentKey<Map<String, String>> pathParametersKey = AttachmentKey.create( Map.class );
    final Map<HttpString, PathTemplateMatcher<RequestHandler>> matchers = new HashMap<>();

    public CustomRoutes( @AllOf(WebRouteHandler.class) Iterable<WebRouteHandler> routes ){
        for (WebRouteHandler route : routes)
            add(route);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange, RequestHandler next) {
        val method = exchange.getRequestMethod();
        val matcher = matchers.get( method );

        if ( matcher == null )
            next.handleRequest( exchange, null );
        else {
            val matched = matcher.match( exchange.getRequestPath() );
            if ( matched == null )
                next.handleRequest( exchange, null );
            else {
                exchange.putAttachment( pathParametersKey,  matched.getParameters() );
                matched.getValue().handleRequest( exchange, next );
            }
        }
    }

    public CustomRoutes add( WebRouteHandler route ) {
        return add( route.getHttpMethod(), route.getRelativePath(), route );
    }

    public CustomRoutes add( String method, String path, RequestHandler handler ) {
        return add( new HttpString(method), path, handler );
    }

    public CustomRoutes add( HttpString method, String path, RequestHandler handler ) {
        val matcher = matchers.computeIfAbsent( method, m -> new PathTemplateMatcher<>() );
        matcher.add( path, handler );
        return this;
    }

    public static Map<String, String> extractRequestParameters( HttpServerExchange exchange ) {
        return exchange.getAttachment( pathParametersKey );
    }
}
