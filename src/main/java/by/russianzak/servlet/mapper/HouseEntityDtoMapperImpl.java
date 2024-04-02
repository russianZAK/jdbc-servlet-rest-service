package by.russianzak.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.HouseEntity.TypeOfBuilding;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto.Street;

public class HouseEntityDtoMapperImpl implements HouseEntityDtoMapper{

  @Override
  public HouseEntity map(RequestHouseEntityDto incomingDto) {
    return HouseEntity.builder().setHouseNumber(incomingDto.getHouseNumber()).setStreet(
            StreetEntity.builder().setName(incomingDto.getStreetName()).setPostalCode(incomingDto.getStreetPostalCode()).build()).setType(
        TypeOfBuilding.valueOf(incomingDto.getType())).setBuildDate(incomingDto.getBuildDate()).setNumFloors(incomingDto.getNumFloors()).build();
  }

  @Override
  public ResponseHouseEntityDto map(HouseEntity entity) {
    return new ResponseHouseEntityDto(entity.getId(), entity.getHouseNumber(), entity.getBuildDate(), entity.getNumFloors(), entity.getType(),
        new Street(entity.getStreet().getId(), entity.getStreet().getName(), entity.getStreet().getPostalCode()));
  }
}
