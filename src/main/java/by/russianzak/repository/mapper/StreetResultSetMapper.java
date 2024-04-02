package by.russianzak.repository.mapper;

import by.russianzak.model.StreetEntity;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface StreetResultSetMapper extends ResultSetMapper<StreetEntity>{
  StreetEntity map(ResultSet resultSet) throws SQLException;
}
