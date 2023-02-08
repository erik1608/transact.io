package com.snee.transactio.db.entities.user;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users_biometry")
public class Biometrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne
    private User user;

    @OneToOne
    private UserDevice device;

    @Column(length = 2048)
    private String pubKeyBase64;

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UserDevice getDevice() {
        return device;
    }

    public String getPubKeyBase64() {
        return pubKeyBase64;
    }

    public Biometrics setId(Integer id) {
        this.id = id;
        return this;
    }

    public Biometrics setUser(User user) {
        this.user = user;
        return this;
    }

    public Biometrics setDevice(UserDevice device) {
        this.device = device;
        return this;
    }


    public Biometrics setPubKeyBase64(String pubKeyBase64) {
        this.pubKeyBase64 = pubKeyBase64;
        return this;
    }

    @Override
    public String toString() {
        return "Biometrics{" +
                "id=" + id +
                ", user=" + user +
                ", device=" + device +
                ", pubKeyBase64='" + pubKeyBase64 + '\'' +
                '}';
    }
}
