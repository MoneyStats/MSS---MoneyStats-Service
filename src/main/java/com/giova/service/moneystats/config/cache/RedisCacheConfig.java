package com.giova.service.moneystats.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

  public static final String REDIS_ERROR_LOG =
      "[Redis] Unable to connect to Redis, falling back to database: {}";

  @Bean
  public RedisTemplate<String, List<WalletEntity>> walletEntitiesTemplate(
      RedisConnectionFactory factory, ObjectMapper mapper) {

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    Jackson2JsonRedisSerializer<List<WalletEntity>> valueSerializer =
        new Jackson2JsonRedisSerializer<>(
            mapper.getTypeFactory().constructCollectionType(List.class, WalletEntity.class));

    RedisTemplate<String, List<WalletEntity>> template = new RedisTemplate<>();
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
