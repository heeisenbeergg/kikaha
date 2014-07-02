package io.skullabs.undertow.standalone.api;

import com.typesafe.config.Config;

public interface Configuration {

	String resourcesPath();

	Integer port();

	String host();

	HandlerTypes defaultHandlerType();

	Config config();
}