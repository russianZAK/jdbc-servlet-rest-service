package by.russianzak.repository.servlet.mapper;

import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestRoadSurfaceEntityDto;
import by.russianzak.servlet.dto.ResponseRoadSurfaceEntityDto;
import by.russianzak.servlet.mapper.RoadSurfaceEntityDtoMapperImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadSurfaceEntityDtoMapperImplTest {

  @Test
  void mapRequestDtoToEntity_ValidRequestDto_MapsCorrectly() {
    RequestRoadSurfaceEntityDto requestDto = new RequestRoadSurfaceEntityDto("ASPHALT", "Smooth asphalt surface", 0.8, new ArrayList<>());

    List<RequestRoadSurfaceEntityDto.StreetDto> streetDtos = new ArrayList<>();
    streetDtos.add(new RequestRoadSurfaceEntityDto.StreetDto("Main Street", 12345L));
    streetDtos.add(new RequestRoadSurfaceEntityDto.StreetDto("Second Street", 54321L));
    requestDto.setStreets(streetDtos);

    RoadSurfaceEntityDtoMapperImpl mapper = new RoadSurfaceEntityDtoMapperImpl();

    RoadSurfaceEntity entity = mapper.map(requestDto);

    assertNotNull(entity);
    assertEquals("ASPHALT", entity.getType());
    assertEquals("Smooth asphalt surface", entity.getDescription());
    assertEquals(0.8, entity.getFrictionCoefficient());
    assertEquals(2, entity.getStreets().size());
    Set<String> expectedStreetNames = Set.of("Main Street", "Second Street");
    for (StreetEntity streetEntity : entity.getStreets()) {
      assertTrue(expectedStreetNames.contains(streetEntity.getName()));
    }
  }

  @Test
  void mapEntityToResponseDto_ValidEntity_MapsCorrectly() {
    RoadSurfaceEntity entity = RoadSurfaceEntity.builder().setId(1L)
        .setDescription("Rough concrete surface").setType(TypeOfRoadSurface.CONCRETE)
        .setFrictionCoefficient(0.7).build();

    Set<StreetEntity> streets = new HashSet<>();
    streets.add(StreetEntity.builder().setId(1L).setName("Main Street").setPostalCode(12345L).build());
    streets.add(StreetEntity.builder().setId(2L).setName("Second Street").setPostalCode(145765L).build());
    entity.setStreets(streets);

    RoadSurfaceEntityDtoMapperImpl mapper = new RoadSurfaceEntityDtoMapperImpl();

    ResponseRoadSurfaceEntityDto responseDto = mapper.map(entity);

    assertNotNull(responseDto);
    assertEquals(1L, responseDto.getId());
    assertEquals("CONCRETE", responseDto.getType());
    assertEquals("Rough concrete surface", responseDto.getDescription());
    assertEquals(0.7, responseDto.getFrictionCoefficient());
    assertEquals(2, responseDto.getStreets().size());
    Set<String> expectedStreetNames = Set.of("Main Street", "Second Street");
    for (ResponseRoadSurfaceEntityDto.StreetDto streetDto : responseDto.getStreets()) {
      assertTrue(expectedStreetNames.contains(streetDto.name()));
    }
  }
}
