package by.russianzak.repository.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import by.russianzak.db.impl.ConnectionManagerImpl;
import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.model.HouseEntity.TypeOfBuilding;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.StreetEntityRepository;
import by.russianzak.repository.mapper.HouseResultSetMapper;
import by.russianzak.repository.mapper.HouseResultSetMapperImpl;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class StreetEntityRepositoryImplTest {
  private static final String INIT_SCRIPT_PATH = "db/migration/V1_1_0__create_street_house_road_surface.schema.sql";

  private final static HouseResultSetMapper houseResultSetMapper = new HouseResultSetMapperImpl();
  private final static StreetResultSetMapper streetResultSetMapper = new StreetResultSetMapperImpl();
  private final static RoadSurfaceResultSetMapper roadSurfaceResultSetMapper = new RoadSurfaceResultSetMapperImpl();
  private static StreetEntityRepository streetEntityRepository;
  private static ConnectionManagerImpl connectionManager;

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
    streetEntityRepository = new StreetEntityRepositoryImpl(houseResultSetMapper, streetResultSetMapper, roadSurfaceResultSetMapper,
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
  void saveStreet_NewStreet_Success() {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();

    StreetEntity savedStreet = assertDoesNotThrow(() -> streetEntityRepository.save(street));

    assertNotNull(savedStreet.getId());
    assertEquals(street.getName(), savedStreet.getName());
    assertEquals(street.getPostalCode(), savedStreet.getPostalCode());
  }

  @Test
  void saveStreet_ExistingPostalCode_EntityExistsExceptionThrown() {
    StreetEntity street1 = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();

    StreetEntity street2 = StreetEntity.builder()
        .setName("First Avenue")
        .setPostalCode(12345L)
        .build();

    assertDoesNotThrow(() -> streetEntityRepository.save(street1));

    assertThrows(EntityExistsException.class, () -> streetEntityRepository.save(street2));
  }

  @Test
  void saveStreet_WithHouses_Success() {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity houseEntity2 = HouseEntity.builder().setHouseNumber("35/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();

    street.setHouses(Set.of(houseEntity, houseEntity2));

    StreetEntity savedStreet = assertDoesNotThrow(() -> streetEntityRepository.save(street));

    assertNotNull(savedStreet.getId());
    assertEquals(2, savedStreet.getHouses().size());
  }

  @Test
  void saveStreet_WithoutName_IllegalArgumentExceptionThrown() {
    StreetEntity street = StreetEntity.builder()
        .setPostalCode(12345L)
        .build();

    assertThrows(IllegalArgumentException.class, () -> streetEntityRepository.save(street));
  }

  @Test
  void saveStreet_WithoutPostalCode_IllegalArgumentExceptionThrown() {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .build();

    assertThrows(IllegalArgumentException.class, () -> streetEntityRepository.save(street));
  }


  @Test
  void saveStreet_WithDuplicateHouseNumber_IllegalArgumentExceptionThrown() {
    StreetEntity firstStreet = StreetEntity.builder()
        .setName("First Street")
        .setPostalCode(11111L)
        .build();

    HouseEntity firstHouse = HouseEntity.builder()
        .setHouseNumber("1")
        .setNumFloors(2)
        .setType(TypeOfBuilding.RESIDENTIAL)
        .setBuildDate(new Date())
        .setStreet(firstStreet)
        .build();

    HouseEntity secondHouse = HouseEntity.builder()
        .setHouseNumber("1")
        .setNumFloors(3)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(firstStreet)
        .build();


    assertThrows(IllegalArgumentException.class, () -> firstStreet.setHouses(Set.of(firstHouse, secondHouse)));
  }

  @Test
  void getById_ExistingStreet_Successful() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    streetEntityRepository.save(street);

    Long streetId = street.getId();

    StreetEntity retrievedStreet = streetEntityRepository.getById(streetId);

    assertEquals(street.getName(), retrievedStreet.getName());
    assertEquals(street.getPostalCode(), retrievedStreet.getPostalCode());
  }

  @Test
  void getById_NonExistingStreet_EntityNotFoundExceptionThrown() {
    Long nonExistentId = Long.MAX_VALUE;

    assertThrows(EntityNotFoundException.class, () -> streetEntityRepository.getById(nonExistentId));
  }

  @Test
  void deleteById_ExistingStreet_Successful() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    streetEntityRepository.save(street);
    Long streetId = street.getId();

    assertNotNull(streetEntityRepository.getById(streetId));

    boolean isDeleted = streetEntityRepository.deleteById(streetId);

    assertTrue(isDeleted);
    assertThrows(EntityNotFoundException.class, () -> streetEntityRepository.getById(streetId));
  }

  @Test
  void deleteById_NonExistingStreet_ReturnFalse() throws RepositoryException {
    Long nonExistentId = Long.MAX_VALUE;

    boolean isDeleted = streetEntityRepository.deleteById(nonExistentId);

    assertFalse(isDeleted);
  }

  @Test
  void deleteById_StreetWithHouses_Successful() throws RepositoryException, SQLException {
    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();

    HouseEntity house1 = HouseEntity.builder()
        .setHouseNumber("1")
        .setBuildDate(new Date())
        .setNumFloors(2)
        .setType(HouseEntity.TypeOfBuilding.RESIDENTIAL).setStreet(street)
        .build();

    HouseEntity house2 = HouseEntity.builder()
        .setHouseNumber("2")
        .setBuildDate(new Date())
        .setNumFloors(3)
        .setType(HouseEntity.TypeOfBuilding.COMMERCIAL).setStreet(street)
        .build();
    street.setHouses(Set.of(house1, house2));

    streetEntityRepository.save(street);
    Long streetId = street.getId();

    boolean isDeleted = streetEntityRepository.deleteById(streetId);

    assertTrue(isDeleted);
    assertThrows(EntityNotFoundException.class, () -> streetEntityRepository.getById(streetId));
    assertTrue(getHousesByStreetId(streetId).isEmpty());
  }

  @Test
  void deleteById_StreetWithRoadSurfaces_Successful() throws RepositoryException, SQLException {
    RoadSurfaceEntity roadSurface1 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();

    RoadSurfaceEntity roadSurface2 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.ASPHALT)
        .setDescription("Rough surface")
        .setFrictionCoefficient(0.7)
        .build();

    StreetEntity street = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(12345L)
        .setRoadSurfaces(List.of(roadSurface1, roadSurface2))
        .build();
    streetEntityRepository.save(street);
    Long streetId = street.getId();

    boolean isDeleted = streetEntityRepository.deleteById(streetId);

    assertTrue(isDeleted);
    assertThrows(EntityNotFoundException.class, () -> streetEntityRepository.getById(streetId));
    assertTrue(getRoadSurfacesByStreetId(streetId).isEmpty());
  }

  @Test
  void deleteStreetEntity_ExistingStreet_Successful() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10001L)
        .build();
    streetEntityRepository.save(street);

    assertNotNull(streetEntityRepository.getById(street.getId()));

    assertTrue(streetEntityRepository.delete(street));

    assertThrows(EntityNotFoundException.class, () -> streetEntityRepository.getById(street.getId()));
  }

  @Test
  void updateStreetEntity_Successful() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10001L)
        .build();
    streetEntityRepository.save(street);

    street.setName("5th Avenue");
    street.setPostalCode(10002L);

    streetEntityRepository.update(street);

    StreetEntity updatedStreet = streetEntityRepository.getById(street.getId());

    assertEquals("5th Avenue", updatedStreet.getName());
    assertEquals(10002L, updatedStreet.getPostalCode());
  }

  @Test
  void updateStreetWithSamePostalCode_SamePostalCode_RepositoryExceptionThrown() throws RepositoryException {
    StreetEntity street1 = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10001L)
        .build();
    streetEntityRepository.save(street1);

    StreetEntity street2 = StreetEntity.builder()
        .setName("5th Avenue")
        .setPostalCode(10002L)
        .build();
    streetEntityRepository.save(street2);

    street1.setPostalCode(10002L);
    assertThrows(RepositoryException.class, () -> streetEntityRepository.update(street1));
  }

  @Test
  void findById_ExistingStreet_Successful() throws RepositoryException {
    StreetEntity streetToSave = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10001L)
        .build();
    StreetEntity savedStreet = streetEntityRepository.save(streetToSave);

    Long streetId = savedStreet.getId();

    Optional<StreetEntity> foundStreetOptional = streetEntityRepository.findById(streetId);

    assertTrue(foundStreetOptional.isPresent());
    StreetEntity foundStreet = foundStreetOptional.get();
    assertEquals(savedStreet.getId(), foundStreet.getId());
    assertEquals(savedStreet.getName(), foundStreet.getName());
    assertEquals(savedStreet.getPostalCode(), foundStreet.getPostalCode());
  }

  @Test
  void findById_NonExistingStreet_Successful() throws RepositoryException {
    Long nonExistingStreetId = 999L;

    Optional<StreetEntity> foundStreetOptional = streetEntityRepository.findById(nonExistingStreetId);

    assertFalse(foundStreetOptional.isPresent());
  }

  @Test
  void findById_ExistingStreetWithHousesAndRoadSurfaces_ReturnsStreetEntityWithHousesAndRoadSurfaces() throws RepositoryException {
    StreetEntity streetToSave = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10001L)
        .build();

    HouseEntity house1 = HouseEntity.builder()
        .setHouseNumber("1")
        .setNumFloors(2)
        .setType(TypeOfBuilding.RESIDENTIAL)
        .setBuildDate(new Date())
        .setStreet(streetToSave)
        .build();

    HouseEntity house2 = HouseEntity.builder()
        .setHouseNumber("2")
        .setNumFloors(3)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(streetToSave)
        .build();

    RoadSurfaceEntity roadSurface1 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();

    RoadSurfaceEntity roadSurface2 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.ASPHALT)
        .setDescription("Rough surface")
        .setFrictionCoefficient(0.7)
        .build();

    streetToSave.setHouses(Set.of(house1, house2));
    streetToSave.setRoadSurfaces(Set.of(roadSurface1, roadSurface2));
    StreetEntity savedStreet = streetEntityRepository.save(streetToSave);
    Long streetId = savedStreet.getId();

    Optional<StreetEntity> foundStreetOptional = streetEntityRepository.findById(streetId);

    assertTrue(foundStreetOptional.isPresent());
    StreetEntity foundStreet = foundStreetOptional.get();

    assertEquals(2, foundStreet.getHouses().size());
    assertEquals(2, foundStreet.getRoadSurfaces().size());
  }

  @Test
  void getAllStreetsWithHousesAndRoadSurfaces_ReturnsListOfStreetEntitiesWithHousesAndRoadSurfaces() throws RepositoryException {
    StreetEntity street1 = StreetEntity.builder()
        .setName("Main Street")
        .setPostalCode(10001L)
        .build();

    StreetEntity street2 = StreetEntity.builder()
        .setName("Broadway")
        .setPostalCode(10002L)
        .build();

    HouseEntity house1 = HouseEntity.builder()
        .setHouseNumber("1A")
        .setNumFloors(2)
        .setType(HouseEntity.TypeOfBuilding.RESIDENTIAL)
        .setBuildDate(new Date())
        .setStreet(street1)
        .build();

    HouseEntity house2 = HouseEntity.builder()
        .setHouseNumber("2B")
        .setNumFloors(3)
        .setType(HouseEntity.TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(street1)
        .build();

    street1.setHouses(Set.of(house1, house2));

    HouseEntity house3 = HouseEntity.builder()
        .setHouseNumber("10")
        .setNumFloors(4)
        .setType(HouseEntity.TypeOfBuilding.RESIDENTIAL)
        .setBuildDate(new Date())
        .setStreet(street2)
        .build();

    street2.setHouses(Set.of(house3));

    RoadSurfaceEntity roadSurface1 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.CONCRETE)
        .setDescription("Smooth surface")
        .setFrictionCoefficient(0.8)
        .build();

    RoadSurfaceEntity roadSurface2 = RoadSurfaceEntity.builder()
        .setType(RoadSurfaceEntity.TypeOfRoadSurface.ASPHALT)
        .setDescription("Rough surface")
        .setFrictionCoefficient(0.7)
        .build();

    street1.setRoadSurfaces(Set.of(roadSurface1));
    street2.setRoadSurfaces(Set.of(roadSurface2));

    streetEntityRepository.save(street1);
    streetEntityRepository.save(street2);

    List<StreetEntity> allStreets = streetEntityRepository.getAll();

    assertFalse(allStreets.isEmpty());

    assertEquals(2, allStreets.size());

    for (StreetEntity street : allStreets) {
      assertNotNull(street.getId());
      assertNotNull(street.getName());
      assertNotNull(street.getPostalCode());

      assertNotNull(street.getHouses());

      assertNotNull(street.getRoadSurfaces());
    }
  }

  private List<RoadSurfaceEntity> getRoadSurfacesByStreetId(Long streetId) throws SQLException {
    List<RoadSurfaceEntity> roadSurfaces = new ArrayList<>();
    String sql = "SELECT rs.* FROM road_surface rs " +
        "JOIN road_surface_street rss ON rs.id = rss.road_surface_id " +
        "WHERE rss.street_id = ?";
    try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, streetId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          RoadSurfaceEntity roadSurface = roadSurfaceResultSetMapper.map(resultSet);
          roadSurfaces.add(roadSurface);
        }
      }
    }
    return roadSurfaces;
  }

  private List<HouseEntity> getHousesByStreetId(Long streetId) throws SQLException {
    List<HouseEntity> houses = new ArrayList<>();
    String sql = "SELECT * FROM house WHERE street_id = ?";
    try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, streetId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          HouseEntity house = houseResultSetMapper.map(resultSet);
          houses.add(house);
        }
      }
    }
    return houses;
  }


}
