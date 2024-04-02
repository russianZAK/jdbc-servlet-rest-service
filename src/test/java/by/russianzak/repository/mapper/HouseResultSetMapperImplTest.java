package by.russianzak.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import by.russianzak.model.HouseEntity;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;


class HouseResultSetMapperImplTest {

  @Test
  void map_ValidInputData_CorrectMapping() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    long houseId = 1;
    String houseNumber = "123";
    Date buildDate = new Date(new java.util.Date().getTime());
    int numFloors = 2;
    HouseEntity.TypeOfBuilding typeOfBuilding = HouseEntity.TypeOfBuilding.RESIDENTIAL;
    long streetId = 1;

    when(resultSet.getLong("id")).thenReturn(houseId);
    when(resultSet.getString("house_number")).thenReturn(houseNumber);
    when(resultSet.getDate("build_date")).thenReturn(buildDate);
    when(resultSet.getInt("num_floors")).thenReturn(numFloors);
    when(resultSet.getString("type")).thenReturn(typeOfBuilding.getValue());
    when(resultSet.getLong("street_id")).thenReturn(streetId);

    HouseResultSetMapperImpl mapper = new HouseResultSetMapperImpl();

    HouseEntity houseEntity = mapper.map(resultSet);

    assertEquals(houseId, houseEntity.getId());
    assertEquals(houseNumber, houseEntity.getHouseNumber());
    assertEquals(buildDate, houseEntity.getBuildDate());
    assertEquals(numFloors, houseEntity.getNumFloors());
    assertEquals(typeOfBuilding.getValue(), houseEntity.getType());
    assertEquals(streetId, houseEntity.getStreet().getId());
  }
}
