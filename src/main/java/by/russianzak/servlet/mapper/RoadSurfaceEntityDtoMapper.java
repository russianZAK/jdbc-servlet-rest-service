package by.russianzak.servlet.mapper;

import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.servlet.dto.RequestRoadSurfaceEntityDto;
import by.russianzak.servlet.dto.ResponseRoadSurfaceEntityDto;

public interface RoadSurfaceEntityDtoMapper {
    RoadSurfaceEntity map(RequestRoadSurfaceEntityDto incomingDto);
    ResponseRoadSurfaceEntityDto map(RoadSurfaceEntity entity);
}
