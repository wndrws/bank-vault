package kspt.bank.dao;

import kspt.bank.boundaries.CellsRepository;
import kspt.bank.domain.entities.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaCellsRepository extends CellsRepository, JpaRepository<Cell, Integer> {
    @Override
    @Query("SELECT c FROM Cell c WHERE pending=true")
    List<Cell> findAllPendingCells();

    @Override
    @Query("SELECT c.pending FROM Cell c WHERE c=:cell")
    boolean isPending(@Param("cell") final Cell cell);
}
