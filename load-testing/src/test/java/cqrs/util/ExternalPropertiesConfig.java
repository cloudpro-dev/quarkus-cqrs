package cqrs.util;

public class ExternalPropertiesConfig {
    public static String baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:9092/");
    public static Integer targetTps = Integer.parseInt(System.getenv().getOrDefault("TARGET_TPS", "100"));
    public static Integer targetTpsDuration = Integer.parseInt(System.getenv().getOrDefault("TARGET_TPS_DURATION_IN_SECS", "300"));
    public static Integer targetTpsRampDuration = Integer.parseInt(System.getenv().getOrDefault("TARGET_TPS_RAMP_PERIOD_IN_SECS", "30"));

    public static String getString(){
        return "{"
                + "baseUrl = " + baseUrl + ","
                + "targetTps = " + targetTps + ","
                + "targetTpsDuration = " + targetTpsDuration + ","
                + "targetTpsRampDuration = " + targetTpsRampDuration + ","
                + "}";
    }
}
