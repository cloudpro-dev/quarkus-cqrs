package cqrs;

import cqrs.util.ExternalPropertiesConfig;
import cqrs.util.InjectionProfile;
import cqrs.util.Protocol;
import cqrs.util.Scenario;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmokeTestSimulation extends Simulation {

    private static final Logger LOG = LoggerFactory.getLogger(SmokeTestSimulation.class);

    private final Scenario scenario = new Scenario();

    private final OpenInjectionStep smokeTest = InjectionProfile.smokeProfile();

    {
        LOG.info("Test Properties = {}", ExternalPropertiesConfig.getString());

        setUp(
            scenario.getEventStoreCommands().injectOpen(smokeTest),
            scenario.getViewStoreCommands().injectOpen(smokeTest)
        ).protocols(Protocol.SERVICE_PROTOCOL);
    }

}
