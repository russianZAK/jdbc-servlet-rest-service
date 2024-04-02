package by.russianzak.repository.impl;

import by.russianzak.db.ConnectionManager;
import by.russianzak.exception.RepositoryException;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.RoadSurfaceEntityRepository;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapper;
import by.russianzak.repository.mapper.StreetResultSetMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RoadSurfaceEntityRepositoryImpl implements RoadSurfaceEntityRepository {

  private final RoadSurfaceResultSetMapper roadSurfaceResultSetMapper;
  private final StreetResultSetMapper streetResultSetMapper;
  private final ConnectionManager connectionManager;

  public RoadSurfaceEntityRepositoryImpl(RoadSurfaceResultSetMapper roadSurfaceResultSetMapper,
      StreetResultSetMapper streetResultSetMapper, ConnectionManager connectionManager) {
    this.roadSurfaceResultSetMapper = roadSurfaceResultSetMapper;
    this.streetResultSetMapper = streetResultSetMapper;
    this.connectionManager = connectionManager;
  }

  @Override
  public RoadSurfaceEntity save(RoadSurfaceEntity entity) throws RepositoryException {
    validateRoadSurfaceEntity(entity);
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      checkRoadSurfaceExistence(entity, connection);

      Long id = saveRoadSurfaceAndGetId(entity, connection);
      entity.setId(id);

      saveStreetsAndRelations(entity, connection);

      connection.commit();
      return entity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while saving RoadSurfaceEntity with type: %s", entity.getType()), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public RoadSurfaceEntity getById(Long id) throws RepositoryException {
    Optional<RoadSurfaceEntity> entity = findById(id);
    if (entity.isPresent()) {
      return entity.get();
    } else {
      throw new EntityNotFoundException(
          String.format("RoadSurfaceEntity with id %s not found", id));
    }
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      deleteRoadSurfaceStreetMappingsByRoadSurfaceId(id, connection);
      boolean isDeleted = deleteRoadSurfaceById(id, connection);

      connection.commit();
      return isDeleted;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(String.format("Error occurred while deleting RoadSurfaceEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public boolean delete(RoadSurfaceEntity entity) throws RepositoryException {
    validateRoadSurfaceEntity(entity);
    return deleteById(entity.getId());
  }

  @Override
  public RoadSurfaceEntity update(RoadSurfaceEntity entity) throws RepositoryException {
    validateRoadSurfaceEntity(entity);
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      updateRoadSurface(entity, connection);
      entity.setStreets(getStreetsByRoadSurfaceId(entity.getId(), connection));
      connection.commit();
      return entity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(String.format("Error occurred while updating RoadSurfaceEntity with id: %s", entity.getId()), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public Optional<RoadSurfaceEntity> findById(Long id) throws RepositoryException {
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      Optional<RoadSurfaceEntity> roadSurfaceEntity = findRoadSurfaceById(id, connection);

      connection.commit();
      return roadSurfaceEntity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(String.format("Error occurred while finding RoadSurfaceEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public List<RoadSurfaceEntity> getAll() throws RepositoryException {
    List<RoadSurfaceEntity> roadSurfaceEntities;

    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      roadSurfaceEntities = retrieveAllRoadSurfaces(connection);

      connection.commit();

      return roadSurfaceEntities;

    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException("Error occurred while retrieving all RoadSurfaceEntity", e);
    } finally {
      closeConnection(connection);
    }
  }

  private void deleteRoadSurfaceStreetMappingsByRoadSurfaceId(Long roadSurfaceId, Connection connection) throws SQLException {
    String sql = "DELETE FROM road_surface_street WHERE road_surface_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, roadSurfaceId);
      statement.executeUpdate();
    }
  }

  private Set<StreetEntity> getStreetsByRoadSurfaceId(Long roadSurfaceId, Connection connection)
      throws RepositoryException {
    String streetSql = "SELECT street.* FROM street " +
        "INNER JOIN road_surface_street ON road_surface_street.street_id = street.id " +
        "WHERE road_surface_street.road_surface_id = ?";
    Set<StreetEntity> streets = new HashSet<>();

    try (PreparedStatement streetStatement = connection.prepareStatement(streetSql)) {
      streetStatement.setObject(1, roadSurfaceId);
      ResultSet streetResultSet = streetStatement.executeQuery();

      while (streetResultSet.next()) {
        StreetEntity streetEntity = streetResultSetMapper.map(streetResultSet);
        streets.add(streetEntity);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to get streets by road surface ID", e);
    }

    return streets;
  }

  private Long saveRoadSurfaceAndGetId(RoadSurfaceEntity entity, Connection connection) throws SQLException {
    String insertSql = "INSERT INTO road_surface (type, description, friction_coefficient) VALUES (?, ?, ?)";
    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
      insertStatement.setString(1, entity.getType());
      insertStatement.setString(2, entity.getDescription());
      insertStatement.setDouble(3, entity.getFrictionCoefficient());
      int rowsAffected = insertStatement.executeUpdate();
      if (rowsAffected > 0) {
        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {

          if (generatedKeys.next()) {
            return generatedKeys.getLong("id");
          } else {
            throw new SQLException("Failed to retrieve generated road surface ID");
          }
        }
      } else {
        throw new SQLException(String.format("Error occurred while saving RoadSurfaceEntity with type: %s", entity.getType()));
      }
    }
  }


  private void saveStreetRoadSurfaceRelation(Long roadSurfaceId, Long streetId,
      Connection connection) throws SQLException {
    String sql = "INSERT INTO road_surface_street (road_surface_id, street_id) VALUES (?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, roadSurfaceId);
      statement.setObject(2, streetId);
      statement.executeUpdate();
    }
  }

  private void checkRoadSurfaceExistence(
      RoadSurfaceEntity entity, Connection connection) throws SQLException {
    if (isRoadSurfaceExists(entity, connection)) {
      throw new EntityExistsException(
          String.format("RoadSurfaceEntity with type %s already exists", entity.getType()));
    }
  }

  private boolean isRoadSurfaceExists(RoadSurfaceEntity roadSurfaceEntity, Connection connection)
      throws SQLException {
    String selectSql = "SELECT id FROM road_surface WHERE type = ?";
    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setString(1, roadSurfaceEntity.getType());
      try (ResultSet resultSet = selectStatement.executeQuery()) {
        return resultSet.next();
      }
    }
  }

  private void saveStreetsAndRelations(RoadSurfaceEntity entity, Connection connection)
      throws SQLException {
    for (StreetEntity street : entity.getStreets()) {
      saveStreet(street, connection);
      if (street != null) {
        saveStreetRoadSurfaceRelation(entity.getId(), street.getId(), connection);
      }
    }
  }

  private void saveStreet(StreetEntity street, Connection connection) throws SQLException {
    if (street != null) {
      if (!isStreetExists(street, connection)) {
        street.setId(saveStreetAndGetId(street, connection));
      } else {
        street.setId(getStreetId(street, connection));
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

  private long getStreetId(StreetEntity street, Connection connection) throws SQLException {
    String selectSql = "SELECT id FROM street WHERE postal_code = ?";

    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setLong(1, street.getPostalCode());

      try (ResultSet resultSet = selectStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong("id");
        } else {
          throw new SQLException(
              String.format("StreetEntity with postal code %s does not exist", street.getPostalCode()));
        }
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

  private boolean deleteRoadSurfaceById(Long id, Connection connection) throws SQLException {
    String sql = "DELETE FROM road_surface WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      int rowsAffected = statement.executeUpdate();
      return rowsAffected > 0;
    }
  }

  private void updateRoadSurface(RoadSurfaceEntity entity, Connection connection) throws SQLException {
    String sql = "UPDATE road_surface SET type = ?, description = ?, friction_coefficient = ? WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, entity.getType());
      statement.setString(2, entity.getDescription());
      statement.setDouble(3, entity.getFrictionCoefficient());
      statement.setObject(4, entity.getId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected <= 0) {
        throw new EntityNotFoundException(
            String.format("RoadSurfaceEntity with ID '%s' was not found for update operation",
                entity.getId()));
      }
    }
  }

  private List<RoadSurfaceEntity> retrieveAllRoadSurfaces(Connection connection)
      throws SQLException, RepositoryException {
    List<RoadSurfaceEntity> roadSurfaceEntities = new ArrayList<>();
    String sql = "SELECT * FROM road_surface";
    try (PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        RoadSurfaceEntity roadSurfaceEntity = roadSurfaceResultSetMapper.map(resultSet);
        Set<StreetEntity> streets = getStreetsByRoadSurfaceId(roadSurfaceEntity.getId(), connection);
        roadSurfaceEntity.setStreets(streets);
        roadSurfaceEntities.add(roadSurfaceEntity);
      }
    }
    return roadSurfaceEntities;
  }

  private void validateRoadSurfaceEntity(RoadSurfaceEntity roadSurface) {
    if (roadSurface == null) {
      throw new IllegalArgumentException("RoadSurfaceEntity cannot be null");
    }

    roadSurface.validateNotNullFields();

    for (StreetEntity street: roadSurface.getStreets()) {
      street.validateNotNullFields();
    }
  }

  private Optional<RoadSurfaceEntity> findRoadSurfaceById(Long id, Connection connection)
      throws SQLException, RepositoryException {
    String sql = "SELECT * FROM road_surface WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          RoadSurfaceEntity roadSurfaceEntity = roadSurfaceResultSetMapper.map(resultSet);
          Set<StreetEntity> streets = getStreetsByRoadSurfaceId(id, connection);
          roadSurfaceEntity.setStreets(streets);
          return Optional.of(roadSurfaceEntity);
        } else {
          return Optional.empty();
        }
      }
    }
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
}
