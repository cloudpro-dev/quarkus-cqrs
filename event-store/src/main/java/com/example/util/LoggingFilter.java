package com.example.util;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.IOException;
import java.util.Locale;

@ApplicationScoped
public class LoggingFilter {

    private final static Logger logger = Logger.getLogger(LoggingFilter.class);

    @ServerRequestFilter
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    }

    @ServerResponseFilter
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        final var method = containerRequestContext.getMethod();
        final var path = containerRequestContext.getUriInfo().getPath();
        final int status = containerResponseContext.getStatus();
        if ( !path.contains("/q/metrics")) {
            logger.infof("(HTTP) method: %s, path: %s, status: %d", method.toUpperCase(Locale.ROOT), path, status);
        }
    }
}
