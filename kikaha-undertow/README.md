### Undertow Module
Uses Undertow as the communication backbone to handle http requests.

## Deploying Using CDI
Probably the easiest and most versatile way to bootstrap your application is using the Injector's CDI.

```java
public class ApplicationBootstrap {

    public static void main( String[] args ) {
        val cdi = new InjectionContext();
        cdi.instanceOf( UndertowServer.class ).start();
    }
}
```

## Manual deployment
This module only handles Undertow's compatible HttpHandler implementation.
Kikaha exposes Undertow's HttpHandler as a `WebRouteHandler`. Thus, it doesn't only
gives you the flexibility you may need on your projects, but you are not even
obied to use Kikaha's CDI in order to have your REST API properly deployed.

```java
public class ApplicationBootstrap {

    public static void main( String[] args ) {
        val handlers = ServiceLoader.load( WebRouteHandler.class );
        val customRouter = new CustomRoutes( handlers );
        val config = ConfigLoader.loadDefaults();
        val server = UndertowServer.usingDefaults()
            .configure(config)
            .requestHandler( customRouter )
            .start();
    }
}
```
