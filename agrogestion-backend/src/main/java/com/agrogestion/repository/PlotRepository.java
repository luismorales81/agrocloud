package com.agrogestion.repository;

import com.agrogestion.model.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlotRepository extends JpaRepository<Plot, Long> {
}
