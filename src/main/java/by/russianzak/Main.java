package by.russianzak;

import by.russianzak.db.ConnectionManager;
import by.russianzak.db.impl.ConnectionManagerImpl;
import by.russianzak.repository.HouseEntityRepository;
import by.russianzak.repository.RoadSurfaceEntityRepository;
import by.russianzak.repository.StreetEntityRepository;
import by.russianzak.repository.impl.HouseEntityRepositoryImpl;
import by.russianzak.repository.impl.RoadSurfaceEntityRepositoryImpl;
import by.russianzak.repository.impl.StreetEntityRepositoryImpl;
import by.russianzak.repository.mapper.HouseResultSetMapper;
import by.russianzak.repository.mapper.HouseResultSetMapperImpl;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapper;
import by.russianzak.repository.mapper.RoadSurfaceResultSetMapperImpl;
import by.russianzak.repository.mapper.StreetResultSetMapper;
import by.russianzak.repository.mapper.StreetResultSetMapperImpl;
import by.russianzak.service.HouseEntityService;
import by.russianzak.service.RoadSurfaceEntityService;
import by.russianzak.service.StreetEntityService;
import by.russianzak.service.impl.HouseEntityServiceImpl;
import by.russianzak.service.impl.RoadSurfaceEntityServiceImpl;
import by.russianzak.service.impl.StreetEntityServiceImpl;
import by.russianzak.servlet.HouseEntityServlet;
import by.russianzak.servlet.RoadSurfaceEntityServlet;
import by.russianzak.servlet.StreetEntityServlet;
import by.russianzak.servlet.mapper.HouseEntityDtoMapper;
import by.russianzak.servlet.mapper.HouseEntityDtoMapperImpl;
import by.russianzak.servlet.mapper.RoadSurfaceEntityDtoMapper;
import by.russianzak.servlet.mapper.RoadSurfaceEntityDtoMapperImpl;
import by.russianzak.servlet.mapper.StreetEntityDtoMapper;
import by.russianzak.servlet.mapper.StreetEntityDtoMapperImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class Main {

  public static void main(String[] args) throws LifecycleException {
    Gson gson = new GsonBuilder()
        .setDateFormat("dd-MM-yyyy")
        .create();
    ConnectionManager connectionManager = new ConnectionManagerImpl();
    HouseResultSetMapper houseResultSetMapper = new HouseResultSetMapperImpl();
    StreetResultSetMapper streetResultSetMapper = new StreetResultSetMapperImpl();
    RoadSurfaceResultSetMapper roadSurfaceResultSetMapper = new RoadSurfaceResultSetMapperImpl();
    HouseEntityRepository houseEntityRepository = new HouseEntityRepositoryImpl(houseResultSetMapper, streetResultSetMapper,
        connectionManager);
    StreetEntityRepository streetEntityRepository = new StreetEntityRepositoryImpl(houseResultSetMapper, streetResultSetMapper, roadSurfaceResultSetMapper,
        connectionManager);
    RoadSurfaceEntityRepository roadSurfaceEntityRepository = new RoadSurfaceEntityRepositoryImpl(roadSurfaceResultSetMapper, streetResultSetMapper,
        connectionManager);

    HouseEntityService houseEntityService = new HouseEntityServiceImpl(houseEntityRepository);
    StreetEntityService streetEntityService = new StreetEntityServiceImpl(streetEntityRepository);
    RoadSurfaceEntityService roadSurfaceEntityService = new RoadSurfaceEntityServiceImpl(roadSurfaceEntityRepository);

    HouseEntityDtoMapper houseEntityDtoMapper = new HouseEntityDtoMapperImpl();
    StreetEntityDtoMapper streetEntityDtoMapper = new StreetEntityDtoMapperImpl();
    RoadSurfaceEntityDtoMapper roadSurfaceEntityDtoMapper = new RoadSurfaceEntityDtoMapperImpl();

    HttpServlet houseEntityServlet = new HouseEntityServlet(houseEntityService, houseEntityDtoMapper,
        gson);
    HttpServlet streetEntityServlet = new StreetEntityServlet(streetEntityService, streetEntityDtoMapper, gson);
    HttpServlet roadSurfaceEntityServlet = new RoadSurfaceEntityServlet(roadSurfaceEntityService, roadSurfaceEntityDtoMapper,
        gson);


    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8080);
    tomcat.getConnector();

    Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());


    Tomcat.addServlet(ctx, "house", houseEntityServlet);
    ctx.addServletMappingDecoded("/house/*", "house");

    Tomcat.addServlet(ctx, "street", streetEntityServlet);
    ctx.addServletMappingDecoded("/street/*", "street");

    Tomcat.addServlet(ctx, "road-surface", roadSurfaceEntityServlet);
    ctx.addServletMappingDecoded("/road-surface/*", "road-surface");


    tomcat.start();
    tomcat.getServer().await();
  }
}

