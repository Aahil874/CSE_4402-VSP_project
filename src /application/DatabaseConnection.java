package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "mascot_baseball_grand_prix";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "Hasmad616";
    private static final String OPTIONS =
            "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&connectTimeout=5000"
            + "&socketTimeout=10000"
            + "&tcpKeepAlive=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";
    private static volatile boolean configured;
    private static volatile boolean offlineMode;
    private static volatile String host = DEFAULT_HOST;
    private static volatile int port = DEFAULT_PORT;
    private static volatile String database = DEFAULT_DATABASE;
    private static volatile String username = DEFAULT_USERNAME;
    private static volatile String password = DEFAULT_PASSWORD;

    private DatabaseConnection() {
    }

    public static synchronized void configureDefaultAndVerify() throws SQLException {
        configureAndVerify(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE,
                DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static synchronized void configureAndVerify(
            String requestedHost, String requestedPort, String requestedDatabase)
            throws SQLException {
        final int parsedPort;
        try {
            parsedPort = Integer.parseInt(requestedPort.trim());
        } catch (RuntimeException exception) {
            throw new SQLException("MySQL port must be a number.", exception);
        }
        configureAndVerify(requestedHost, parsedPort, requestedDatabase,
                DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static synchronized void configureAndVerify(
            String requestedHost, int requestedPort, String requestedDatabase,
            String requestedUsername, String requestedPassword) throws SQLException {
        String checkedHost = requireValue(requestedHost, "MySQL host");
        String checkedDatabase = requireValue(requestedDatabase, "Database name");
        String checkedUsername = requireValue(requestedUsername, "MySQL username");
        if (requestedPort < 1 || requestedPort > 65535) {
            throw new SQLException("MySQL port must be between 1 and 65535.");
        }
        if (!checkedDatabase.matches("[A-Za-z0-9_]+")) {
            throw new SQLException("Database name may contain only letters, numbers, and underscores.");
        }

        configured = false;
        offlineMode = false;
        loadMysqlDriver();
        createDatabaseIfMissing(checkedHost, requestedPort, checkedDatabase,
                checkedUsername, requestedPassword);
        try (Connection connection = DriverManager.getConnection(
                databaseUrl(checkedHost, requestedPort, checkedDatabase),
                checkedUsername, requestedPassword)) {
            if (!connection.isValid(3)) {
                throw new SQLException("MySQL did not validate the database connection.");
            }
        }

        host = checkedHost;
        port = requestedPort;
        database = checkedDatabase;
        username = checkedUsername;
        password = requestedPassword == null ? "" : requestedPassword;
        configured = true;
        offlineMode = false;
    }

    private static void createDatabaseIfMissing(
            String checkedHost, int checkedPort, String checkedDatabase,
            String checkedUsername, String checkedPassword) throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                serverUrl(checkedHost, checkedPort), checkedUsername, checkedPassword);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + checkedDatabase
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    public static Connection open() throws SQLException {
        if (offlineMode) {
            throw new SQLException("Database access is disabled while playing offline.");
        }
        if (!configured) {
            configureDefaultAndVerify();
        }
        return DriverManager.getConnection(
                databaseUrl(host, port, database), username, password);
    }

    public static void clear() {
        configured = false;
        offlineMode = false;
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static void enableOfflineMode() {
        configured = false;
        offlineMode = true;
    }

    public static boolean isOfflineMode() {
        return offlineMode;
    }

    private static void loadMysqlDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "MySQL Connector/J 8.0.25 was not found in the Eclipse module path.",
                    exception);
        }
    }

    static String defaultHost() { return DEFAULT_HOST; }
    static int defaultPort() { return DEFAULT_PORT; }
    static String defaultDatabase() { return DEFAULT_DATABASE; }
    static String defaultUsername() { return DEFAULT_USERNAME; }
    static String defaultPassword() { return DEFAULT_PASSWORD; }

    private static String serverUrl(String serverHost, int serverPort) {
        return "jdbc:mysql://" + serverHost + ":" + serverPort + "/" + OPTIONS;
    }

    private static String databaseUrl(String serverHost, int serverPort, String databaseName) {
        return "jdbc:mysql://" + serverHost + ":" + serverPort + "/"
                + databaseName + OPTIONS;
    }

    private static String requireValue(String value, String label) throws SQLException {
        if (value == null || value.isBlank()) {
            throw new SQLException(label + " cannot be empty.");
        }
        return value.trim();
    }
}
