package by.russianzak.repository.mapper;

import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.model.RoadSurfaceEntity.TypeOfRoadSurface;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoadSurfaceResultSetMapperImplTest {

  @Test
  void map_ValidInputData_CorrectMapping() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);

    long id = 1;
    TypeOfRoadSurface type = TypeOfRoadSurface.ASPHALT;
    String description = "Smooth surface";
    double frictionCoefficient = 0.8;

    when(resultSet.getLong("id")).thenReturn(id);
    when(resultSet.getString("type")).thenReturn(type.getValue());
    when(resultSet.getString("description")).thenReturn(description);
    when(resultSet.getDouble("friction_coefficient")).thenReturn(frictionCoefficient);

    RoadSurfaceResultSetMapperImpl mapper = new RoadSurfaceResultSetMapperImpl();

    RoadSurfaceEntity roadSurfaceEntity = mapper.map(resultSet);

    assertEquals(id, roadSurfaceEntity.getId());
    assertEquals(type.getValue(), roadSurfaceEntity.getType());
    assertEquals(description, roadSurfaceEntity.getDescription());
    assertEquals(frictionCoefficient, roadSurfaceEntity.getFrictionCoefficient());
  }
}
