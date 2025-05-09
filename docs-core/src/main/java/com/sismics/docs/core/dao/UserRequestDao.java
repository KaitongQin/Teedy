package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserRequestDao {
    public void create(UserRequest req) {
        req.setId(UUID.randomUUID().toString());
        req.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(req);
    }

    public void update(UserRequest req) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(req);
    }

    public UserRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(UserRequest.class, id);
    }

    public UserRequest findPendingByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from UserRequest r where r.username = :username and r.status = 'pending'");
            q.setParameter("username", username);
            return (UserRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserRequest> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from UserRequest r order by r.createDate desc");
        return q.getResultList();
    }
}
