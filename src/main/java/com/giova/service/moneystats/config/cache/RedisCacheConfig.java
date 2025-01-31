package com.giova.service.moneystats.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

  public static final String REDIS_ERROR_LOG =
      "[Redis] Unable to connect to Redis, falling back to database: {}";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Bean
  public RedisTemplate<String, String> walletEntitiesTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<String> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, String.class);

    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  @Bean
  public RedisTemplate<String, WalletEntity> walletEntityTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<WalletEntity> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, WalletEntity.class);

    RedisTemplate<String, WalletEntity> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  @Bean
  public RedisTemplate<String, List<AssetEntity>> assetEntityTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<List<AssetEntity>> valueSerializer =
        new Jackson2JsonRedisSerializer<>(
            mapper.getTypeFactory().constructCollectionType(List.class, AssetEntity.class));

    RedisTemplate<String, List<AssetEntity>> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  @Bean
  public RedisTemplate<String, List<AssetLivePrice>> assetLivePriceTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<List<AssetLivePrice>> valueSerializer =
        new Jackson2JsonRedisSerializer<>(
            mapper.getTypeFactory().constructCollectionType(List.class, AssetLivePrice.class));

    RedisTemplate<String, List<AssetLivePrice>> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  @Bean
  public RedisTemplate<String, List<AssetWithoutOpAndStats>> assetWithoutOpAndStatsTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<List<AssetWithoutOpAndStats>> valueSerializer =
        new Jackson2JsonRedisSerializer<>(
            mapper
                .getTypeFactory()
                .constructCollectionType(List.class, AssetWithoutOpAndStats.class));

    RedisTemplate<String, List<AssetWithoutOpAndStats>> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  /**
   * MarketData Cache Config
   *
   * @param factory Redis
   * @param mapper ObjectMapper
   * @return MarketDataTemplate
   */
  @Bean
  public RedisTemplate<String, String> marketDataEntityTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<String> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, String.class);

    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  /**
   * ForexData Cache Config
   *
   * @param factory Redis
   * @param mapper ObjectMapper
   * @return ForexDataTemplate
   */
  @Bean
  public RedisTemplate<String, ForexDataEntity> forexDataEntityTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<ForexDataEntity> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, ForexDataEntity.class);

    RedisTemplate<String, ForexDataEntity> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  /**
   * ForexData List Cache Config
   *
   * @param factory Redis
   * @param mapper ObjectMapper
   * @return ForexDataTemplate
   */
  @Bean
  public RedisTemplate<String, List<ForexDataEntity>> forexDataEntitiesTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<List<ForexDataEntity>> valueSerializer =
        new Jackson2JsonRedisSerializer<>(
            mapper.getTypeFactory().constructCollectionType(List.class, ForexDataEntity.class));

    RedisTemplate<String, List<ForexDataEntity>> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(keySerializer);
    template.setValueSerializer(valueSerializer);

    return template;
  }

  /**
   * Caching Access Sphere UserInfo
   *
   * @param factory Redis
   * @param mapper ObjectMapper
   * @return UserInfoTemplate
   */
  @Bean
  public ReactiveRedisTemplate<String, UserData> userDataTemplate(
      ReactiveRedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<UserData> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, UserData.class);

    RedisSerializationContext.RedisSerializationContextBuilder<String, UserData> builder =
        RedisSerializationContext.newSerializationContext(keySerializer);
    RedisSerializationContext<String, UserData> context = builder.value(valueSerializer).build();

    return new ReactiveRedisTemplate<>(factory, context);
  }

  // @Bean
  // public ReactiveRedisTemplate<String, WalletEntity> walletEntityTemplate(
  //    ReactiveRedisConnectionFactory factory, ObjectMapper mapper) {
  //  StringRedisSerializer keySerializer = new StringRedisSerializer();
  //  Jackson2JsonRedisSerializer<WalletEntity> valueSerializer =
  //      new Jackson2JsonRedisSerializer<>(mapper, WalletEntity.class);
  //  RedisSerializationContext.RedisSerializationContextBuilder<String, WalletEntity> builder =
  //      RedisSerializationContext.newSerializationContext(keySerializer);
  //  RedisSerializationContext<String, WalletEntity> context =
  //      builder.value(valueSerializer).build();
  //  return new ReactiveRedisTemplate<>(factory, context);
  // }
}
