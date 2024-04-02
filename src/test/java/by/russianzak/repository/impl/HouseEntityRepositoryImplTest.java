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
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.HouseEntityRepository;
import by.russianzak.repository.mapper.HouseResultSetMapper;
import by.russianzak.repository.mapper.HouseResultSetMapperImpl;
import by.russianzak.repository.mapper.StreetResultSetMapper;
import by.russianzak.repository.mapper.StreetResultSetMapperImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.ResultSet;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class HouseEntityRepositoryImplTest {
  private static final String INIT_SCRIPT_PATH = "db/migration/V1_1_0__create_street_house_road_surface.schema.sql";

  private final static HouseResultSetMapper houseResultSetMapper = new HouseResultSetMapperImpl();
  private final static StreetResultSetMapper streetResultSetMapper = new StreetResultSetMapperImpl();
  private static HouseEntityRepository houseEntityRepository;
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
    houseEntityRepository = new HouseEntityRepositoryImpl(houseResultSetMapper, streetResultSetMapper,
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
  void saveHouse_StreetDoesntExist_Success() {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue")
        .build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();

    Long savedHouseId = assertDoesNotThrow(() -> houseEntityRepository.save(houseEntity).getId());

    assertNotNull(savedHouseId);
  }

  @Test
  void saveHouse_EntityExists_ThrowsEntityExistsException() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue")
        .build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();

    houseEntityRepository.save(houseEntity);

    assertThrows(EntityExistsException.class, () -> houseEntityRepository.save(houseEntity));
  }

  @Test
  void saveHouse_StreetExists_Success() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    houseEntityRepository.save(houseEntity);

    assertNotNull(houseEntity.getStreet().getId());

    HouseEntity anotherHouseEntityOnSameStreet = HouseEntity.builder().setHouseNumber("35/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();

    houseEntityRepository.save(anotherHouseEntityOnSameStreet);

    assertEquals(houseEntity.getStreet().getId(), anotherHouseEntityOnSameStreet.getStreet().getId());
  }

  @Test
  void saveHouse_NullHouse_ThrowsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> houseEntityRepository.save(null));
  }

  @Test
  void saveHouse_NullStreet_ThrowsIllegalArgumentException() {
    HouseEntity houseEntity = HouseEntity.builder()
        .setHouseNumber("34/A")
        .setNumFloors(3)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(null)
        .build();

    assertThrows(IllegalArgumentException.class, () -> houseEntityRepository.save(houseEntity));
  }

  @Test
  void saveHouse_NullFields_ThrowsIllegalArgumentException() {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder()
        .setHouseNumber(null)
        .setNumFloors(3)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(street)
        .build();
    assertThrows(IllegalArgumentException.class, () -> houseEntityRepository.save(houseEntity));
  }

  @Test
  void saveHouse_StreetWithNullFields_ThrowsIllegalArgumentException() {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName(null).build();

    HouseEntity houseEntity = HouseEntity.builder()
        .setHouseNumber("34/A")
        .setNumFloors(3)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setBuildDate(new Date())
        .setStreet(street)
        .build();

    assertThrows(IllegalArgumentException.class, () -> houseEntityRepository.save(houseEntity));
  }

  @Test
  void deleteHouseById_ExistingId_Success() throws RepositoryException, SQLException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity savedHouse = houseEntityRepository.save(houseEntity);
    Long houseId = savedHouse.getId();
    boolean isDeleted = assertDoesNotThrow(() -> houseEntityRepository.deleteById(savedHouse.getId()));
    HouseEntity houseFromRepository = retrieveHouseById(houseId);
    assertNull(houseFromRepository);
    assertTrue(isDeleted);
  }

  @Test
  void deleteHouseById_NonExistentId_ReturnsFalse() {
    Long nonExistentId = Long.MAX_VALUE;
    boolean isDeleted = assertDoesNotThrow(() -> houseEntityRepository.deleteById(nonExistentId));
    assertFalse(isDeleted);
  }

  @Test
  void getById_ExistingId_Success() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity savedHouse = houseEntityRepository.save(houseEntity);
    Long houseId = savedHouse.getId();

    HouseEntity retrievedHouse = assertDoesNotThrow(() -> houseEntityRepository.getById(houseId));
    assertNotNull(retrievedHouse);
    assertEquals(houseId, retrievedHouse.getId());
    assertEquals(houseEntity.getHouseNumber(), retrievedHouse.getHouseNumber());
  }

  @Test
  void getById_NonExistentId_ThrowsEntityNotFoundException() {
    Long nonExistentId = Long.MAX_VALUE;
    assertThrows(EntityNotFoundException.class, () -> houseEntityRepository.getById(nonExistentId));
  }

  @Test
  void deleteExistingHouseEntity_Success() throws RepositoryException, SQLException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity savedHouse = houseEntityRepository.save(houseEntity);

    boolean isDeleted = assertDoesNotThrow(() -> houseEntityRepository.delete(savedHouse));

    assertTrue(isDeleted);
    assertNull(retrieveHouseById(savedHouse.getId()));
  }

  @Test
  void deleteNonExistentHouseEntity_ReturnsFalse() {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity nonExistentHouse = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    nonExistentHouse.setId(Long.MAX_VALUE);
    boolean isDeleted = assertDoesNotThrow(() -> houseEntityRepository.delete(nonExistentHouse));
    assertFalse(isDeleted);
  }

  @Test
  void updateExistingHouseEntity_Success() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity savedHouse = houseEntityRepository.save(houseEntity);
    savedHouse.setHouseNumber("35/B");
    HouseEntity updatedHouse = assertDoesNotThrow(() -> houseEntityRepository.update(savedHouse));
    assertNotNull(updatedHouse);
    assertEquals("35/B", updatedHouse.getHouseNumber());
  }

  @Test
  void updateNonExistentHouseEntity_ThrowsEntityNotFoundException() {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").setId(1L).build();
    HouseEntity nonExistentHouse = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    nonExistentHouse.setId(Long.MAX_VALUE);
    assertThrows(EntityNotFoundException.class, () -> houseEntityRepository.update(nonExistentHouse));
  }

  @Test
  void updateHouseWithExistingStreetAndHouseNumber_ThrowsRepositoryException() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity existingHouse = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    houseEntityRepository.save(existingHouse);

    HouseEntity houseToUpdate = HouseEntity.builder().setHouseNumber("35/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    houseEntityRepository.save(houseToUpdate);

    houseToUpdate.setHouseNumber(existingHouse.getHouseNumber());

    assertThrows(RepositoryException.class, () -> houseEntityRepository.update(houseToUpdate));
  }

  @Test
  void findHouseById_ExistingId_ReturnsHouseEntity() throws RepositoryException {
    StreetEntity street = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    HouseEntity houseEntity = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street).build();
    HouseEntity savedHouse = houseEntityRepository.save(houseEntity);
    Long houseId = savedHouse.getId();

    Optional<HouseEntity> retrievedHouseOptional = assertDoesNotThrow(() ->
       houseEntityRepository.findById(houseId));

    assertTrue(retrievedHouseOptional.isPresent());
    assertEquals(houseId, retrievedHouseOptional.get().getId());
    assertEquals(houseEntity.getHouseNumber(), retrievedHouseOptional.get().getHouseNumber());
  }

  @Test
  void findHouseById_NonExistentId_ReturnsEmptyOptional() {
    Long nonExistentId = Long.MAX_VALUE;

    Optional<HouseEntity> retrievedHouseOptional = assertDoesNotThrow(() ->
         houseEntityRepository.findById(nonExistentId)
      );

    assertTrue(retrievedHouseOptional.isEmpty());
  }

  @Test
  void getAllHouseEntities_HousesExist_ReturnsListOfHouseEntities() throws RepositoryException {
    StreetEntity street1 = StreetEntity.builder().setPostalCode(123L).setName("First avenue").build();
    StreetEntity street2 = StreetEntity.builder().setPostalCode(456L).setName("Second street").build();

    HouseEntity house1 = HouseEntity.builder().setHouseNumber("34/A")
        .setNumFloors(3).setType(TypeOfBuilding.COMMERCIAL).setBuildDate(new Date())
        .setStreet(street1).build();
    HouseEntity house2 = HouseEntity.builder().setHouseNumber("56/B")
        .setNumFloors(2).setType(TypeOfBuilding.RESIDENTIAL).setBuildDate(new Date())
        .setStreet(street2).build();

    houseEntityRepository.save(house1);
    houseEntityRepository.save(house2);

    List<HouseEntity> allHouses = assertDoesNotThrow(() -> houseEntityRepository.getAll());

    assertNotNull(allHouses);
    assertEquals(2, allHouses.size());

    assertTrue(allHouses.stream().anyMatch(house -> house.getId().equals(house1.getId())));
    assertTrue(allHouses.stream().anyMatch(house -> house.getId().equals(house2.getId())));
  }

  @Test
  void getAllHouseEntities_NoHousesExist_ReturnsEmptyList() {
    List<HouseEntity> allHouses = assertDoesNotThrow(() -> houseEntityRepository.getAll());

    assertNotNull(allHouses);
    assertTrue(allHouses.isEmpty());
  }

  private HouseEntity retrieveHouseById(Long id) throws SQLException {
    String sql = "SELECT * FROM house WHERE id = ?";
    try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return houseResultSetMapper.map(resultSet);
        }
      }
    }
    return null;
  }
}
