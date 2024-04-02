package by.russianzak.servlet.dto;

import java.util.Date;

public class RequestHouseEntityDto {
  private String houseNumber;
  private Date buildDate;
  private int numFloors;
  private String type;
  private String streetName;
  private Long streetPostalCode;

  public RequestHouseEntityDto() {}

  public RequestHouseEntityDto(String houseNumber, Date buildDate, int numFloors, String type,
      String streetName, Long streetPostalCode) {
    this.houseNumber = houseNumber;
    this.buildDate = buildDate;
    this.numFloors = numFloors;
    this.type = type;
    this.streetName = streetName;
    this.streetPostalCode = streetPostalCode;
  }

  public String getHouseNumber() {
    return houseNumber;
  }

  public void setHouseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
  }

  public Date getBuildDate() {
    return buildDate;
  }

  public void setBuildDate(Date buildDate) {
    this.buildDate = buildDate;
  }

  public int getNumFloors() {
    return numFloors;
  }

  public void setNumFloors(int numFloors) {
    this.numFloors = numFloors;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public Long getStreetPostalCode() {
    return streetPostalCode;
  }

  public void setStreetPostalCode(Long streetPostalCode) {
    this.streetPostalCode = streetPostalCode;
  }
}
