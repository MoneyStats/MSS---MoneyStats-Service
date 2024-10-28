package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ImageMapper {

  private static final Logger LOG = LoggerFactory.getLogger(ImageMapper.class);

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static Mono<Image> fromPartToDto(FilePart file, Long maxSize) {
    AtomicReference<Integer> size = new AtomicReference<>(0);
    Mono<byte[]> getByteArray =
        DataBufferUtils.join(file.content())
            .map(
                dataBuffer -> {
                  try {
                    size.set(dataBuffer.capacity());
                    return dataBuffer.asInputStream().readAllBytes();
                  } catch (IOException e) {
                    LOG.error("Error on converting Attachment: {}", e.getMessage());
                    throw new ImageException(
                        ExceptionMap.ERR_IMG_MSS_001.getMessage()
                            + " with filename: "
                            + file.filename(),
                        e.getMessage());
                  }
                });
    String contentType =
        Utilities.isNullOrEmpty(file.headers().getContentType())
            ? null
            : Objects.requireNonNull(file.headers().getContentType()).toString();
    return getByteArray.map(
        bytes -> {
          if (size.get() > maxSize) {
            LOG.error("Image exceed the max size of {}, current size is: {}", maxSize, size.get());
            throw new ImageException(ExceptionMap.ERR_IMG_MSS_002);
          }
          return new Image(file.filename(), file.filename(), contentType, size.get(), bytes);
        });
  }
}
