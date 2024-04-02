package by.russianzak.repository.mapper;

import by.russianzak.model.HouseEntity;
import by.russianzak.model.HouseEntity.TypeOfBuilding;
import by.russianzak.model.StreetEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class HouseResultSetMapperImpl implements HouseResultSetMapper{

  @Override
  public HouseEntity map(ResultSet resultSet) throws SQLException {
    long houseId = resultSet.getLong("id");
    String houseNumber = resultSet.getString("house_number");
    Date buildDate = resultSet.getDate("build_date");
    int numFloors = resultSet.getInt("num_floors");
    TypeOfBuilding typeOfBuilding = TypeOfBuilding.valueOf(resultSet.getString("type"));
    long streetId = resultSet.getLong("street_id");

    return HouseEntity.builder().setId(houseId).setHouseNumber(houseNumber).setBuildDate(buildDate).setNumFloors(numFloors)
        .setType(typeOfBuilding).setStreet(StreetEntity.builder().setId(streetId).build()).build();
  }
}
