package com.giova.service.moneystats.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {
  @Bean
  public ReactiveRedisTemplate<String, WalletEntity> walletEntityTemplate(
      ReactiveRedisConnectionFactory factory, ObjectMapper mapper) {
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<WalletEntity> valueSerializer =
        new Jackson2JsonRedisSerializer<>(mapper, WalletEntity.class);
    RedisSerializationContext.RedisSerializationContextBuilder<String, WalletEntity> builder =
        RedisSerializationContext.newSerializationContext(keySerializer);
    RedisSerializationContext<String, WalletEntity> context =
        builder.value(valueSerializer).build();
    return new ReactiveRedisTemplate<>(factory, context);
  }
}
