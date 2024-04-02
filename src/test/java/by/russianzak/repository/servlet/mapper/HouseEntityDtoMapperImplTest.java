package by.russianzak.repository.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto;
import by.russianzak.servlet.mapper.HouseEntityDtoMapperImpl;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HouseEntityDtoMapperImplTest {

  @Test
  void mapRequestDtoToEntity_ValidRequestDto_MapsCorrectly() {
    RequestHouseEntityDto requestDto = new RequestHouseEntityDto("123", new Date(), 2, "COMMERCIAL", "Main Street", 12345L);

    HouseEntityDtoMapperImpl mapper = new HouseEntityDtoMapperImpl();

    HouseEntity entity = mapper.map(requestDto);

    assertNotNull(entity);
    assertEquals("123", entity.getHouseNumber());
    assertEquals(2, entity.getNumFloors());
    assertEquals("COMMERCIAL", entity.getType());
    assertEquals("Main Street", entity.getStreet().getName());
    assertEquals(12345L, entity.getStreet().getPostalCode());
  }

  @Test
  void mapEntityToResponseDto_ValidEntity_MapsCorrectly() {
    HouseEntity entity = new HouseEntity.Builder()
        .setId(1L)
        .setHouseNumber("123")
        .setBuildDate(new Date())
        .setNumFloors(2)
        .setType(HouseEntity.TypeOfBuilding.COMMERCIAL)
        .setStreet(StreetEntity.builder()
            .setId(1L)
            .setName("Main Street")
            .setPostalCode(12345L)
            .build())
        .build();

    HouseEntityDtoMapperImpl mapper = new HouseEntityDtoMapperImpl();

    ResponseHouseEntityDto responseDto = mapper.map(entity);

    assertNotNull(responseDto);
    assertEquals(1L, responseDto.getId());
    assertEquals("123", responseDto.getHouseNumber());
    assertEquals(2, responseDto.getNumFloors());
    assertEquals("COMMERCIAL", responseDto.getType());
    assertEquals(1L, responseDto.getStreet().id());
    assertEquals("Main Street", responseDto.getStreet().name());
    assertEquals(12345L, responseDto.getStreet().postalCode());
  }
}
