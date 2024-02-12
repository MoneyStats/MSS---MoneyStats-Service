package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAssetDAO extends JpaRepository<AssetEntity, Long> {

    List<AssetEntity> findAllByUserIdOrderByRank(Long userId);
}
