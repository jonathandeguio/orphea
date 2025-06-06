package io.bosler.capture.deployments.library.model;

public class EnvConfig {

    public static String getHelmValues() {
        return getRequiredEnv("HELM_VALUES");
    }

    public static String getUpdateHost() {
        return getRequiredEnv("UPDATE_HOST");
    }

    public static String getBundleDir() {
        return getRequiredEnv("BUNDLE_DIR");
    }

    public static String getHelmChart() {
        return getRequiredEnv("HELM_CHART");
    }

    public static String getToken() {
        return getRequiredEnv("TOKEN");
    }

    public static String getDeploymentId() {
        return getRequiredEnv("DEPLOYMENT_ID");
    }

    private static String getRequiredEnv(String varName) {
        String value = System.getenv(varName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment variable '" + varName + "' is required but not set.");
        }
        return value;
    }
}
