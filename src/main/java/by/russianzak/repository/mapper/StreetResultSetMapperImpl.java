package by.russianzak.repository.mapper;

import by.russianzak.model.StreetEntity;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StreetResultSetMapperImpl implements StreetResultSetMapper {

  @Override
  public StreetEntity map(ResultSet resultSet) throws SQLException {
    long id = resultSet.getLong("id");
    String name = resultSet.getString("name");
    long postalCode = resultSet.getLong("postal_code");
    return StreetEntity.builder().setId(id).setName(name).setPostalCode(postalCode).build();
  }
}
