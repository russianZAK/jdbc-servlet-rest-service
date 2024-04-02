package by.russianzak.repository.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import by.russianzak.db.ConnectionManager;
import by.russianzak.db.impl.ConnectionManagerImpl;
import by.russianzak.exception.RepositoryException;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.RoadSurfaceEntityRepository;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapper;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapperImpl;
import by.russianzak.repository.mapper.StreetResultSetMapper;
import by.russianzak.repository.mapper.StreetResultSetMapperImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class RoadSurfaceEntityRepositoryImplTest {
  private static final String INIT_SCRIPT_PATH = "db/migration/V1_1_0__create_street_house_road_surface.schema.sql";

  private final static RoadSurfaceResultSetMapper roadSurfaceResultSetMapper = new RoadSurfaceResultSetMapperImpl();
  private final static StreetResultSetMapper streetResultSetMapper = new StreetResultSetMapperImpl();
  private static RoadSurfaceEntityRepository roadSurfaceEntityRepository;
  private static ConnectionManager connectionManager;

  @Container
  public static final PostgreSQLContainer<?> container =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("my_database")
          .withUsername("postgres")
          .withPassword("root")
          .withInitScript(INIT_SCRIPT_PATH);


  @BeforeAll
  static void setUp() {
    container.start();
    connectionManager = new ConnectionManagerImpl(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    roadSurfaceEntityRepository = new RoadSurfaceEntityRepositoryImpl(roadSurfaceResultSetMapper, streetResultSetMapper,
        connectionManager);
  }

  @AfterAll
  static void tearDown() {
    container.stop();
  }

  @BeforeEach
  void clearTablesAndResetSequences() throws SQLException {
    try (Connection connection = connectionManager.getConnection();
        Statement statement = connection.createStatement()) {
      statement.executeUpdate("TRUNCATE TABLE road_surface_street CASCADE;");
      statement.executeUpdate("TRUNCATE TABLE house CASCADE;");
      statement.executeUpdate("TRUNCATE TABLE street CASCADE;");
      statement.executeUpdate("TRUNCATE TABLE road_surface CASCADE;");


      statement.executeUpdate("ALTER SEQUENCE street_id_sequence RESTART WITH 1;");
      statement.executeUpdate("ALTER SEQUENCE house_id_sequence RESTART WITH 1;");
      statement.executeUpdate("ALTER SEQUENCE road_surface_id_sequence RESTART WITH 1;");
    }
  }

  @Test
  void saveNewRoadSurfaceWithoutStreets_Success() {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.ASPHALT).setDescription("Smooth surface")
        .setFrictionCoefficient(0.8).build();

    assertDoesNotThrow(() -> {
      RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);
      assertNotNull(savedRoadSurface.getId());
    });
  }

  @Test
  void saveNewRoadSurfaceWithStreets_Success() {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder().setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Durable surface").setFrictionCoefficient(0.7).build();
    StreetEntity street1 = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();

    StreetEntity street2 = StreetEntity.builder().setName("First Avenue").setPostalCode(67890L).build();

    roadSurface.addStreet(street1);
    roadSurface.addStreet(street2);

    assertDoesNotThrow(() -> {
      RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);
      assertNotNull(savedRoadSurface.getId());
      assertEquals(2, savedRoadSurface.getStreets().size());
    });
  }

  @Test
  void saveRoadSurfaceWithExistingStreet_Success() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder().setType(TypeOfRoadSurface.GRAVEL).setDescription("Durable surface").setFrictionCoefficient(0.55).build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);
    roadSurfaceEntityRepository.save(roadSurface);

    RoadSurfaceEntity roadSurfaceWithSameStreet = RoadSurfaceEntity.builder().setType(TypeOfRoadSurface.ASPHALT).setDescription("Non durable surface").setFrictionCoefficient(0.75).build();
    roadSurfaceWithSameStreet.addStreet(street);

    assertDoesNotThrow(() -> {
      RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurfaceWithSameStreet);
      assertNotNull(savedRoadSurface.getId());
      assertEquals(street.getId(), savedRoadSurface.getStreets().get(0).getId());
    });
  }

  @Test
  void saveExistingRoadSurface_ThrowsEntityExistsException() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.GRAVEL).setDescription("Loose surface")
        .setFrictionCoefficient(0.5).build();

    roadSurfaceEntityRepository.save(roadSurface);

    assertThrows(EntityExistsException.class, () -> roadSurfaceEntityRepository.save(roadSurface));
  }

  @Test
  void saveNullRoadSurface_ThrowsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> roadSurfaceEntityRepository.save(null));
  }

  @Test
  void saveRoadSurfaceWithNullStreet_ThrowsNullPointerException() {
    StreetEntity street = null;

    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.GRAVEL).setDescription("Loose surface")
        .setFrictionCoefficient(0.5).setStreets(Collections.singletonList(street)).build();

    assertThrows(NullPointerException.class, () -> roadSurfaceEntityRepository.save(roadSurface));
  }

  @Test
  void getByIdExistingRoadSurface_Success() throws RepositoryException {
    RoadSurfaceEntity roadSurfaceToSave = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurfaceToSave);

    RoadSurfaceEntity retrievedRoadSurface = roadSurfaceEntityRepository.getById(savedRoadSurface.getId());

    assertNotNull(retrievedRoadSurface);
    assertEquals("CONCRETE", retrievedRoadSurface.getType());
  }

  @Test
  void getByIdNonExistingRoadSurface_ThrowsEntityNotFoundException() {
    Executable executable = () -> roadSurfaceEntityRepository.getById(-1L);

    assertThrows(EntityNotFoundException.class, executable);
  }

  @Test
  void deleteByIdExistingRoadSurface_Success() throws RepositoryException, SQLException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);

    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);

    assertNotNull(savedRoadSurface.getStreets().get(0).getId());

    boolean isDeleted = roadSurfaceEntityRepository.deleteById(savedRoadSurface.getId());

    assertTrue(isDeleted);

    assertThrows(EntityNotFoundException.class, () -> roadSurfaceEntityRepository.getById(savedRoadSurface.getId()));

    try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM road_surface_street WHERE road_surface_id = ?")) {
      statement.setLong(1, savedRoadSurface.getId());
      ResultSet resultSet = statement.executeQuery();
      assertFalse(resultSet.next(), "Street-road surface mappings were not deleted");
    }
  }

  @Test
  void deleteByIdNonExistingRoadSurface_ReturnsFalse() {

    boolean isDeleted = assertDoesNotThrow(() -> roadSurfaceEntityRepository.deleteById(-1L));

    assertFalse(isDeleted);
  }

  @Test
  void deleteRoadSurfaceEntity_Success() throws RepositoryException, SQLException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);

    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);

    boolean isDeleted = roadSurfaceEntityRepository.delete(savedRoadSurface);

    assertTrue(isDeleted);

    assertThrows(EntityNotFoundException.class, () -> roadSurfaceEntityRepository.getById(savedRoadSurface.getId()));

    try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM road_surface_street WHERE road_surface_id = ?")) {
      statement.setLong(1, savedRoadSurface.getId());
      ResultSet resultSet = statement.executeQuery();
      assertFalse(resultSet.next(), "Street-road surface mappings were not deleted");
    }
  }

  @Test
  void updateRoadSurfaceEntity_Success() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);

    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);

    savedRoadSurface.setDescription("Updated description");
    savedRoadSurface.setFrictionCoefficient(0.7);

    roadSurfaceEntityRepository.update(savedRoadSurface);

    RoadSurfaceEntity updatedRoadSurface = roadSurfaceEntityRepository.getById(savedRoadSurface.getId());

    assertEquals("Updated description", updatedRoadSurface.getDescription());
    assertEquals(0.7, updatedRoadSurface.getFrictionCoefficient());
  }

  @Test
  void updateNonExistentRoadSurfaceEntity_ThrowsEntityNotFoundException() {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setId(100L)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();

    assertThrows(EntityNotFoundException.class, () -> roadSurfaceEntityRepository.update(roadSurface));
  }

  @Test
  void updateRoadSurfaceEntityWithNullValues_ThrowsIllegalArgumentException() throws RepositoryException, SQLException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);

    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);

    savedRoadSurface.setType(null);
    savedRoadSurface.setDescription(null);
    savedRoadSurface.setFrictionCoefficient(0.0);
    assertThrows(IllegalArgumentException.class, () -> roadSurfaceEntityRepository.update(roadSurface));
  }

  @Test
  void updateRoadSurfaceEntityWithTypeThatAlreadyExists_ThrowsRepositoryException() throws RepositoryException {
    RoadSurfaceEntity roadSurface1 = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street1 = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface1.addStreet(street1);
    roadSurfaceEntityRepository.save(roadSurface1);

    RoadSurfaceEntity roadSurface2 = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.ASPHALT)
        .setDescription("Durable surface")
        .setFrictionCoefficient(0.7)
        .build();
    StreetEntity street2 = StreetEntity.builder().setName("First Avenue").setPostalCode(67890L).build();
    roadSurface2.addStreet(street2);
    roadSurfaceEntityRepository.save(roadSurface2);

    roadSurface2.setType(TypeOfRoadSurface.CONCRETE);
    assertThrows(RepositoryException.class, () -> roadSurfaceEntityRepository.update(roadSurface2));
  }

  @Test
  void findByIdExistingRoadSurface_ReturnsRoadSurfaceEntity() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    StreetEntity street = StreetEntity.builder().setName("Main Street").setPostalCode(12345L).build();
    roadSurface.addStreet(street);
    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityRepository.save(roadSurface);

    Optional<RoadSurfaceEntity> foundRoadSurfaceOptional = roadSurfaceEntityRepository.findById(savedRoadSurface.getId());

    assertTrue(foundRoadSurfaceOptional.isPresent());
    RoadSurfaceEntity foundRoadSurface = foundRoadSurfaceOptional.get();
    assertEquals(savedRoadSurface.getId(), foundRoadSurface.getId());
    assertEquals(savedRoadSurface.getType(), foundRoadSurface.getType());
    assertEquals(savedRoadSurface.getDescription(), foundRoadSurface.getDescription());
    assertEquals(savedRoadSurface.getFrictionCoefficient(), foundRoadSurface.getFrictionCoefficient());
    assertEquals(savedRoadSurface.getStreets().size(), foundRoadSurface.getStreets().size());
    assertEquals(savedRoadSurface.getStreets().get(0).getId(), foundRoadSurface.getStreets().get(0).getId());
  }

  @Test
  void findByIdNonExistingRoadSurface_ReturnsEmptyOptional() throws RepositoryException {
    Long nonExistentId = Long.MAX_VALUE;
    Optional<RoadSurfaceEntity> foundRoadSurfaceOptional = roadSurfaceEntityRepository.findById(nonExistentId);

    assertTrue(foundRoadSurfaceOptional.isEmpty());
  }

  @Test
  void getAllRoadSurfaces_HasRoadSurfaces_ReturnsListOfRoadSurfaceEntities() throws RepositoryException {
    RoadSurfaceEntity roadSurface1 = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();
    RoadSurfaceEntity roadSurface2 = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.ASPHALT)
        .setDescription("Durable surface")
        .setFrictionCoefficient(0.7)
        .build();
    roadSurfaceEntityRepository.save(roadSurface1);
    roadSurfaceEntityRepository.save(roadSurface2);

    List<RoadSurfaceEntity> roadSurfaces = roadSurfaceEntityRepository.getAll();

    assertEquals(2, roadSurfaces.size());

    RoadSurfaceEntity retrievedRoadSurface1 = roadSurfaces.get(0);
    assertEquals(roadSurface1.getType(), retrievedRoadSurface1.getType());
    assertEquals(roadSurface1.getDescription(), retrievedRoadSurface1.getDescription());
    assertEquals(roadSurface1.getFrictionCoefficient(), retrievedRoadSurface1.getFrictionCoefficient());

    RoadSurfaceEntity retrievedRoadSurface2 = roadSurfaces.get(1);
    assertEquals(roadSurface2.getType(), retrievedRoadSurface2.getType());
    assertEquals(roadSurface2.getDescription(), retrievedRoadSurface2.getDescription());
    assertEquals(roadSurface2.getFrictionCoefficient(), retrievedRoadSurface2.getFrictionCoefficient());
  }

  @Test
  void getAllRoadSurfaces_NoRoadSurfaces_ReturnsEmptyList() throws RepositoryException {
    List<RoadSurfaceEntity> roadSurfaces = roadSurfaceEntityRepository.getAll();

    assertTrue(roadSurfaces.isEmpty());
  }
}
