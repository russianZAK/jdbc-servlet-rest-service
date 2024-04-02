package by.russianzak.repository.servlet;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.model.StreetEntity;
import by.russianzak.service.HouseEntityService;
import by.russianzak.servlet.HouseEntityServlet;
import by.russianzak.servlet.dto.RequestHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto;
import by.russianzak.servlet.mapper.HouseEntityDtoMapper;
import com.google.gson.Gson;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HouseEntityServletTest {

  @Mock
  HouseEntityService houseService;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  BufferedReader reader;

  @Mock
  PrintWriter printWriter;

  @Mock
  HouseEntityDtoMapper mapper;

  @Mock
  Gson gson;

  HouseEntityServlet servlet;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    servlet = new HouseEntityServlet(houseService, mapper, gson);
  }

  @Test
  void postRequest_ValidRequestData_Successful() throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestHouseEntityDto.class)))
        .thenReturn(new RequestHouseEntityDto());

    HouseEntity savedEntity = new HouseEntity();
    ResponseHouseEntityDto responseDto = new ResponseHouseEntityDto();
    when(mapper.map(any(RequestHouseEntityDto.class))).thenReturn(new HouseEntity());
    when(houseService.save(any(HouseEntity.class))).thenReturn(savedEntity);
    when(mapper.map(any(HouseEntity.class))).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_EntityExistsException_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestHouseEntityDto.class)))
        .thenReturn(new RequestHouseEntityDto());

    when(houseService.save(any(HouseEntity.class))).thenThrow(new EntityExistsException("Test exception"));

    when(mapper.map(any(RequestHouseEntityDto.class))).thenReturn(new HouseEntity());

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_RepositoryException_ReturnsInternalServerError()
      throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestHouseEntityDto.class)))
        .thenReturn(new RequestHouseEntityDto());

    when(houseService.save(any(HouseEntity.class))).thenThrow(new RepositoryException("Test exception"));
    when(mapper.map(any(RequestHouseEntityDto.class))).thenReturn(new HouseEntity());
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void getRequest_WithIdParam_ReturnsOk() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");

    HouseEntity entity = new HouseEntity();
    ResponseHouseEntityDto responseDto = new ResponseHouseEntityDto();
    when(houseService.getById(1L)).thenReturn(entity);
    when(mapper.map(entity)).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void getRequest_WithInvalidIdParam_ReturnsBadRequest() throws IOException {
    when(request.getParameter("id")).thenReturn("invalid_id");

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void getRequest_NoIdParam_ReturnsOk() throws IOException, RepositoryException {
    List<HouseEntity> houseEntities = List.of(new HouseEntity());
    when(houseService.getAll()).thenReturn(houseEntities);

    ResponseHouseEntityDto responseDto = new ResponseHouseEntityDto();
    when(mapper.map(any(HouseEntity.class))).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");

    verify(mapper, times(houseEntities.size())).map(any(HouseEntity.class));
  }

  @Test
  void putRequest_ValidRequestData_SuccessfullyUpdatesHouse()
      throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestHouseEntityDto.class)))
        .thenReturn(new RequestHouseEntityDto());
    when(mapper.map(any(RequestHouseEntityDto.class))).thenReturn(new HouseEntity());

    HouseEntity updatedEntity = new HouseEntity();
    ResponseHouseEntityDto responseDto = new ResponseHouseEntityDto();
    when(houseService.update(any(HouseEntity.class))).thenReturn(updatedEntity);
    when(mapper.map(any(HouseEntity.class))).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void putRequest_InvalidIdParameter_ReturnsBadRequest() throws IOException {
    when(request.getParameter("id")).thenReturn(null);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void putRequest_RepositoryException_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestHouseEntityDto.class)))
        .thenReturn(new RequestHouseEntityDto());
    when(mapper.map(any(RequestHouseEntityDto.class))).thenReturn(new HouseEntity());

    when(houseService.update(any(HouseEntity.class))).thenThrow(new RepositoryException("Test exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_SuccessfulDelete_ReturnsOk() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(houseService.deleteById(1L)).thenReturn(true);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_HouseNotFound_ReturnsNotFound() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(houseService.deleteById(1L)).thenReturn(false);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_InvalidIdParameter_ReturnsBadRequest() throws IOException {
    when(request.getParameter("id")).thenReturn("invalid_id");

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_RepositoryException_ReturnsInternalServerError() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(houseService.deleteById(1L)).thenThrow(new RepositoryException("Test exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }
}
