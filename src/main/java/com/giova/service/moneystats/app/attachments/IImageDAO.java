package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IImageDAO extends JpaRepository<ImageEntity, Long> {}
