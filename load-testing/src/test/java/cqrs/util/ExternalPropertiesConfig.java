package cqrs.util;

public class ExternalPropertiesConfig {
    /**
     * URLs for accessing services
     */
    public static String viewStoreUrl = System.getenv().getOrDefault("VIEW_STORE_URL", "http://localhost:9010");
    public static String eventStoreUrl = System.getenv().getOrDefault("EVENT_STORE_URL", "http://localhost:9020");

    /**
     * Target Load test properties
     */
    public static Double targetTps = Double.parseDouble(System.getenv().getOrDefault("TARGET_TPS", "10.0"));
    public static Integer targetTpsDuration = Integer.parseInt(System.getenv().getOrDefault("TARGET_TPS_DURATION_IN_SECS", "30"));
    public static Integer targetTpsRampDuration = Integer.parseInt(System.getenv().getOrDefault("TARGET_TPS_RAMP_PERIOD_IN_SECS", "5"));

    /**
     * Fatigue test properties
     */
    public static Double fatigueInitialTargetTps = Double.parseDouble(System.getenv().getOrDefault("FATIGUE_INITIAL_TARGET_TPS", "10.0"));
    public static Double fatigueStepTpsIncrease = Double.parseDouble(System.getenv().getOrDefault("FATIGUE_STEP_TPS_INCREASE", "5.0"));
    public static Long fatigueStepDuration = Long.parseLong(System.getenv().getOrDefault("FATIGUE_STEP_DURATION", "10"));
    public static Integer fatigueTotalStepCount = Integer.parseInt(System.getenv().getOrDefault("FATIGUE_TOTAL_STEP_COUNT", "4"));

    /**
     * Spike test properties
     */
    public static Double spikeBaseTps = Double.parseDouble(System.getenv().getOrDefault("SPIKE_BASE_TPS", "10.0"));
    public static Long spikeRampDuration = Long.parseLong(System.getenv().getOrDefault("SPIKE_RAMP_DURATION", "10"));
    public static Integer spikeMaxTps = Integer.parseInt(System.getenv().getOrDefault("SPIKE_MAX_TPS", "50"));
    public static Integer spikeInterval = Integer.parseInt(System.getenv().getOrDefault("SPIKE_INTERVAL", "10"));

    public static String getString(){
        return "{"
                + "eventStoreUrl = " + eventStoreUrl + ","
                + "viewStoreUrl = " + viewStoreUrl + ","
                + "targetTps = " + targetTps + ","
                + "targetTpsDuration = " + targetTpsDuration + ","
                + "targetTpsRampDuration = " + targetTpsRampDuration + ","
                + "}";
    }
}
