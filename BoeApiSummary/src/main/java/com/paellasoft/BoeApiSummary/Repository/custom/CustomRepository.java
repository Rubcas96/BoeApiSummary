package com.paellasoft.BoeApiSummary.Repository.custom;

import com.paellasoft.BoeApiSummary.entity.Boe;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.util.List;
@Repository
public class CustomRepository<T,ID> implements ICustomRepository<T,ID>{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<T> findNotSubscribedBoes2(Long userId) {
        return null;
    }




    }

