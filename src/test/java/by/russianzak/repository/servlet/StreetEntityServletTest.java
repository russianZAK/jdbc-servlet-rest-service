package by.russianzak.repository.servlet;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.StreetEntity;
import by.russianzak.service.StreetEntityService;
import by.russianzak.servlet.StreetEntityServlet;
import by.russianzak.servlet.dto.RequestStreetEntityDto;
import by.russianzak.servlet.dto.ResponseStreetEntityDto;
import by.russianzak.servlet.mapper.StreetEntityDtoMapper;
import by.russianzak.servlet.mapper.StreetEntityDtoMapperImpl;
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
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StreetEntityServletTest {

  @Mock
  StreetEntityService streetService;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  BufferedReader reader;

  @Mock
  PrintWriter printWriter;

  StreetEntityDtoMapper mapper;

  StreetEntityServlet servlet;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mapper = new StreetEntityDtoMapperImpl();
    servlet = new StreetEntityServlet(streetService, mapper, new Gson());
  }

  @Test
  void postRequest_ValidRequestData_Successful() throws IOException, RepositoryException {
    when(request.getParameter("name")).thenReturn("First street");
    when(request.getParameter("postalCode")).thenReturn("546");

    String requestData = "{\"key\":\"value\"}";
    reader = new BufferedReader(new StringReader(requestData));
    when(request.getReader()).thenReturn(reader);

    StreetEntity savedEntity = StreetEntity.builder().setName("First street").setPostalCode(546L).setId(1L).build();
    when(streetService.save(any(StreetEntity.class))).thenReturn(savedEntity);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_EntityExistsException_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getParameter("name")).thenReturn("Existing street");
    when(request.getParameter("postalCode")).thenReturn("123");

    String requestData = "{\"key\":\"value\"}";
    reader = new BufferedReader(new StringReader(requestData));
    when(request.getReader()).thenReturn(reader);

    when(streetService.save(any(StreetEntity.class))).thenThrow(new EntityExistsException("Text of exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void postRequest_RepositoryException_ReturnsInternalServerError() throws IOException, RepositoryException {
    when(request.getParameter("name")).thenReturn("Some street");
    when(request.getParameter("postalCode")).thenReturn("789");

    String requestData = "{\"key\":\"value\"}";
    reader = new BufferedReader(new StringReader(requestData));
    when(request.getReader()).thenReturn(reader);

    when(streetService.save(any(StreetEntity.class))).thenThrow(new RepositoryException("Text of exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void putRequest_ValidRequestData_SuccessfullyUpdatesStreet() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(request.getParameter("name")).thenReturn("Updated street");
    when(request.getParameter("postalCode")).thenReturn("999");

    String requestData = "{\"key\":\"value\"}";
    reader = new BufferedReader(new StringReader(requestData));
    when(request.getReader()).thenReturn(reader);

    StreetEntity updatedEntity = StreetEntity.builder().setId(1L).setName("Updated street").setPostalCode(999L).build();
    when(streetService.update(any(StreetEntity.class))).thenReturn(updatedEntity);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void putRequest_InvalidIdParameter_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn(null);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void putRequest_RepositoryException_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(request.getParameter("name")).thenReturn("Updated street");
    when(request.getParameter("postalCode")).thenReturn("999");

    String requestData = "{\"key\":\"value\"}";
    reader = new BufferedReader(new StringReader(requestData));
    when(request.getReader()).thenReturn(reader);

    when(streetService.update(any(StreetEntity.class))).thenThrow(new RepositoryException("sadas"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_SuccessfulDelete_ReturnsOk() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(streetService.deleteById(1L)).thenReturn(true);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_StreetNotFound_ReturnsNotFound() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(streetService.deleteById(1L)).thenReturn(false);

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_InvalidIdParameter_ReturnsBadRequest() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("invalid_id");

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, times(1)).setContentType("application/json");
  }

  @Test
  void deleteRequest_RepositoryException_ReturnsInternalServerError() throws IOException, RepositoryException {
    when(request.getParameter("id")).thenReturn("1");
    when(streetService.deleteById(1L)).thenThrow(new RepositoryException("Text of exception"));

    when(response.getWriter()).thenReturn(printWriter);

    servlet.doDelete(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(response, times(1)).setContentType("application/json");
  }

}
