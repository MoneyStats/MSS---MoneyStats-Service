package com.giova.service.moneystats.crypto.forex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.api.forex.anyApi.dto.Rates;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ForexDataMapper {

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static ForexData fromEntityToForexData(ForexDataEntity forexDataEntity) {
    ForexData forexData = new ForexData();
    BeanUtils.copyProperties(forexDataEntity, forexData);
    forexData.setQuotes(
        Mapper.readObject(
            forexDataEntity.getQuotes(), new TypeReference<Map<String, Double>>() {}));
    return forexData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<ForexData> fromEntitiesToForexDataList(
      List<ForexDataEntity> forexDataEntities) {
    return forexDataEntities.stream().map(ForexDataMapper::fromEntityToForexData).toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static ForexDataEntity fromForexDataToEntity(ForexData forexData) {
    ForexDataEntity forexDataEntity = new ForexDataEntity();
    forexDataEntity.setCurrency(forexData.getCurrency());
    forexDataEntity.setLastUpdate(forexData.getLastUpdate());
    forexDataEntity.setQuotes(Mapper.writeObjectToString(forexData.getQuotes()));
    return forexDataEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static ForexData fromRatesToForexData(Rates exchangeRates) {
    return new ForexData(LocalDateTime.now(), exchangeRates.getBase(), exchangeRates.getRates());
  }
}
