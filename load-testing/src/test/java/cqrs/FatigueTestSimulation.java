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

public class FatigueTestSimulation extends Simulation {
    private static final Logger LOG = LoggerFactory.getLogger(FatigueTestSimulation.class);

    private final Scenario scenario = new Scenario();

    OpenInjectionStep[] fatigueTest = InjectionProfile.stepWiseProfile(fatigueInitialTargetTps, fatigueStepTpsIncrease, fatigueStepDuration, fatigueTotalStepCount);

    {
        LOG.info("Test Properties = {}", ExternalPropertiesConfig.getString());

        setUp(
            scenario.getEventStoreCommands().injectOpen(fatigueTest),
            scenario.getViewStoreCommands().injectOpen(fatigueTest)
        ).protocols(Protocol.SERVICE_PROTOCOL);
    }
}
