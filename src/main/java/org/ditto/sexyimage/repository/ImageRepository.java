package org.ditto.sexyimage.repository;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.ditto.sexyimage.common.grpc.ImageType;
import org.ditto.sexyimage.model.Image;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryConfig(cacheName = "ImageCache")
public interface ImageRepository extends IgniteRepository<Image, String> {

    @Query("SELECT * FROM Image WHERE type = ? AND lastUpdated > ? ORDER BY lastUpdated ASC")
    List<Image> getAllBy(ImageType type, long startLastUpdated, Pageable pageable);

    @Query("SELECT * FROM Image WHERE type = ? ORDER BY visitCount DESC")
    List<Image> getTopRankBy(ImageType it, Pageable pageable);

    Image findByUrl(String url);
}