package by.russianzak.servlet;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.service.HouseEntityService;
import by.russianzak.servlet.dto.RequestHouseEntityDto;
import by.russianzak.servlet.dto.ResponseHouseEntityDto;
import by.russianzak.servlet.response.WebResponse;
import by.russianzak.servlet.mapper.HouseEntityDtoMapper;
import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class HouseEntityServlet extends HttpServlet {
  private final HouseEntityService houseService;
  private final HouseEntityDtoMapper mapper;
  private final Gson gson;

  public HouseEntityServlet(HouseEntityService houseService, HouseEntityDtoMapper mapper, Gson gson) {
    this.houseService = houseService;
    this.mapper = mapper;
    this.gson = gson;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      processPostRequest(req, resp);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    processGetRequest(req, resp);
  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse resp) {
    try {
      processPutRequest(req, resp);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    processDeleteRequest(req, resp);
  }

  private void processPostRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    RequestHouseEntityDto requestDto = getRequestDto(req);
    HouseEntity entity = mapper.map(requestDto);
    try {
      HouseEntity savedEntity = houseService.save(entity);
      ResponseHouseEntityDto responseDto = mapper.map(savedEntity);
      sendResponse(resp, HttpServletResponse.SC_CREATED, responseDto);
    }catch (EntityExistsException e) {
      sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }catch (JsonSyntaxException e) {
      sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getCause().getMessage());
    }
    catch (RepositoryException e) {
      sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private void processGetRequest(HttpServletRequest req, HttpServletResponse resp) {
    String idParam = req.getParameter("id");
    if (idParam != null) {
      try {
        Long id = Long.parseLong(idParam);
        HouseEntity entity = houseService.getById(id);
        ResponseHouseEntityDto responseDto = mapper.map(entity);
        sendResponse(resp, HttpServletResponse.SC_OK, responseDto);
      } catch (NumberFormatException | EntityNotFoundException  e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
      } catch (JsonSyntaxException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getCause().getMessage());
      }
      catch (RepositoryException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } else {
      try {
        List<HouseEntity> entities = houseService.getAll();
        List<ResponseHouseEntityDto> responseDtos = entities.stream()
            .map(mapper::map)
            .toList();
        sendResponse(resp, HttpServletResponse.SC_OK, responseDtos);
      } catch (RepositoryException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    }
  }

  private void processPutRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String idParam = req.getParameter("id");
    if (idParam != null) {
      long id = Long.parseLong(idParam);
      RequestHouseEntityDto requestDto = getRequestDto(req);
      HouseEntity entity = mapper.map(requestDto);
      entity.setId(id);
      try {
        HouseEntity updatedEntity = houseService.update(entity);
        ResponseHouseEntityDto responseDto = mapper.map(updatedEntity);
        sendResponse(resp, HttpServletResponse.SC_OK, responseDto);
      } catch (RepositoryException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
      } catch (JsonSyntaxException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getCause().getMessage());
      }
    } else {
      sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
    }
  }

  private void processDeleteRequest(HttpServletRequest req, HttpServletResponse resp) {
    String idParam = req.getParameter("id");
    if (idParam != null) {
      try {
        Long id = Long.parseLong(idParam);
        boolean isDeleted = houseService.deleteById(id);
        if (isDeleted) {
          sendResponse(resp, HttpServletResponse.SC_OK, new WebResponse(HttpServletResponse.SC_OK, "Deleted successfully"));
        } else {
          sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "House not found or already deleted");
        }
      } catch (NumberFormatException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
      } catch (JsonSyntaxException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getCause().getMessage());
      } catch (RepositoryException e) {
        sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } else {
      sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
    }
  }


  private RequestHouseEntityDto getRequestDto(HttpServletRequest req) throws IOException {
    try (BufferedReader reader = req.getReader()) {
      return gson.fromJson(reader, RequestHouseEntityDto.class);
    }
  }

  private void sendResponse(HttpServletResponse resp, int status, Object responseObject) {
    try {
      resp.setContentType("application/json");
      resp.setStatus(status);
      try (PrintWriter writer = resp.getWriter()) {
        writer.print(gson.toJson(responseObject));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendErrorResponse(HttpServletResponse resp, int status, String errorMessage) {
    WebResponse webResponse = new WebResponse(status, errorMessage);
    sendResponse(resp, status, webResponse);
  }
}
