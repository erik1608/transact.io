package com.snee.transactio.db.entities.user;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user_friends")
public class UserRelationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private User friend;

    private String alias;

    private String status;

    public Integer getId() {
        return id;
    }

    public User getFriend() {
        return friend;
    }

    public String getAlias() {
        return alias;
    }

    public String getStatus() {
        return status;
    }

    public UserRelationMapping setId(Integer id) {
        this.id = id;
        return this;
    }

    public UserRelationMapping setFriend(User friend) {
        this.friend = friend;
        return this;
    }

    public UserRelationMapping setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public UserRelationMapping setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "Friend [" + alias + ", " + friend + "]";
    }
}
