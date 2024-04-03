package com.paellasoft.BoeApiSummary.Repository.custom;

import com.paellasoft.BoeApiSummary.entity.Boe;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@NoRepositoryBean
public interface ICustomRepository<T,ID> {

    List<T> findNotSubscribedBoes2(Long userId);

}
