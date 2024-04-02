package by.russianzak.repository.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.StreetEntityRepository;
import by.russianzak.service.impl.StreetEntityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StreetEntityServiceImplTest {

  @Mock
  private StreetEntityRepository streetRepository;

  @InjectMocks
  private StreetEntityServiceImpl streetService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void save_ValidStreet_Success() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setId(1L)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    when(streetRepository.save(any(StreetEntity.class))).thenReturn(street);

    StreetEntity savedStreet = streetService.save(street);

    assertEquals(street, savedStreet);
    verify(streetRepository, times(1)).save(street);
  }

  @Test
  void update_ValidStreet_Success() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setId(1L)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    when(streetRepository.update(any(StreetEntity.class))).thenReturn(street);

    StreetEntity updatedStreet = streetService.update(street);

    assertEquals(street, updatedStreet);
    verify(streetRepository, times(1)).update(street);
  }

  @Test
  void getById_ExistingStreetId_ReturnsCorrectStreet() throws RepositoryException {
    Long streetId = 1L;
    StreetEntity street = StreetEntity.builder()
        .setId(streetId)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    when(streetRepository.getById(streetId)).thenReturn(street);

    StreetEntity retrievedStreet = streetService.getById(streetId);

    assertEquals(street, retrievedStreet);
    verify(streetRepository, times(1)).getById(streetId);
  }

  @Test
  void deleteById_ExistingStreetId_StreetDeletedSuccessfully() throws RepositoryException {
    Long streetId = 1L;
    when(streetRepository.deleteById(streetId)).thenReturn(true);

    boolean isDeleted = streetService.deleteById(streetId);

    assertTrue(isDeleted);
    verify(streetRepository, times(1)).deleteById(streetId);
  }

  @Test
  void delete_ValidStreet_StreetDeletedSuccessfully() throws RepositoryException {
    StreetEntity street = StreetEntity.builder()
        .setId(1L)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    when(streetRepository.delete(any(StreetEntity.class))).thenReturn(true);

    boolean isDeleted = streetService.delete(street);

    assertTrue(isDeleted);
    verify(streetRepository, times(1)).delete(street);
  }

  @Test
  void findById_ExistingStreetId_ReturnsOptionalWithCorrectStreet() throws RepositoryException {
    Long streetId = 1L;
    StreetEntity street = StreetEntity.builder()
        .setId(streetId)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();
    when(streetRepository.findById(streetId)).thenReturn(Optional.of(street));

    Optional<StreetEntity> optionalStreet = streetService.findById(streetId);

    assertTrue(optionalStreet.isPresent());
    assertEquals(street, optionalStreet.get());
    verify(streetRepository, times(1)).findById(streetId);
  }

  @Test
  void getAll_RetrieveAllStreets_Success() throws RepositoryException {
    List<StreetEntity> streets = new ArrayList<>();
    streets.add(StreetEntity.builder()
        .setId(1L)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build());
    streets.add(StreetEntity.builder()
        .setId(2L)
        .setName("Oak Street")
        .setPostalCode(67890L)
        .build());
    when(streetRepository.getAll()).thenReturn(streets);

    List<StreetEntity> retrievedStreets = streetService.getAll();

    assertEquals(streets, retrievedStreets);
    verify(streetRepository, times(1)).getAll();
  }
}
