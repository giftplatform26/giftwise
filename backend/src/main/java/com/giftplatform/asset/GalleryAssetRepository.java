package com.giftplatform.asset;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GalleryAssetRepository extends JpaRepository<GalleryAsset,UUID> {
 List<GalleryAsset> findByPublishedTrueOrderByTitleAsc();
}
