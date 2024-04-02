package by.russianzak.repository.mapper;

import by.russianzak.model.StreetEntity;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreetResultSetMapperImplTest {

  @Test
  void map_ValidInputData_CorrectMapping() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);

    long id = 1;
    String name = "Main Street";
    long postalCode = 12345L;

    when(resultSet.getLong("id")).thenReturn(id);
    when(resultSet.getString("name")).thenReturn(name);
    when(resultSet.getLong("postal_code")).thenReturn(postalCode);

    StreetResultSetMapperImpl mapper = new StreetResultSetMapperImpl();

    StreetEntity streetEntity = mapper.map(resultSet);

    assertEquals(id, streetEntity.getId());
    assertEquals(name, streetEntity.getName());
    assertEquals(postalCode, streetEntity.getPostalCode());
  }
}
