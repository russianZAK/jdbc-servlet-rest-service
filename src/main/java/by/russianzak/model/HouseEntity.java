package by.russianzak.model;

import java.util.Date;
import java.util.Objects;

public class HouseEntity {

  private Long id;
  private String houseNumber;
  private Date buildDate;
  private int numFloors;
  private TypeOfBuilding type;
  private StreetEntity street;

  public HouseEntity() {}

  private HouseEntity(Builder builder) {
    setId(builder.id);
    setHouseNumber(builder.houseNumber);
    setBuildDate(builder.buildDate);
    setNumFloors(builder.numFloors);
    setType(builder.type);
    setStreet(builder.street);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public String getHouseNumber() {
    return houseNumber;
  }

  public Date getBuildDate() {
    return buildDate;
  }

  public int getNumFloors() {
    return numFloors;
  }

  public String getType() {
    return type.value;
  }

  public StreetEntity getStreet() {
    return street;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setHouseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
  }

  public void setBuildDate(Date buildDate) {
    this.buildDate = buildDate;
  }

  public void setNumFloors(int numFloors) {
     this.numFloors = numFloors;
  }

  public void setType(TypeOfBuilding type) {
    this.type = type;
  }

  public void setStreet(StreetEntity street) {
    this.street = street;
  }

  @Override
  public String toString() {
    return "HouseEntity{" +
        "id=" + id +
        ", houseNumber='" + houseNumber + '\'' +
        ", buildDate=" + buildDate +
        ", numFloors=" + numFloors +
        ", type=" + type.value +
        ", streetId=" + street.getId()+
        '}';
  }

  public static class Builder {
    private Long id;
    private String houseNumber;
    private Date buildDate;
    private int numFloors;
    private TypeOfBuilding type;
    private StreetEntity street;

    public Builder setId(Long id) {
      this.id = id;
      return this;
    }

    public Builder setHouseNumber(String houseNumber) {
      this.houseNumber = houseNumber;
      return this;
    }

    public Builder setBuildDate(Date buildDate) {
      this.buildDate = buildDate;
      return this;
    }

    public Builder setNumFloors(int numFloors) {
      this.numFloors = numFloors;
      return this;
    }

    public Builder setType(TypeOfBuilding type) {
      this.type = type;
      return this;
    }

    public Builder setStreet(StreetEntity street) {
      this.street = street;
      return this;
    }

    public HouseEntity build() {
      return new HouseEntity(this);
    }
  }

  public enum TypeOfBuilding {
    RESIDENTIAL("RESIDENTIAL"),
    COMMERCIAL("COMMERCIAL"),
    GARAGE("GARAGE"),
    UTILITY("UTILITY");

    private final String value;

    TypeOfBuilding(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static TypeOfBuilding fromValue(String value) {
      for (TypeOfBuilding type : TypeOfBuilding.values()) {
        if (type.getValue().equalsIgnoreCase(value)) {
          return type;
        }
      }
      throw new IllegalArgumentException("Invalid building type value: " + value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HouseEntity that = (HouseEntity) o;
    return Objects.equals(houseNumber, that.houseNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(houseNumber);
  }

  public void validateNotNullFields() {
    if (houseNumber == null || buildDate == null || type == null || street == null) {
      throw new IllegalArgumentException("All fields of HouseEntity must not be null");
    }
  }
}
