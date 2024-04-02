package by.russianzak.repository.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.HouseEntity.TypeOfBuilding;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestStreetEntityDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto;
import by.russianzak.servlet.mapper.StreetEntityDtoMapperImpl;
import java.util.ArrayList;
import java.util.Date;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreetEntityDtoMapperImplTest {

  @Test
  void mapRequestDtoToEntity_ValidRequestDto_MapsCorrectly() {
    RequestStreetEntityDto requestDto = new RequestStreetEntityDto("Main Street", 12345L, new ArrayList<>(), new ArrayList<>());

    List<RequestStreetEntityDto.HouseDto> houseDtos = List.of(
        new RequestStreetEntityDto.HouseDto("123", new java.sql.Date(new Date().getTime()), 2, "COMMERCIAL"),
        new RequestStreetEntityDto.HouseDto("456",  new java.sql.Date(new Date().getTime()), 3, "RESIDENTIAL")
    );
    requestDto.setHouses(houseDtos);

    List<RequestStreetEntityDto.RoadSurfaceDto> roadSurfaceDtos = List.of(
        new RequestStreetEntityDto.RoadSurfaceDto("ASPHALT", "Smooth asphalt surface", 0.8),
        new RequestStreetEntityDto.RoadSurfaceDto("CONCRETE", "Rough concrete surface", 0.7)
    );
    requestDto.setRoadSurfaces(roadSurfaceDtos);

    StreetEntityDtoMapperImpl mapper = new StreetEntityDtoMapperImpl();

    StreetEntity entity = mapper.map(requestDto);

    assertNotNull(entity);
    assertEquals("Main Street", entity.getName());
    assertEquals(12345L, entity.getPostalCode());

    List<HouseEntity> houses = entity.getHouses();
    assertNotNull(houses);
    assertEquals(2, houses.size());
    for (HouseEntity house : houses) {
      assertNotNull(house);
      assertEquals(entity, house.getStreet());
    }

    List<RoadSurfaceEntity> roadSurfaces = entity.getRoadSurfaces();
    assertNotNull(roadSurfaces);
    assertEquals(2, roadSurfaces.size());
  }

  @Test
  void mapEntityToResponseDto_ValidEntity_MapsCorrectly() {
    StreetEntity entity = StreetEntity.builder()
        .setId(1L)
        .setName("Main Street")
        .setPostalCode(12345L)
        .build();

    Set<HouseEntity> houses = new HashSet<>();
    houses.add(HouseEntity.builder()
        .setId(1L)
        .setHouseNumber("123")
        .setNumFloors(2)
        .setType(TypeOfBuilding.COMMERCIAL)
        .setStreet(entity)
        .build());
    houses.add(HouseEntity.builder()
        .setId(2L)
        .setHouseNumber("456")
        .setNumFloors(3)
        .setType(TypeOfBuilding.RESIDENTIAL)
        .setStreet(entity)
        .build());
    entity.setHouses(houses);

    Set<RoadSurfaceEntity> roadSurfaces = new HashSet<>();
    roadSurfaces.add(RoadSurfaceEntity.builder()
        .setId(1L)
        .setType(TypeOfRoadSurface.ASPHALT)
        .setDescription("Smooth asphalt surface")
        .setFrictionCoefficient(0.8)
        .build());
    roadSurfaces.add(RoadSurfaceEntity.builder()
        .setId(2L)
        .setType(TypeOfRoadSurface.CONCRETE)
        .setDescription("Rough concrete surface")
        .setFrictionCoefficient(0.7)
        .build());
    entity.setRoadSurfaces(roadSurfaces);

    StreetEntityDtoMapperImpl mapper = new StreetEntityDtoMapperImpl();

    ResponseStreetEntityDto responseDto = mapper.map(entity);

    assertNotNull(responseDto);
    assertEquals(1L, responseDto.getId());
    assertEquals("Main Street", responseDto.getName());
    assertEquals(12345L, responseDto.getPostalCode());

    List<ResponseStreetEntityDto.HouseDto> houseDtos = responseDto.getHouses();
    assertNotNull(houseDtos);
    assertEquals(2, houseDtos.size());

    List<ResponseStreetEntityDto.RoadSurfaceDto> roadSurfaceDtos = responseDto.getRoadSurfaces();
    assertNotNull(roadSurfaceDtos);
    assertEquals(2, roadSurfaceDtos.size());

    for (ResponseStreetEntityDto.HouseDto houseDto : houseDtos) {
      boolean foundMatchingHouse = houses.stream()
          .anyMatch(houseEntity ->
              houseEntity.getId().equals(houseDto.id()) &&
                  houseEntity.getHouseNumber().equals(houseDto.houseNumber()) &&
                  houseEntity.getNumFloors() == houseDto.numFloors() &&
                  houseEntity.getType().equals(houseDto.type()));
      assertTrue(foundMatchingHouse);
    }

    for (ResponseStreetEntityDto.RoadSurfaceDto roadSurfaceDto : roadSurfaceDtos) {
      boolean foundMatchingRoadSurface = roadSurfaces.stream()
          .anyMatch(roadSurfaceEntity ->
              roadSurfaceEntity.getId().equals(roadSurfaceDto.id()) &&
                  roadSurfaceEntity.getType().equals(roadSurfaceDto.type()) &&
                  roadSurfaceEntity.getDescription().equals(roadSurfaceDto.description()) &&
                  roadSurfaceEntity.getFrictionCoefficient() == roadSurfaceDto.frictionCoefficient());
      assertTrue(foundMatchingRoadSurface);
    }
  }

}
