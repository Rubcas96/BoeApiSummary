package com.paellasoft.BoeApiSummary.Repository;


import com.paellasoft.BoeApiSummary.Repository.custom.ICustomRepository;
import com.paellasoft.BoeApiSummary.entity.Boe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IBoeRepository extends JpaRepository<Boe, Long>, ICustomRepository<Boe, Long> {

    Boe findTopByOrderByFechaBoeDesc();

    //@Query("SELECT b FROM Boe b WHERE b.id NOT IN (SELECT bu.boe.id FROM BoeUser bu WHERE bu.user.id = :userId)")
    //List<Boe> findNotSubscribedBoes(@Param("userId") Long userId);

    //List<Boe> findNotSubscribedBoes2(Long userId);
}