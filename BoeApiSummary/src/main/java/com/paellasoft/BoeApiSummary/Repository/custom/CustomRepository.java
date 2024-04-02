package com.paellasoft.BoeApiSummary.Repository.custom;

import com.paellasoft.BoeApiSummary.entity.Boe;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class CustomRepository<T,ID> implements ICustomRepository<T,ID>{


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Boe> findNotSubscribedBoes2(Long userId) {
        String hql = "SELECT b FROM Boe b " +
                "WHERE b NOT IN " +
                "(SELECT bu.boe FROM BoeUser bu " +
                "WHERE bu.user.id = :userId)";
        Query query = entityManager.createQuery(hql);
        query.setParameter("userId", userId);
        return query.getResultList();
    }




    }

