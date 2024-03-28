package com.paellasoft.BoeApiSummary.Repository;


import com.paellasoft.BoeApiSummary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User,Long> {
    User findByUsernameAndPassword(String username, String password);

}
