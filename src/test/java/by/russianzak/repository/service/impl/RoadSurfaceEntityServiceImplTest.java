package by.russianzak.repository.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import by.russianzak.repository.RoadSurfaceEntityRepository;
import by.russianzak.service.impl.RoadSurfaceEntityServiceImpl;
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

class RoadSurfaceEntityServiceImplTest {

  @Mock
  private RoadSurfaceEntityRepository roadSurfaceEntityRepository;

  @InjectMocks
  private RoadSurfaceEntityServiceImpl roadSurfaceEntityService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void save_ValidRoadSurface_Success() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setType(TypeOfRoadSurface.ASPHALT)
        .setDescription("Smooth asphalt surface")
        .setFrictionCoefficient(0.8)
        .build();

    when(roadSurfaceEntityRepository.save(any(RoadSurfaceEntity.class))).thenReturn(roadSurface);

    RoadSurfaceEntity savedRoadSurface = roadSurfaceEntityService.save(roadSurface);

    assertEquals(roadSurface, savedRoadSurface);
    verify(roadSurfaceEntityRepository, times(1)).save(roadSurface);
  }

  @Test
  void update_ValidRoadSurface_Success() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setId(1L)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build();

    when(roadSurfaceEntityRepository.update(any(RoadSurfaceEntity.class))).thenReturn(roadSurface);

    RoadSurfaceEntity updatedRoadSurface = roadSurfaceEntityService.update(roadSurface);

    assertEquals(roadSurface, updatedRoadSurface);
    verify(roadSurfaceEntityRepository, times(1)).update(roadSurface);
  }

  @Test
  void getById_ExistingRoadSurfaceId_ReturnsCorrectRoadSurface() throws RepositoryException {
    Long roadSurfaceId = 1L;
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setId(roadSurfaceId)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build();

    when(roadSurfaceEntityRepository.getById(roadSurfaceId)).thenReturn(roadSurface);

    RoadSurfaceEntity retrievedRoadSurface = roadSurfaceEntityService.getById(roadSurfaceId);

    assertEquals(roadSurface, retrievedRoadSurface);
    verify(roadSurfaceEntityRepository, times(1)).getById(roadSurfaceId);
  }

  @Test
  void deleteById_ExistingRoadSurfaceId_RoadSurfaceDeletedSuccessfully() throws RepositoryException {
    Long roadSurfaceId = 1L;

    when(roadSurfaceEntityRepository.deleteById(roadSurfaceId)).thenReturn(true);

    assertTrue(roadSurfaceEntityService.deleteById(roadSurfaceId));
    verify(roadSurfaceEntityRepository, times(1)).deleteById(roadSurfaceId);
  }

  @Test
  void delete_ValidRoadSurface_RoadSurfaceDeletedSuccessfully() throws RepositoryException {
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setId(1L)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build();

    when(roadSurfaceEntityRepository.delete(any(RoadSurfaceEntity.class))).thenReturn(true);

    assertTrue(roadSurfaceEntityService.delete(roadSurface));
    verify(roadSurfaceEntityRepository, times(1)).delete(roadSurface);
  }

  @Test
  void findById_ExistingRoadSurfaceId_ReturnsOptionalWithCorrectRoadSurface() throws RepositoryException {
    Long roadSurfaceId = 1L;
    RoadSurfaceEntity roadSurface = RoadSurfaceEntity.builder()
        .setId(roadSurfaceId)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build();

    when(roadSurfaceEntityRepository.findById(roadSurfaceId)).thenReturn(Optional.of(roadSurface));

    Optional<RoadSurfaceEntity> optionalRoadSurface = roadSurfaceEntityService.findById(roadSurfaceId);

    assertTrue(optionalRoadSurface.isPresent());
    assertEquals(roadSurface, optionalRoadSurface.get());
    verify(roadSurfaceEntityRepository, times(1)).findById(roadSurfaceId);
  }

  @Test
  void getAll_RetrieveAllRoadSurfaces_Success() throws RepositoryException {
    List<RoadSurfaceEntity> roadSurfaces = new ArrayList<>();
    roadSurfaces.add(RoadSurfaceEntity.builder()
        .setId(1L)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build());
    roadSurfaces.add(RoadSurfaceEntity.builder()
        .setId(2L)
        .setType(TypeOfRoadSurface.ASPHALT)
        .setDescription("Smooth asphalt surface")
        .setFrictionCoefficient(0.8)
        .build());

    when(roadSurfaceEntityRepository.getAll()).thenReturn(roadSurfaces);

    List<RoadSurfaceEntity> retrievedRoadSurfaces = roadSurfaceEntityService.getAll();

    assertEquals(roadSurfaces.size(), retrievedRoadSurfaces.size());
    assertEquals(roadSurfaces, retrievedRoadSurfaces);
    verify(roadSurfaceEntityRepository, times(1)).getAll();
  }
}
