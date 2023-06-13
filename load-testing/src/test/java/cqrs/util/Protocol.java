package cqrs.util;

import io.gatling.javaapi.http.HttpProtocolBuilder;

import static cqrs.util.ExternalPropertiesConfig.baseUrl;
import static io.gatling.javaapi.http.HttpDsl.http;


public class Protocol {

    public static HttpProtocolBuilder SERVICE_PROTOCOL = http.baseUrl(baseUrl).acceptHeader("application/json");

}
