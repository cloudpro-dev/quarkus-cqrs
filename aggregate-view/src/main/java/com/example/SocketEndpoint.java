package com.example;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/bankaccount-aggregates/{username}")
@ApplicationScoped
public class SocketEndpoint {

    private final static Logger logger = Logger.getLogger(SocketEndpoint.class);

    private final Map<String, Session> socketSessions = new HashMap<>();

    @Incoming("event-store-aggregated")
    public Uni<Void> processMessage(Message<String> message) {
        logger.infof("Received message %s", message.getPayload());
        Aggregation aggregation = SerializerUtils.deserializeFromString(message.getPayload(), Aggregation.class);
        logger.infof("Extracted payload %s", aggregation);

        return Uni.createFrom()
                .item(message.getPayload())
                .onItem().invoke(this::broadcast)
                .replaceWithVoid()
                .onItem().invoke(v -> message.ack())
                .onFailure().invoke(ex -> logger.error("consumer process aggregate: %s", aggregation, ex));
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        logger.debugf("{} has just connected", username);
        socketSessions.put(username, session);
        // session.getAsyncRemote().sendText(String.format("Welcome to the show %s", username));
    }

    public void onMessage(String message, @PathParam("username") String username) {
        logger.debugf("{} has just sent us a message: {}", username, message);
        Session session = socketSessions.get(username);
        session.getAsyncRemote().sendText(message, result -> {
            if (result.isOK()) {
                logger.debug("Echoed message back successfully!");
            }
        });
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        logger.errorf("{} encountered an error", username);
    }

    @OnClose
    public void onClose(Session session) {
        logger.debug("session disconnected");
    }

    private void broadcast(String aggregation) {
        logger.debugf("Sending message {}", aggregation);
        logger.debugf("Total sessions is {}", socketSessions.size());
        socketSessions.values().forEach(s -> {
            logger.debug("Getting ready to send");
            s.getAsyncRemote().sendObject(aggregation, result -> {
                if (result.getException() != null) {
                    logger.error("Unable to send message: {}", result.getException().getMessage(), result.getException());
                }

                if(result.isOK()) {
                    logger.debug("Message was sent!");
                }
            });
        });
    }

}
