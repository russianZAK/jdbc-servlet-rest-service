package by.russianzak.repository.servlet;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.service.RoadSurfaceEntityService;
import by.russianzak.servlet.RoadSurfaceEntityServlet;
import by.russianzak.servlet.dto.RequestRoadSurfaceEntityDto;
import by.russianzak.servlet.dto.ResponseRoadSurfaceEntityDto;
import by.russianzak.servlet.mapper.RoadSurfaceEntityDtoMapper;
import com.google.gson.Gson;
import jakarta.persistence.EntityExistsException;
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

class RoadSurfaceEntityServletTest {

  @Mock
  RoadSurfaceEntityService roadSurfaceService;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  BufferedReader reader;

  @Mock
  PrintWriter printWriter;

  @Mock
  RoadSurfaceEntityDtoMapper mapper;

  @Mock
  Gson gson;

  RoadSurfaceEntityServlet servlet;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    servlet = new RoadSurfaceEntityServlet(roadSurfaceService, mapper, gson);
  }

  @Test
  void postRequest_ValidRequestData_Successful() throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestRoadSurfaceEntityDto.class)))
        .thenReturn(new RequestRoadSurfaceEntityDto());

    RoadSurfaceEntity savedEntity = new RoadSurfaceEntity();
    ResponseRoadSurfaceEntityDto responseDto = new ResponseRoadSurfaceEntityDto();
    when(mapper.map(any(RequestRoadSurfaceEntityDto.class))).thenReturn(new RoadSurfaceEntity());
    when(roadSurfaceService.save(any(RoadSurfaceEntity.class))).thenReturn(savedEntity);
    when(mapper.map(any(RoadSurfaceEntity.class))).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_EntityExistsException_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestRoadSurfaceEntityDto.class)))
        .thenReturn(new RequestRoadSurfaceEntityDto());

    when(roadSurfaceService.save(any(RoadSurfaceEntity.class))).thenThrow(new EntityExistsException("Test exception"));

    when(mapper.map(any(RequestRoadSurfaceEntityDto.class))).thenReturn(new RoadSurfaceEntity());

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_RepositoryException_ReturnsInternalServerError()
      throws IOException, RepositoryException {
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestRoadSurfaceEntityDto.class)))
        .thenReturn(new RequestRoadSurfaceEntityDto());

    when(roadSurfaceService.save(any(RoadSurfaceEntity.class))).thenThrow(new RepositoryException("Test exception"));
    when(mapper.map(any(RequestRoadSurfaceEntityDto.class))).thenReturn(new RoadSurfaceEntity());
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void getRequest_WithIdParam_ReturnsOk() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");

    RoadSurfaceEntity entity = new RoadSurfaceEntity();
    ResponseRoadSurfaceEntityDto responseDto = new ResponseRoadSurfaceEntityDto();
    when(roadSurfaceService.getById(1L)).thenReturn(entity);
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
    List<RoadSurfaceEntity> roadSurfaceEntities = List.of(new RoadSurfaceEntity());
    when(roadSurfaceService.getAll()).thenReturn(roadSurfaceEntities);

    ResponseRoadSurfaceEntityDto responseDto = new ResponseRoadSurfaceEntityDto();
    when(mapper.map(any(RoadSurfaceEntity.class))).thenReturn(responseDto);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");

    verify(mapper, times(roadSurfaceEntities.size())).map(any(RoadSurfaceEntity.class));
  }

  @Test
  void putRequest_ValidRequestData_SuccessfullyUpdatesRoadSurface()
      throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(request.getReader()).thenReturn(reader);
    when(gson.fromJson(any(BufferedReader.class), eq(RequestRoadSurfaceEntityDto.class)))
        .thenReturn(new RequestRoadSurfaceEntityDto());
    when(mapper.map(any(RequestRoadSurfaceEntityDto.class))).thenReturn(new RoadSurfaceEntity());

    RoadSurfaceEntity updatedEntity = new RoadSurfaceEntity();
    ResponseRoadSurfaceEntityDto responseDto = new ResponseRoadSurfaceEntityDto();
    when(roadSurfaceService.update(any(RoadSurfaceEntity.class))).thenReturn(updatedEntity);
    when(mapper.map(any(RoadSurfaceEntity.class))).thenReturn(responseDto);

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
    when(gson.fromJson(any(BufferedReader.class), eq(RequestRoadSurfaceEntityDto.class)))
        .thenReturn(new RequestRoadSurfaceEntityDto());
    when(mapper.map(any(RequestRoadSurfaceEntityDto.class))).thenReturn(new RoadSurfaceEntity());

    when(roadSurfaceService.update(any(RoadSurfaceEntity.class))).thenThrow(new RepositoryException("Test exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_SuccessfulDelete_ReturnsOk() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(roadSurfaceService.deleteById(1L)).thenReturn(true);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_RoadSurfaceNotFound_ReturnsNotFound() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(roadSurfaceService.deleteById(1L)).thenReturn(false);

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
    when(roadSurfaceService.deleteById(1L)).thenThrow(new RepositoryException("Test exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }
}
