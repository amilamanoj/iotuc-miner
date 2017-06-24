package de.tum.in.i17.iotminer.lib.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by amilamanoj on 24.06.17.
 */
public interface UseCaseRepository extends CrudRepository<UseCase, Long> {

    List<UseCase> findByIndustries_Name(String name);
}
