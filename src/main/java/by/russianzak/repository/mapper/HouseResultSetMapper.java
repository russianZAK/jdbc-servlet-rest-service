package by.russianzak.repository.mapper;

import by.russianzak.model.HouseEntity;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface HouseResultSetMapper extends ResultSetMapper<HouseEntity>{
  HouseEntity map(ResultSet resultSet) throws SQLException;
}
