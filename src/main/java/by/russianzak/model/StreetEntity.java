package by.russianzak.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StreetEntity {
  private Long id;
  private String name;
  private Long postalCode;
  private Set<HouseEntity> houses;
  private Set<RoadSurfaceEntity> roadSurfaces;

  public StreetEntity() {}

  private StreetEntity(Builder builder) {
    setId(builder.id);
    setName(builder.name);
    setPostalCode(builder.postalCode);
    setHouses(builder.houses);
    setRoadSurfaces(builder.roadSurfaces);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getPostalCode() {
    return postalCode;
  }

  public List<HouseEntity> getHouses() {
    return houses.stream().toList();
  }

  public List<RoadSurfaceEntity> getRoadSurfaces() {
    return roadSurfaces.stream().toList();
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPostalCode(Long postalCode) {
    this.postalCode = postalCode;
  }

  public void setHouses(Set<HouseEntity> houses) {
    this.houses = houses;
  }

  public void addHouse(HouseEntity house) {
    this.houses.add(house);
  }

  public void setRoadSurfaces(Set<RoadSurfaceEntity> roadSurfaces) {
    this.roadSurfaces = roadSurfaces;
  }

  public void addRoadSurface(RoadSurfaceEntity roadSurface) {
    this.roadSurfaces.add(roadSurface);
  }

  @Override
  public String toString() {
    return "StreetEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", postalCode=" + postalCode +
        ", houseIds=" + houses.stream().map(HouseEntity::getId).collect(Collectors.toSet()) +
        ", roadSurfaceIds=" + roadSurfaces.stream().map(RoadSurfaceEntity::getId).collect(Collectors.toSet()) +
        '}';
  }

  public static class Builder {
    private Long id;
    private String name;
    private Long postalCode;
    private Set<HouseEntity> houses;
    private Set<RoadSurfaceEntity> roadSurfaces;

    private Builder() {
      this.houses = new HashSet<>();
      this.roadSurfaces = new HashSet<>();
    }

    public Builder setId(Long id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setPostalCode(Long postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    public Builder setHouses(List<HouseEntity> houses) {
      this.houses = new HashSet<>(houses);
      return this;
    }

    public Builder setRoadSurfaces(List<RoadSurfaceEntity> roadSurfaces) {
      this.roadSurfaces = new HashSet<>(roadSurfaces);
      return this;
    }

    public StreetEntity build() {
      return new StreetEntity(this);
    }

    @Override
    public String toString() {
      return "StreetEntity.Builder{" +
          ", name='" + name + '\'' +
          ", postalCode=" + postalCode +
          '}';
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StreetEntity that = (StreetEntity) o;
    return Objects.equals(postalCode, that.postalCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postalCode);
  }

  public void validateNotNullFields() {
    if (name == null || postalCode == null || houses == null || roadSurfaces == null) {
      throw new IllegalArgumentException("All fields of StreetEntity must not be null");
    }
  }
}
