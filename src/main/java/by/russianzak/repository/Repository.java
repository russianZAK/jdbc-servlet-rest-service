package by.russianzak.repository;

import by.russianzak.exception.RepositoryException;
import java.util.List;
import java.util.Optional;

public interface Repository<T, K> {

  T save(T entity) throws RepositoryException;

  T getById(K id) throws RepositoryException;

  boolean deleteById(K id) throws RepositoryException;

  boolean delete(T entity) throws RepositoryException;

  T update(T entity) throws RepositoryException;

  Optional<T> findById(K id) throws RepositoryException;

  List<T> getAll() throws RepositoryException;
}