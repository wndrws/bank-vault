package kspt.bank.dao;

import kspt.bank.boundaries.ApplicationsRepository;
import kspt.bank.domain.entities.CellApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaApplicationsRepository extends ApplicationsRepository, JpaRepository<CellApplication, Integer> {
    @Override
    default CellApplication find(Integer id) {
        return findById(id).orElse(null);
    }
}
