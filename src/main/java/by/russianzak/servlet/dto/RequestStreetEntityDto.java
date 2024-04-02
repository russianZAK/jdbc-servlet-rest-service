package by.russianzak.servlet.dto;


import com.google.gson.annotations.SerializedName;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class RequestStreetEntityDto {
  private String name;
  private Long postalCode;
  private List<HouseDto> houses;
  private List<RoadSurfaceDto> roadSurfaces;

  public RequestStreetEntityDto() {}

  public RequestStreetEntityDto(String name, Long postalCode, List<HouseDto> houses, List<RoadSurfaceDto> roadSurfaces) {
    this.name = name;
    this.postalCode = postalCode;
    this.houses = houses;
    this.roadSurfaces = roadSurfaces;
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


  public record HouseDto(String houseNumber, Date buildDate, int numFloors, String type) {}

  public record RoadSurfaceDto(String type, String description, double frictionCoefficient) {}
}
