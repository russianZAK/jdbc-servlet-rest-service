package by.russianzak.repository.impl;

import by.russianzak.db.ConnectionManager;
import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.StreetEntityRepository;
import by.russianzak.repository.mapper.HouseResultSetMapper;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StreetEntityRepositoryImpl implements StreetEntityRepository {

  private final HouseResultSetMapper houseResultSetMapper;
  private final StreetResultSetMapper streetResultSetMapper;
  private final RoadSurfaceResultSetMapper roadSurfaceResultSetMapper;
  private final ConnectionManager connectionManager;

  public StreetEntityRepositoryImpl(HouseResultSetMapper houseResultSetMapper,
      StreetResultSetMapper streetResultSetMapper,
      RoadSurfaceResultSetMapper roadSurfaceResultSetMapper, ConnectionManager connectionManager) {
    this.houseResultSetMapper = houseResultSetMapper;
    this.streetResultSetMapper = streetResultSetMapper;
    this.roadSurfaceResultSetMapper = roadSurfaceResultSetMapper;
    this.connectionManager = connectionManager;
  }

  @Override
  public StreetEntity save(StreetEntity entity) throws RepositoryException {
    validateStreetEntity(entity);

    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      checkStreetExistence(entity, connection);

      Long id = saveStreetAndGetId(entity, connection);
      entity.setId(id);

      saveHouses(entity.getHouses(), entity.getId(), connection);

      saveRoadSurfacesAndRelations(entity, connection);

      connection.commit();
      return entity;

    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while saving StreetEntity with postal code: %s",
              entity.getPostalCode()), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public StreetEntity getById(Long id) throws RepositoryException {
    Optional<StreetEntity> entity = findById(id);
    if (entity.isPresent()) {
      return entity.get();
    } else {
      throw new EntityNotFoundException(String.format("StreetEntity with id %s not found", id));
    }
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      deleteHousesByStreetId(id, connection);

      deleteRoadSurfaceStreetMappingsByStreetId(id, connection);

      boolean isDeleted = deleteStreetById(id, connection);

      connection.commit();

      return isDeleted;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while deleting StreetEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public boolean delete(StreetEntity entity) throws RepositoryException {
    validateStreetEntity(entity);
    return deleteById(entity.getId());
  }

  @Override
  public StreetEntity update(StreetEntity entity) throws RepositoryException {
    validateStreetEntity(entity);

    Connection connection = null;
    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      updateStreet(entity, connection);
      entity.setRoadSurfaces(getRoadSurfacesByStreetId(entity.getId(), connection));
      entity.setHouses(getHousesByStreet(entity, connection));
      connection.commit();

      return entity;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while updating StreetEntity with id: %s", entity.getId()),
          e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public Optional<StreetEntity> findById(Long id) throws RepositoryException {
    Connection connection = null;

    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      Optional<StreetEntity> streetEntity = findStreetById(id, connection);

      connection.commit();

      return streetEntity;

    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException(
          String.format("Error occurred while finding StreetEntity with id: %s", id), e);
    } finally {
      closeConnection(connection);
    }
  }

  @Override
  public List<StreetEntity> getAll() throws RepositoryException {
    List<StreetEntity> streetEntities;
    Connection connection = null;

    try {
      connection = connectionManager.getConnection();
      connection.setAutoCommit(false);

      streetEntities = retrieveAllStreets(connection);

      connection.commit();

      return streetEntities;
    } catch (SQLException e) {
      rollbackConnection(connection);
      throw new RepositoryException("Error occurred while retrieving all StreetEntity", e);
    } finally {
      closeConnection(connection);
    }
  }

  private List<StreetEntity> retrieveAllStreets(Connection connection) throws SQLException {
    List<StreetEntity> streetEntities = new ArrayList<>();
    String sql = "SELECT * FROM street";
    try (PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        StreetEntity streetEntity = streetResultSetMapper.map(resultSet);
        streetEntity.setHouses(getHousesByStreet(streetEntity, connection));
        streetEntity.setRoadSurfaces(getRoadSurfacesByStreetId(streetEntity.getId(), connection));
        streetEntities.add(streetEntity);
      }
    }
    return streetEntities;
  }

  private Set<HouseEntity> getHousesByStreet(StreetEntity streetEntity, Connection connection)
      throws SQLException {
    Set<HouseEntity> houseEntities = new HashSet<>();
    String sql = "SELECT * FROM house WHERE street_id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, streetEntity.getId());
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        HouseEntity houseEntity = houseResultSetMapper.map(resultSet);
        houseEntity.setStreet(streetEntity);
        houseEntities.add(houseEntity);
      }
    }
    return houseEntities;
  }

  private boolean isStreetExists(Long postalCode, Connection connection) throws SQLException {
    String selectSql = "SELECT id FROM street WHERE postal_code = ?";
    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setLong(1, postalCode);
      try (ResultSet resultSet = selectStatement.executeQuery()) {
        return resultSet.next();
      }
    }
  }

  private void checkStreetExistence(
      StreetEntity entity, Connection connection) throws SQLException {
    if (isStreetExists(entity.getPostalCode(), connection)) {
      throw new EntityExistsException(
          String.format("StreetEntity with postal code %s already exists", entity.getPostalCode()));

    }
  }

  private Long saveStreetAndGetId(StreetEntity street, Connection connection) throws SQLException {
    String query = "INSERT INTO street (name, postal_code) VALUES (?, ?)";
    try (PreparedStatement insertStatement = connection.prepareStatement(query,
        Statement.RETURN_GENERATED_KEYS)) {
      insertStatement.setString(1, street.getName());
      insertStatement.setLong(2, street.getPostalCode());
      int rowsAffected = insertStatement.executeUpdate();

      if (rowsAffected > 0) {
        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return generatedKeys.getLong("id");
          } else {
            throw new SQLException("Failed to retrieve generated street ID");
          }
        }
      } else {
        throw new SQLException(
            String.format("Error occurred while saving StreetEntity with postal code: %s",
                street.getPostalCode()));
      }
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

  private void validateStreetEntity(StreetEntity streetEntity) {
    if (streetEntity == null) {
      throw new IllegalArgumentException("HouseEntity cannot be null");
    }

    streetEntity.validateNotNullFields();

    for (HouseEntity house: streetEntity.getHouses()) {
      house.validateNotNullFields();
    }

    for (RoadSurfaceEntity roadSurface: streetEntity.getRoadSurfaces()) {
      roadSurface.validateNotNullFields();
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

  private Long saveRoadSurfaceAndGetId(RoadSurfaceEntity entity, Connection connection)
      throws SQLException {
    String insertSql = "INSERT INTO road_surface (type, description, friction_coefficient) VALUES (?, ?, ?)";
    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql,
        Statement.RETURN_GENERATED_KEYS)) {
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
        throw new SQLException(
            String.format("Error occurred while saving RoadSurfaceEntity with type: %s",
                entity.getType()));
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

  private Set<RoadSurfaceEntity> getRoadSurfacesByStreetId(Long streetId, Connection connection)
      throws SQLException {
    Set<RoadSurfaceEntity> roadSurfaces = new HashSet<>();
    String sql = "SELECT road_surface.* FROM road_surface " +
        "INNER JOIN road_surface_street ON road_surface.id = road_surface_street.road_surface_id " +
        "WHERE road_surface_street.street_id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, streetId);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        RoadSurfaceEntity roadSurfaceEntity = roadSurfaceResultSetMapper.map(resultSet);
        roadSurfaces.add(roadSurfaceEntity);
      }
    }
    return roadSurfaces;
  }

  private void saveHouses(List<HouseEntity> houses, Long streetId, Connection connection)
      throws SQLException {
    for (HouseEntity house : houses) {
      if (!isHouseExists(house.getHouseNumber(), streetId, connection)) {
        Long id = saveHouseAndGetId(house, connection);
        house.setId(id);
      }
    }
  }

  private void saveRoadSurfacesAndRelations(StreetEntity street, Connection connection)
      throws SQLException {
    for (RoadSurfaceEntity roadSurface : street.getRoadSurfaces()) {
      saveRoadSurface(roadSurface, connection);
      saveStreetRoadSurfaceRelation(roadSurface.getId(), street.getId(), connection);
    }
  }

  private void saveRoadSurface(RoadSurfaceEntity entity, Connection connection)
      throws SQLException {
    if (entity != null) {
      if (!isRoadSurfaceExists(entity, connection)) {
        entity.setId(saveRoadSurfaceAndGetId(entity, connection));
      } else {
        entity.setId(getRoadSurfaceId(entity, connection));
      }
    }
  }

  private void deleteHousesByStreetId(Long streetId, Connection connection) throws SQLException {
    String sql = "DELETE FROM house WHERE street_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, streetId);
      statement.executeUpdate();
    }
  }

  private void deleteRoadSurfaceStreetMappingsByStreetId(Long streetId, Connection connection) throws SQLException {
    String sql = "DELETE FROM road_surface_street WHERE street_id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, streetId);
      statement.executeUpdate();
    }
  }

  private long getRoadSurfaceId(RoadSurfaceEntity entity, Connection connection)
      throws SQLException {
    String selectSql = "SELECT id FROM road_surface WHERE type = ?";

    try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
      selectStatement.setString(1, entity.getType());

      try (ResultSet resultSet = selectStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong("id");
        } else {
          throw new SQLException(
              String.format("RoadSurfaceEntity with type %s does not exist.", entity.getType()));
        }
      }
    }
  }

  private boolean deleteStreetById(Long id, Connection connection) throws SQLException {
    String sql = "DELETE FROM street WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);
      int rowsAffected = statement.executeUpdate();
      return rowsAffected > 0;
    }
  }

  private void updateStreet(StreetEntity entity, Connection connection) throws SQLException {
    String sql = "UPDATE street SET name = ?, postal_code = ? WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, entity.getName());
      statement.setLong(2, entity.getPostalCode());
      statement.setObject(3, entity.getId());
      int rowsAffected = statement.executeUpdate();

      if (rowsAffected <= 0) {
        throw new EntityNotFoundException(
            String.format("RoadSurfaceEntity with ID '%s' was not found for update operation",
                entity.getId()));
      }
      }
  }



  private Optional<StreetEntity> findStreetById(Long id, Connection connection)
      throws SQLException {
    String sql = "SELECT * FROM street WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setObject(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          StreetEntity streetEntity = streetResultSetMapper.map(resultSet);

          streetEntity.setHouses(getHousesByStreet(streetEntity, connection));
          streetEntity.setRoadSurfaces(getRoadSurfacesByStreetId(streetEntity.getId(), connection));

          return Optional.of(streetEntity);
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
