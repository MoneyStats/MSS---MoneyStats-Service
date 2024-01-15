package com.giova.service.moneystats.crypto.forex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.api.forex.dto.ExchangeRates;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ForexDataMapper {

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public ForexData fromExchangeRatesToForexData(ExchangeRates exchangeRates) {
    return new ForexData(LocalDateTime.now(), exchangeRates.getSource(), exchangeRates.getQuotes());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public ForexDataEntity fromForexDataToEntity(ForexData forexData) {
    ForexDataEntity forexDataEntity = new ForexDataEntity();
    forexDataEntity.setCurrency(forexData.getCurrency());
    forexDataEntity.setLastUpdate(forexData.getLastUpdate());
    try {
      forexDataEntity.setQuotes(mapper.writeValueAsString(forexData.getQuotes()));
    } catch (JsonProcessingException e) {
      LOG.error("An Error happen during mapping quotes for Forex");
      throw new ForexDataException("An Error happen during mapping quotes for Forex");
    }
    return forexDataEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public ForexData fromEntityToForexData(ForexDataEntity forexDataEntity) {
    ForexData forexData = new ForexData();
    BeanUtils.copyProperties(forexDataEntity, forexData);
    try {
      forexData.setQuotes(
          mapper.readValue(
              forexDataEntity.getQuotes(), new TypeReference<Map<String, Double>>() {}));
    } catch (JsonProcessingException e) {
      LOG.error("An Error happen during mapping quotes for Forex");
      throw new ForexDataException("An Error happen during mapping quotes for Forex");
    }
    return forexData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public List<ForexData> fromEntitiesToForexDataList(List<ForexDataEntity> forexDataEntities) {
    return forexDataEntities.stream().map(this::fromEntityToForexData).collect(Collectors.toList());
  }
}
