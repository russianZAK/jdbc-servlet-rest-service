package by.russianzak.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.RoadSurfaceEntity;
import by.russianzak.repository.RoadSurfaceEntityRepository;
import by.russianzak.service.RoadSurfaceEntityService;
import java.util.List;
import java.util.Optional;

public class RoadSurfaceEntityServiceImpl implements RoadSurfaceEntityService {

  private final RoadSurfaceEntityRepository roadSurfaceEntityRepository;

  public RoadSurfaceEntityServiceImpl(RoadSurfaceEntityRepository roadSurfaceEntityRepository) {
    this.roadSurfaceEntityRepository = roadSurfaceEntityRepository;
  }

  @Override
  public RoadSurfaceEntity save(RoadSurfaceEntity entity) throws RepositoryException {
    return roadSurfaceEntityRepository.save(entity);
  }

  @Override
  public RoadSurfaceEntity getById(Long id) throws RepositoryException {
    return roadSurfaceEntityRepository.getById(id);
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    return roadSurfaceEntityRepository.deleteById(id);
  }

  @Override
  public boolean delete(RoadSurfaceEntity entity) throws RepositoryException {
    return roadSurfaceEntityRepository.delete(entity);
  }

  @Override
  public RoadSurfaceEntity update(RoadSurfaceEntity entity) throws RepositoryException {
    return roadSurfaceEntityRepository.update(entity);
  }

  @Override
  public Optional<RoadSurfaceEntity> findById(Long id) throws RepositoryException {
    return roadSurfaceEntityRepository.findById(id);
  }

  @Override
  public List<RoadSurfaceEntity> getAll() throws RepositoryException {
    return roadSurfaceEntityRepository.getAll();
  }

}
