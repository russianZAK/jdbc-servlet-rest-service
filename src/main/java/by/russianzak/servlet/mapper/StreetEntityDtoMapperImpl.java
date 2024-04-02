package by.russianzak.servlet.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.HouseEntity.TypeOfBuilding;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import by.russianzak.model.StreetEntity;
import by.russianzak.servlet.dto.RequestStreetEntityDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto.HouseDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto.RoadSurfaceDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StreetEntityDtoMapperImpl implements StreetEntityDtoMapper{

  @Override
  public StreetEntity map(RequestStreetEntityDto incomingDto) {
    StreetEntity streetEntity = StreetEntity.builder()
        .setName(incomingDto.getName())
        .setPostalCode(incomingDto.getPostalCode())
        .build();

    Set<RoadSurfaceEntity> roadSurfaceEntities = Optional.ofNullable(incomingDto.getRoadSurfaces())
        .map(surfaceDtos -> surfaceDtos.stream()
            .map(surfaceDto -> RoadSurfaceEntity.builder()
                .setType(TypeOfRoadSurface.fromValue(surfaceDto.type()))
                .setDescription(surfaceDto.description())
                .setFrictionCoefficient(surfaceDto.frictionCoefficient())
                .build())
            .collect(Collectors.toSet()))
        .orElse(new HashSet<>());

    Set<HouseEntity> houseEntities = Optional.ofNullable(incomingDto.getHouses())
        .map(houseDtos -> houseDtos.stream()
            .map(houseDto -> HouseEntity.builder()
                .setHouseNumber(houseDto.houseNumber())
                .setNumFloors(houseDto.numFloors())
                .setBuildDate(houseDto.buildDate())
                .setType(TypeOfBuilding.fromValue(houseDto.type()))
                .setStreet(streetEntity)
                .build())
            .collect(Collectors.toSet()))
        .orElse(new HashSet<>());

    streetEntity.setHouses(houseEntities);
    streetEntity.setRoadSurfaces(roadSurfaceEntities);

    return streetEntity;
  }

  @Override
  public ResponseStreetEntityDto map(StreetEntity entity) {
    List<HouseDto> houseDtos = entity.getHouses().stream().map(house -> new HouseDto(
        house.getId(), house.getHouseNumber(), house.getBuildDate(), house.getNumFloors(),
        house.getType())).toList();

    List<RoadSurfaceDto> roadSurfaceDtos = entity.getRoadSurfaces().stream().map(roadSurface -> new RoadSurfaceDto(roadSurface.getId(), roadSurface.getType(), roadSurface.getDescription(),
        roadSurface.getFrictionCoefficient())).toList();

    return new ResponseStreetEntityDto(entity.getId(), entity.getName(), entity.getPostalCode(), houseDtos, roadSurfaceDtos);
  }
}
