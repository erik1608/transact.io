package com.snee.transactio.db.entities.user;

import javax.persistence.*;

@Entity
@Table(name = "user_account")
public class UserAccount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private boolean isPrimary;

	@Column(nullable = false)
	private String name;

	@Column(unique = true)
	private String number;

	@Column(nullable = false)
	private Integer balance;

	@Column(nullable = false)
	private Integer userId;

	public Integer getId() {
		return id;
	}

	public Integer getUserId() {
		return userId;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public String getNumber() {
		return number;
	}

	public Integer getBalance() {
		return balance;
	}

	public String getName() {
		return name;
	}

	public UserAccount setPrimary(boolean primary) {
		isPrimary = primary;
		return this;
	}

	public UserAccount setNumber(String number) {
		this.number = number;
		return this;
	}

	public UserAccount setBalance(Integer balance) {
		this.balance = balance;
		return this;
	}

	public UserAccount setName(String name) {
		this.name = name;
		return this;
	}

	public UserAccount setUserId(Integer userId) {
		this.userId = userId;
		return this;
	}

	@Override
	public String toString() {
		return "UserAccount{" +
				"id=" + id +
				", isPrimary=" + isPrimary +
				", name='" + name + '\'' +
				", number='" + number + '\'' +
				", balance=" + balance +
				", userId=" + userId +
				'}';
	}
}
