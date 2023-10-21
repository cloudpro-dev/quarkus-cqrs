package cqrs;

import static cqrs.util.ExternalPropertiesConfig.*;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import cqrs.util.ExternalPropertiesConfig;
import cqrs.util.InjectionProfile;
import cqrs.util.Protocol;
import cqrs.util.Scenario;
import io.gatling.javaapi.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulation of users performing various bank related activities.
 */
public class ConfiguredTestSimulation extends Simulation {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredTestSimulation.class);

    private final Scenario scenario = new Scenario();

    /*
     * Test definitions
     */
    OpenInjectionStep smokeTest = InjectionProfile.smokeProfile();

    OpenInjectionStep[] maxTpsTest = InjectionProfile.stepWiseProfile(10.0, 5.0, 10L, 4);

    OpenInjectionStep[] spikeTest = InjectionProfile.spikeProfile(10.0, 10L, 50, 10);

    OpenInjectionStep[] targetTpsTest = InjectionProfile.targetTpsProfile(targetTps, targetTpsRampDuration, targetTpsDuration);

    {
        LOG.info("Test Properties = {}", ExternalPropertiesConfig.getString());

        setUp(
            // scenario.getEventStoreCommands().injectOpen(smokeTest),
            // scenario.getViewStoreCommands().injectOpen(smokeTest)

            // commands.injectOpen(InjectionProfile.stepWiseProfile(10.0, 1.0, 150L, 12)),
            // queries.injectOpen(InjectionProfile.stepWiseProfile(10.0, 3.0, 150L, 12))

            // commands.injectOpen(spikeTest),
            // queries.injectOpen(spikeTest)

            scenario.getEventStoreCommands().injectOpen(constantUsersPerSec(1.0).during(30)),
            scenario.getViewStoreCommands().injectOpen(constantUsersPerSec(1.0).during(30))
        ).protocols(Protocol.SERVICE_PROTOCOL);
    }

}

