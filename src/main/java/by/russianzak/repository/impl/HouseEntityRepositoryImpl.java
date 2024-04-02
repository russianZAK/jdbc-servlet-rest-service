package by.russianzak.repository.impl;

import by.russianzak.db.ConnectionManager;
import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.HouseEntityRepository;
import by.russianzak.repository.mapper.HouseResultSetMapper;

import by.russianzak.repository.mapper.StreetResultSetMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HouseEntityRepositoryImpl implements HouseEntityRepository {

  private final HouseResultSetMapper houseResultSetMapper;
  private final StreetResultSetMapper streetResultSetMapper;
  private final ConnectionManager connectionManager;

  public HouseEntityRepositoryImpl(HouseResultSetMapper houseResultSetMapper,
      StreetResultSetMapper streetResultSetMapper, ConnectionManager connectionManager) {
    this.houseResultSetMapper = houseResultSetMapper;
    this.streetResultSetMapper = streetResultSetMapper;
    this.connectionManager = connectionManager;
  }

  @Override
  public HouseEntity save(HouseEntity entity) throws RepositoryException {
    validateHouseEntity(entity);
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      StreetEntity street = entity.getStreet();

      saveStreet(street, connection);

      checkHouseExistence(entity, connection);
      Long houseId = saveHouseAndGetId(entity, connection);
      connection.commit();

      entity.setId(houseId);

      return entity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while saving HouseEntity with house number: %s",
              entity.getHouseNumber()), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public HouseEntity getById(Long id) throws RepositoryException {
    Optional<HouseEntity> entity = findById(id);
    if (entity.isPresent()) {
      return entity.get();
    } else {
      throw new EntityNotFoundException(String.format("HouseEntity with id %s not found", id));
    }
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      boolean isDeleted = deleteHouseById(id, connection);

      connection.commit();
      return isDeleted;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while deleting HouseEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public boolean delete(HouseEntity entity) throws RepositoryException {
    validateHouseEntity(entity);
    return deleteById(entity.getId());
  }

  @Override
  public HouseEntity update(HouseEntity entity) throws RepositoryException {
    validateHouseEntity(entity);
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      StreetEntity street = entity.getStreet();

      saveStreet(street, connection);

      updateHouse(entity, connection);

      connection.commit();
      return entity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while updating HouseEntity with id: %s", entity.getId()),
          e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public Optional<HouseEntity> findById(Long id) throws RepositoryException {
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      Optional<HouseEntity> houseEntities = findHouseById(id, connection);

      connection.commit();
      return houseEntities;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while finding HouseEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public List<HouseEntity> getAll() throws RepositoryException {
    Connection connection = null;
    List<HouseEntity> houseEntities;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      houseEntities = retrieveAllHouses(connection);

      connection.commit();
      return houseEntities;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException("Error occurred while retrieving all HouseEntities", e);
    } finally {
      closeConnection(connection);
    }
  }

  private boolean isHouseExists(String houseNumber, long streetId, Connection connection)
      throws SQLException {
    String selectSql = "SELECT id FROM house WHERE house_number = ? AND street_id = ?";

    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setString(1, houseNumber);
      selectStatement.setLong(2, streetId);

      try (ResultSet resultSet = selectStatement.executeQuery()) {
        return resultSet.next();
      }
    }
  }

  private StreetEntity getStreetById(Long id, Connection connection) throws SQLException {
    String sql = "SELECT * FROM street WHERE id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return streetResultSetMapper.map(resultSet);
        } else {
          throw new EntityNotFoundException(String.format("StreetEntity with id %s not found", id));
        }
      }
    }
  }

  private void saveStreet(StreetEntity street, Connection connection) throws SQLException {
    if (street == null) {
      throw new IllegalArgumentException("Street entity cannot be null");
    }

    if (!isStreetExists(street, connection)) {
      street.setId(saveStreetAndGetId(street, connection));
    } else {
      street.setId(getStreetId(street, connection));
    }
  }

  private void checkHouseExistence(HouseEntity entity, Connection connection) throws SQLException {
    if (isHouseExists(entity.getHouseNumber(), entity.getStreet().getId(), connection)) {
      throw new EntityExistsException(
          String.format("HouseEntity with house number %s already exists on street with ID %s",
              entity.getHouseNumber(), entity.getStreet().getId()));
    }
  }

  private Long saveHouseAndGetId(HouseEntity entity, Connection connection) throws SQLException {
    String insertSql = "INSERT INTO house (house_number, build_date, num_floors, type, street_id) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql,
        Statement.RETURN_GENERATED_KEYS)) {
      insertStatement.setString(1, entity.getHouseNumber());
      insertStatement.setDate(2, new Date(entity.getBuildDate().getTime()));
      insertStatement.setInt(3, entity.getNumFloors());
      insertStatement.setString(4, entity.getType());
      insertStatement.setObject(5, entity.getStreet().getId());
      int rowsAffected = insertStatement.executeUpdate();

      if (rowsAffected > 0) {
        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return generatedKeys.getLong("id");
          } else {
            throw new SQLException("Failed to retrieve generated house ID");
          }
        }
      } else {
        throw new SQLException(
            String.format("Error occurred while saving HouseEntity with house number: %s",
                entity.getHouseNumber()));
      }
    }
  }

  private void validateHouseEntity(HouseEntity house) {
    if (house == null) {
      throw new IllegalArgumentException("HouseEntity cannot be null");
    }

    house.validateNotNullFields();
    house.getStreet().validateNotNullFields();
  }

  private boolean deleteHouseById(Long id, Connection connection) throws SQLException {
    String sql = "DELETE FROM house WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      int rowsAffected = statement.executeUpdate();
      return rowsAffected > 0;
    }
  }

  private void updateHouse(HouseEntity entity, Connection connection) throws SQLException {
    String sql = "UPDATE house SET house_number = ?, build_date = ?, num_floors = ?, type = ?, street_id = ? WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, entity.getHouseNumber());
      statement.setDate(2, new Date(entity.getBuildDate().getTime()));
      statement.setInt(3, entity.getNumFloors());
      statement.setString(4, entity.getType());
      statement.setObject(5, entity.getStreet().getId());
      statement.setObject(6, entity.getId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected <= 0) {
        throw new EntityNotFoundException(
            String.format("HouseEntity with ID '%s' was not found for update operation",
                entity.getId()));
      }
    }
  }

  private Optional<HouseEntity> findHouseById(Long id, Connection connection) throws SQLException {
    String sql = "SELECT * FROM house WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          HouseEntity houseEntity = houseResultSetMapper.map(resultSet);

          Long streetId = resultSet.getLong("street_id");
          StreetEntity streetEntity = getStreetById(streetId, connection);
          houseEntity.setStreet(streetEntity);

          return Optional.of(houseEntity);
        } else {
          return Optional.empty();
        }
      }
    }
  }

  private List<HouseEntity> retrieveAllHouses(Connection connection) throws SQLException {
    List<HouseEntity> houseEntities = new ArrayList<>();
    String sql = "SELECT * FROM house";
    try (PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        HouseEntity houseEntity = houseResultSetMapper.map(resultSet);

        Long streetId = resultSet.getLong("street_id");
        StreetEntity streetEntity = getStreetById(streetId, connection);
        houseEntity.setStreet(streetEntity);

        houseEntities.add(houseEntity);
      }
    }
    return houseEntities;
  }

  private void rollbackConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.rollback();
      } catch (SQLException rollbackException) {
        rollbackException.printStackTrace();
      }
    }
  }

  private void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.setAutoCommit(true);
        connection.close();
      } catch (SQLException closeException) {
        closeException.printStackTrace();
      }
    }
  }

  private boolean isStreetExists(StreetEntity street, Connection connection) throws SQLException {
    String selectSql = "SELECT id FROM street WHERE postal_code = ?";

    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setLong(1, street.getPostalCode());

      try (ResultSet resultSet = selectStatement.executeQuery()) {
        return resultSet.next();
      }
    }
  }

  private long getStreetId(StreetEntity street, Connection connection) throws SQLException {
    String selectSql = "SELECT id FROM street WHERE postal_code = ?";

    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setLong(1, street.getPostalCode());

      try (ResultSet resultSet = selectStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong("id");
        } else {
          throw new SQLException(String.format("StreetEntity with postal code %s does not exist",
              street.getPostalCode()));
        }
      }
    }
  }

  private long saveStreetAndGetId(StreetEntity street, Connection connection) throws SQLException {
    String insertSql = "INSERT INTO street (name, postal_code) VALUES (?, ?)";
    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql,
        Statement.RETURN_GENERATED_KEYS)) {
      insertStatement.setString(1, street.getName());
      insertStatement.setLong(2, street.getPostalCode());
      insertStatement.executeUpdate();

      try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {

        if (generatedKeys.next()) {
          return generatedKeys.getLong("id");
        } else {
          throw new SQLException("Failed to retrieve generated street ID");
        }
      }
    }
  }
}
