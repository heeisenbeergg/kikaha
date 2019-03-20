package kikaha.undertow;

import injector.Producer;
import injector.Singleton;
import kikaha.config.Config;

@Singleton
class UndertowApplication {

    final Config config;
    final CustomRoutes routes;

    UndertowApplication(Config config, CustomRoutes routes) {
        this.config = config;
        this.routes = routes;
    }

    @Producer UndertowServer produceUndertowServer(){
        return UndertowServer.usingDefaults()
            .configure( config )
            .requestHandler( routes );
    }
}
