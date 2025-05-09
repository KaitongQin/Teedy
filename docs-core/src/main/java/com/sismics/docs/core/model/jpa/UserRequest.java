package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Represents a user request entity.
 */
@Entity
@Table(name = "T_USER_REQUEST")
public class UserRequest {
    @Id
    @Column(name = "USR_ID_C", length = 36)
    private String id;

    @Column(name = "USR_USERNAME_C", nullable = false, length = 50)
    private String username;

    @Column(name = "USR_EMAIL_C", nullable = false, length = 100)
    private String email;

    @Column(name = "USR_PASSWORD_C", nullable = false, length = 100)
    private String password;

    @Column(name = "USR_STATUS_C", nullable = false, length = 20)
    private String status; // pending, accepted, rejected

    @Column(name = "USR_CREATEDATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

}