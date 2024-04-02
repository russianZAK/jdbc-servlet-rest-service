package by.russianzak.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestStreetEntityDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto;

public interface StreetEntityDtoMapper {
  StreetEntity map(RequestStreetEntityDto incomingDto);
  ResponseStreetEntityDto map(StreetEntity entity);
}

