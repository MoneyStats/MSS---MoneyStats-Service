package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAssetDAO extends JpaRepository<AssetEntity, Long> {
}
