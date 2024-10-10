package com.giova.service.moneystats.crypto.operations;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import com.giova.service.moneystats.crypto.operations.entity.OperationsEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationsMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Operations> fromOperationsEntitiesToDTOS(
      List<OperationsEntity> operationsEntities) {
    return operationsEntities.stream()
        .map(
            operationsEntity -> {
              Operations operations = new Operations();
              BeanUtils.copyProperties(operationsEntity, operations);
              return operations;
            })
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public Operations fromOperationEntityToDTO(OperationsEntity operationsEntity) {
    Operations operations = new Operations();
    BeanUtils.copyProperties(operationsEntity, operations);
    return operations;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public OperationsEntity fromOperationDTOToEntity(
      Operations operations, UserEntity user, AssetEntity asset) {
    OperationsEntity operationsEntity = new OperationsEntity();
    BeanUtils.copyProperties(operations, operationsEntity);
    operationsEntity.setUser(user);
    operationsEntity.setAsset(asset);
    return operationsEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public List<OperationsEntity> fromOperationDTOSToEntities(
      List<Operations> operations, UserEntity user, AssetEntity asset) {
    return operations.stream()
        .map(
            operation -> {
              OperationsEntity operationsEntity = new OperationsEntity();
              BeanUtils.copyProperties(operation, operationsEntity);
              operationsEntity.setUser(user);
              operationsEntity.setAsset(asset);
              return operationsEntity;
            })
        .collect(Collectors.toList());
  }
}
