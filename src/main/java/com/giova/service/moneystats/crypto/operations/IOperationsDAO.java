package com.giova.service.moneystats.crypto.operations;

import com.giova.service.moneystats.crypto.operations.entity.OperationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOperationsDAO extends JpaRepository<OperationsEntity, Long> {}
