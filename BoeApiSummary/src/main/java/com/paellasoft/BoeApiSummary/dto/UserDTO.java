package com.paellasoft.BoeApiSummary.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean sendNotification;
    private List<Long> subscriptionsIds;
}
