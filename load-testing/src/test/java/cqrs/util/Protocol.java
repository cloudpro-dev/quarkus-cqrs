package cqrs.util;

import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;


public class Protocol {

    public static HttpProtocolBuilder SERVICE_PROTOCOL = http.baseUrl("http://localhost/").acceptHeader("application/json");

}
