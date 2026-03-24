package steps.context;

public final class SensitiveDataResolver {

    private SensitiveDataResolver() {
    }

    public static String resolve(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("env:")) {
            return requireFromEnvironment(trimmed.substring(4));
        }

        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String key = trimmed.substring(2, trimmed.length() - 1);
            return requireFromEnvironment(key);
        }

        return value;
    }

    public static String resolveOrDefault(String envKey, String defaultValue) {
        if (envKey == null || envKey.isBlank()) {
            return defaultValue;
        }
        String normalizedKey = envKey.trim();
        String envValue = System.getenv(normalizedKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String propValue = System.getProperty(normalizedKey);
        if (propValue != null && !propValue.isBlank()) {
            return propValue;
        }
        return defaultValue;
    }

    private static String requireFromEnvironment(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Chave de variavel de ambiente invalida.");
        }

        String normalizedKey = key.trim();
        String envValue = System.getenv(normalizedKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String propertyValue = System.getProperty(normalizedKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        throw new IllegalStateException(
                "Variavel nao definida: " + normalizedKey
                + ". Configure como variavel de ambiente ou -D" + normalizedKey + "=valor"
        );
    }
}
