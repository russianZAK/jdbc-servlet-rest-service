package by.russianzak.repository.mapper;

import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoadSurfaceResultSetMapperImpl implements RoadSurfaceResultSetMapper{

  @Override
  public RoadSurfaceEntity map(ResultSet resultSet) throws SQLException {
    long id = resultSet.getLong("id");
    TypeOfRoadSurface type = TypeOfRoadSurface.fromValue(resultSet.getString("type"));
    String description = resultSet.getString("description");
    double frictionCoefficient = resultSet.getDouble("friction_coefficient");
    return RoadSurfaceEntity.builder().setId(id).setType(type).setDescription(description).setFrictionCoefficient(frictionCoefficient).build();
  }
}
