package by.russianzak.repository.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.repository.HouseEntityRepository;
import by.russianzak.service.impl.HouseEntityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HouseEntityServiceImplTest {

  @Mock
  private HouseEntityRepository houseEntityRepository;

  @InjectMocks
  private HouseEntityServiceImpl houseEntityService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void save_ValidHouseEntity_Success() throws RepositoryException {
    HouseEntity houseEntity = HouseEntity.builder().setId(1L).setHouseNumber("123").setNumFloors(2).build();

    when(houseEntityRepository.save(houseEntity)).thenReturn(houseEntity);

    HouseEntity result = houseEntityService.save(houseEntity);

    assertEquals(houseEntity, result);

    verify(houseEntityRepository, times(1)).save(houseEntity);
  }

  @Test
  void getById_ExistingHouseId_ReturnsCorrectHouseEntity() throws RepositoryException {
    HouseEntity houseEntity = HouseEntity.builder().setId(1L).setHouseNumber("123").setNumFloors(2).build();

    when(houseEntityRepository.getById(1L)).thenReturn(houseEntity);

    HouseEntity result = houseEntityService.getById(1L);

    assertEquals(houseEntity, result);

    verify(houseEntityRepository, times(1)).getById(1L);
  }

  @Test
  void deleteById_ExistingHouseId_HouseEntityDeletedSuccessfully() throws RepositoryException {
    when(houseEntityRepository.deleteById(1L)).thenReturn(true);

    boolean isDeleted = houseEntityService.deleteById(1L);

    verify(houseEntityRepository, times(1)).deleteById(1L);
    assertTrue(isDeleted);
  }

  @Test
  void delete_ValidHouseEntity_HouseEntityDeletedSuccessfully() throws RepositoryException {
    HouseEntity houseEntity = HouseEntity.builder()
        .setId(1L)
        .setHouseNumber("123")
        .setNumFloors(2)
        .build();

    when(houseEntityRepository.delete(houseEntity)).thenReturn(true);

    boolean isDeleted = houseEntityService.delete(houseEntity);

    verify(houseEntityRepository, times(1)).delete(houseEntity);
    assertTrue(isDeleted);
  }

  @Test
  void update_ValidHouseEntity_Success() throws RepositoryException {
    HouseEntity houseEntity = HouseEntity.builder().setId(1L).setHouseNumber("123").setNumFloors(2).build();

    when(houseEntityRepository.update(houseEntity)).thenReturn(houseEntity);

    HouseEntity result = houseEntityService.update(houseEntity);

    assertEquals(houseEntity, result);

    verify(houseEntityRepository, times(1)).update(houseEntity);
  }

  @Test
  void findById_ExistingHouseId_ReturnsOptionalWithCorrectHouseEntity() throws RepositoryException {
    HouseEntity houseEntity = HouseEntity.builder().setId(1L).setHouseNumber("123").setNumFloors(2).build();

    when(houseEntityRepository.findById(1L)).thenReturn(Optional.of(houseEntity));

    Optional<HouseEntity> result = houseEntityService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(houseEntity, result.get());

    verify(houseEntityRepository, times(1)).findById(1L);
  }

  @Test
  void getAll_RetrieveAllHouseEntities_Success() throws RepositoryException {
    HouseEntity houseEntity1 = HouseEntity.builder().setId(1L).setHouseNumber("123").setNumFloors(2).build();

    HouseEntity houseEntity2 = HouseEntity.builder().setId(2L).setHouseNumber("123").setNumFloors(2).build();

    List<HouseEntity> houseEntities = Arrays.asList(houseEntity1, houseEntity2);

    when(houseEntityRepository.getAll()).thenReturn(houseEntities);

    List<HouseEntity> result = houseEntityService.getAll();

    assertEquals(houseEntities, result);

    verify(houseEntityRepository, times(1)).getAll();
  }
}
