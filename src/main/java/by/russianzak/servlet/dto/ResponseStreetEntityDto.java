package by.russianzak.servlet.dto;

import java.util.Date;
import java.util.List;

public class ResponseStreetEntityDto {
  private long id;
  private String name;
  private Long postalCode;
  private List<HouseDto> houses;
  private List<RoadSurfaceDto> roadSurfaces;

  public ResponseStreetEntityDto(long id, String name, Long postalCode, List<HouseDto> houses,
      List<RoadSurfaceDto> roadSurfaces) {
    this.id = id;
    this.name = name;
    this.postalCode = postalCode;
    this.houses = houses;
    this.roadSurfaces = roadSurfaces;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(Long postalCode) {
    this.postalCode = postalCode;
  }

  public List<HouseDto> getHouses() {
    return houses;
  }

  public void setHouses(List<HouseDto> houses) {
    this.houses = houses;
  }

  public List<RoadSurfaceDto> getRoadSurfaces() {
    return roadSurfaces;
  }

  public void setRoadSurfaces(
      List<RoadSurfaceDto> roadSurfaces) {
    this.roadSurfaces = roadSurfaces;
  }

  public record HouseDto(long id, String houseNumber, Date buildDate, int numFloors, String type) {}

  public record RoadSurfaceDto(long id, String type, String description, double frictionCoefficient) {}

}
