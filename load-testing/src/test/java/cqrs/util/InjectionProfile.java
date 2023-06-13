package cqrs.util;

import io.gatling.javaapi.core.OpenInjectionStep;

import static cqrs.util.ExternalPropertiesConfig.*;
import static io.gatling.javaapi.core.CoreDsl.*;

public class InjectionProfile {

    /**
     * Single user, single execution load profile used for operational checks.
     */
    public static OpenInjectionStep smokeProfile() {
        return atOnceUsers(1);
    }

    /**
     * Target load profile which will continually start new user threads to maintain the target TPS.
     * This load profile is commonly used in target load and soak tests.
     * <p>
     * Function will ramp up the request rate to a maximum of {@code targetTps} over the {@code targetTpsRampDuration} period.
     * Once full TPS has been achieved it will be held constant for {@code targetTpsDuration}.
     *
     * @param targetTps The target TPS to reach at full speed
     * @param rampDuration The number of seconds to reach full TPS
     * @param tpsDuration The number of seconds to hold at full TPS
     * @return a configured injection profile
     */
    public static OpenInjectionStep[] targetTpsProfile(Double targetTps, Integer rampDuration, Integer tpsDuration) {
        return new OpenInjectionStep[]{
                rampUsersPerSec(0.0).to(targetTps).during(rampDuration),
                constantUsersPerSec(targetTps).during(tpsDuration)
        };
    }

    /**
     * Step-wise load profile which will step up the number of requests at set intervals.
     * This load profile is commonly used during fatigue/break-point testing.
     * <p>
     * Function will ramp up the request rate to the {@code initialTargetTps} over {@code stepDuration}.
     * Once {@code initialTargetTps} has been achieved the function will incrementally increase the request rate by
     * {@code stepTpsIncrease} TPS every {@code stepDuration} seconds, {@code totalStepCount} times.
     */
    public static OpenInjectionStep[] stepWiseProfile(Double initialTargetTps, Double stepTpsIncrease, Long stepDuration, Integer totalStepCount) {
        return new OpenInjectionStep[]{
                rampUsersPerSec(0)
                        .to(initialTargetTps)
                        .during(stepDuration), // warm up
                incrementUsersPerSec(stepTpsIncrease)
                        .times(totalStepCount)
                        .eachLevelLasting(stepDuration)
                        .startingFrom(initialTargetTps) // incrementing load
        };
    }

    /**
     * All users at once load profile which adds some base load and then at set intervals, introduces a large number of users all at once.
     * This load profile is commonly used during spike testing.
     * <p>
     * Function will ramp up the request rate to the {@code spikeBaseTps} over {@code rampDuration}.
     * Once {@code spikeBaseTps} has been achieved the function will add {@code spikeMaxTps} users all at once, every {@code spikeInterval} seconds.
     * This will simulate a large number of simultaneous user requests.
     */
    public static OpenInjectionStep[] spikeProfile(Double spikeBaseTps, Long rampDuration, Integer spikeMaxTps, Integer spikeInterval) {
        return new OpenInjectionStep[]{
                // warm up
                rampUsersPerSec(0).to(spikeBaseTps).during(rampDuration),
                // repeat spike (5 times)
                // TODO can we specify loop size via parameter
                constantUsersPerSec(spikeBaseTps).during(spikeInterval),
                atOnceUsers(spikeMaxTps), // min user is 1
                constantUsersPerSec(spikeBaseTps).during(spikeInterval),
                atOnceUsers(spikeMaxTps), // min user is 1
                constantUsersPerSec(spikeBaseTps).during(spikeInterval),
                atOnceUsers(spikeMaxTps), // min user is 1
                constantUsersPerSec(spikeBaseTps).during(spikeInterval),
                atOnceUsers(spikeMaxTps), // min user is 1
                constantUsersPerSec(spikeBaseTps).during(spikeInterval),
                // cool down
                rampUsersPerSec(spikeBaseTps).to(0).during(spikeInterval)
        };
    }

}
