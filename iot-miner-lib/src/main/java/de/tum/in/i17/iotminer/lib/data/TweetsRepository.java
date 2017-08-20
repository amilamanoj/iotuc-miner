package de.tum.in.i17.iotminer.lib.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by amilamanoj on 24.06.17.
 */
public interface TweetsRepository extends CrudRepository<Tweets, Long> {
    Page<Tweets> findAll(Pageable pageable);


}
