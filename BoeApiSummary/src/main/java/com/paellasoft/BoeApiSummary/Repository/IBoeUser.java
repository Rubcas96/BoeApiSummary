package com.paellasoft.BoeApiSummary.Repository;


import com.paellasoft.BoeApiSummary.entity.BoeUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBoeUser extends JpaRepository<BoeUser, Long> {

}
