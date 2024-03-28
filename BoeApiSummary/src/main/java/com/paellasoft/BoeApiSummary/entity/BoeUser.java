package com.paellasoft.BoeApiSummary.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "boe_user")
public class BoeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "boe_id")
    private Boe boe;

    // Constructor, getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boe getBoe() {
        return boe;
    }

    public void setBoe(Boe boe) {
        this.boe = boe;
    }
}