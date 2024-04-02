package by.russianzak.service.impl;

import by.russianzak.exception.RepositoryException;
import by.russianzak.model.StreetEntity;
import by.russianzak.repository.StreetEntityRepository;
import by.russianzak.service.StreetEntityService;
import java.util.List;
import java.util.Optional;

public class StreetEntityServiceImpl implements StreetEntityService {
  private final StreetEntityRepository streetRepository;

  public StreetEntityServiceImpl(StreetEntityRepository streetRepository) {
    this.streetRepository = streetRepository;
  }

  @Override
  public StreetEntity save(StreetEntity entity) throws RepositoryException {
    return streetRepository.save(entity);
  }

  @Override
  public StreetEntity getById(Long id) throws RepositoryException {
    return streetRepository.getById(id);
  }

  @Override
  public boolean deleteById(Long id) throws RepositoryException {
    return streetRepository.deleteById(id);
  }

  @Override
  public boolean delete(StreetEntity entity) throws RepositoryException {
    return streetRepository.delete(entity);
  }

  @Override
  public StreetEntity update(StreetEntity entity) throws RepositoryException {
    return streetRepository.update(entity);
  }

  @Override
  public List<StreetEntity> getAll() throws RepositoryException {
    return streetRepository.getAll();
  }

  @Override
  public Optional<StreetEntity> findById(Long id) throws RepositoryException {
    return streetRepository.findById(id);
  }

}
