package com.example.listener;

import com.example.events.Event;
import io.smallrye.mutiny.Uni;

public interface Projection {
    Uni<Void> when(Event event);
}
