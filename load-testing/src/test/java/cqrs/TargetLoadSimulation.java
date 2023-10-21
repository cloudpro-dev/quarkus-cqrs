package cqrs;

import cqrs.util.ExternalPropertiesConfig;
import cqrs.util.InjectionProfile;
import cqrs.util.Protocol;
import cqrs.util.Scenario;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cqrs.util.ExternalPropertiesConfig.*;

public class TargetLoadSimulation extends Simulation {
    private static final Logger LOG = LoggerFactory.getLogger(TargetLoadSimulation.class);

    private final Scenario scenario = new Scenario();

    OpenInjectionStep[] targetTps = InjectionProfile.targetTpsProfile(ExternalPropertiesConfig.targetTps, targetTpsRampDuration, targetTpsDuration);

    {
        LOG.info("Test Properties = {}", ExternalPropertiesConfig.getString());

        setUp(
            scenario.getEventStoreCommands().injectOpen(targetTps),
            scenario.getViewStoreCommands().injectOpen(targetTps)
        ).protocols(Protocol.SERVICE_PROTOCOL);
    }
}
