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
import static cqrs.util.ExternalPropertiesConfig.fatigueTotalStepCount;

public class SpikeTestSimulation extends Simulation {

    private static final Logger LOG = LoggerFactory.getLogger(SpikeTestSimulation.class);

    private final Scenario scenario = new Scenario();

    OpenInjectionStep[] spikeTest = InjectionProfile.spikeProfile(spikeBaseTps, spikeRampDuration, spikeMaxTps, spikeInterval);

    {
        LOG.info("Test Properties = {}", ExternalPropertiesConfig.getString());

        setUp(
            scenario.getEventStoreCommands().injectOpen(spikeTest),
            scenario.getViewStoreCommands().injectOpen(spikeTest)
        ).protocols(Protocol.SERVICE_PROTOCOL);
    }
}
