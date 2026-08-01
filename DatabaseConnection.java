package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3306;
    private static final String DB_NAME = "mascot_baseball_grand_prix";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Hasmad616";
    private static final String OPTIONS =
            "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&connectTimeout=5000"
            + "&socketTimeout=10000"
            + "&tcpKeepAlive=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";
    private static final String SERVER_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + OPTIONS;
    private static final String DATABASE_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + OPTIONS;

    private static volatile boolean configured;

    private DatabaseConnection() {
    }

    public static synchronized void configureDefaultAndVerify() throws SQLException {
        loadMysqlDriver();
        createDatabaseIfMissing();
        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DB_USERNAME, DB_PASSWORD)) {
            if (!connection.isValid(3)) {
                throw new SQLException("MySQL did not validate the database connection.");
            }
        }
        configured = true;
    }

    private static void createDatabaseIfMissing() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                SERVER_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    public static Connection open() throws SQLException {
        if (!configured) {
            configureDefaultAndVerify();
        }
        return DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static void clear() {
        configured = false;
    }

    public static boolean isConfigured() {
        return configured;
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
}
