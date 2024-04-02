package by.russianzak.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.servlet.dto.RequestHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto;

public interface HouseEntityDtoMapper {
  HouseEntity map(RequestHouseEntityDto incomingDto);

  ResponseHouseEntityDto map(HouseEntity entity);
}
