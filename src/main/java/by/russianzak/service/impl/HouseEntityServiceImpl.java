package by.russianzak.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.HouseEntity;
import by.russianzak.repository.HouseEntityRepository;
import by.russianzak.service.HouseEntityService;
import java.util.List;
import java.util.Optional;

public class HouseEntityServiceImpl implements HouseEntityService {

  private final HouseEntityRepository houseEntityRepository;

  public HouseEntityServiceImpl(HouseEntityRepository houseEntityRepository) {
    this.houseEntityRepository = houseEntityRepository;
  }

  @Override
  public HouseEntity save(HouseEntity entity) throws RepositoryException {
    return houseEntityRepository.save(entity);
  }

  @Override
  public HouseEntity getById(Long id) throws RepositoryException {
    return houseEntityRepository.getById(id);
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    return houseEntityRepository.deleteById(id);
  }

  @Override
  public boolean delete(HouseEntity entity) throws RepositoryException {
    return houseEntityRepository.delete(entity);
  }

  @Override
  public HouseEntity update(HouseEntity entity) throws RepositoryException {
    return houseEntityRepository.update(entity);
  }

  @Override
  public Optional<HouseEntity> findById(Long id) throws RepositoryException {
    return houseEntityRepository.findById(id);
  }

  @Override
  public List<HouseEntity> getAll() throws RepositoryException {
    return houseEntityRepository.getAll();
  }
}
