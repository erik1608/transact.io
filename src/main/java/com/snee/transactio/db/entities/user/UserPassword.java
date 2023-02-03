package com.snee.transactio.db.entities.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_password")
public class UserPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String algorithm;
    private int iter_count;
    private String salt;
    private String hash;

    public String getAlgorithm() {
        return algorithm;
    }

    public int getIterCount() {
        return iter_count;
    }

    public String getSalt() {
        return salt;
    }

    public String getHash() {
        return hash;
    }

    public UserPassword setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public UserPassword setIterCount(int iter_count) {
        this.iter_count = iter_count;
        return this;
    }

    public UserPassword setSalt(String salt) {
        this.salt = salt;
        return this;
    }

    public UserPassword setHash(String hash) {
        this.hash = hash;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!String.class.equals(obj.getClass())) {
            return false;
        }

        return hash.equals(obj);
    }

    @Override
    public String toString() {
        return "UserPassword{" +
                "id=" + id +
                ", algorithm='" + algorithm + '\'' +
                ", iter_count=" + iter_count +
                ", salt='" + salt + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
