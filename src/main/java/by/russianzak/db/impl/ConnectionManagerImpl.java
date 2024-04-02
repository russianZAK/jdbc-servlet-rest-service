package by.russianzak.db.impl;

import by.russianzak.db.ConnectionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManagerImpl implements ConnectionManager {

  private final HikariDataSource dataSource;

  public ConnectionManagerImpl(String jdbcUrl, String username, String password) {
    if (jdbcUrl == null || jdbcUrl.isEmpty()) {
      throw new IllegalArgumentException("JDBC URL cannot be null or empty");
    }
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    final HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    dataSource = new HikariDataSource(config);
  }

  public ConnectionManagerImpl() {
    final HikariConfig config = new HikariConfig("src/main/resources/db.properties");
    try {
      dataSource = new HikariDataSource(config);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public HikariDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
