package com.snee.transactio.db.entities.user;

import org.apache.log4j.Logger;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
	private static final Logger LOG = Logger.getLogger(User.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String firstname;

	@Column(nullable = false)
	private String lastname;

	@Column(nullable = false)
	private String dob;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String phoneNumber;

	@OneToOne
	private UserPassword password;

	@ManyToMany
	@Column(nullable = false)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<UserDevice> userDevices;

	@OneToMany
	@Column(nullable = false)
	@LazyCollection(LazyCollectionOption.FALSE)
	@PrimaryKeyJoinColumn
	private List<UserAccount> userAccounts;

	@OneToMany
	@Column(nullable = false)
	@LazyCollection(LazyCollectionOption.FALSE)
	@PrimaryKeyJoinColumn
	private List<UserRelationMapping> friends;

	public Integer getId() {
		return id;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getDob() {
		return dob;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public UserPassword getPassword() {
		return password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public List<UserDevice> getUserDevices() {
		return userDevices;
	}

	public List<UserAccount> getUserAccounts() {
		return userAccounts;
	}

	public List<UserRelationMapping> getFriends() {
		return friends;
	}

	public User setFirstname(String firstname) {
		this.firstname = firstname;
		return this;
	}

	public User setLastname(String lastname) {
		this.lastname = lastname;
		return this;
	}

	public User setDob(String dob) {
		this.dob = dob;
		return this;
	}

	public User setUsername(String username) {
		this.username = username;
		return this;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

	public User setPassword(UserPassword password) {
		this.password = password;
		return this;
	}

	public User setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public User setUserDeviceList(List<UserDevice> userDevices) {
		this.userDevices = userDevices;
		return this;
	}

	public User setUserAccounts(List<UserAccount> userAccounts) {
		this.userAccounts = userAccounts;
		return this;
	}

	public User setFriends(List<UserRelationMapping> easyAccessList) {
		this.friends = easyAccessList;
		return this;
	}

	public void copyFrom(User user) {
		this.id = user.id;
		this.firstname = user.firstname;
		this.lastname = user.lastname;
		this.username = user.username;
		this.email = user.email;
		this.userAccounts = user.userAccounts;
		this.dob = user.dob;
		this.password = user.password;
		this.phoneNumber = user.phoneNumber;
		this.userDevices = user.userDevices;
	}

	public UserAccount getAccountByName(String accountName) {
		for (UserAccount account : userAccounts) {
			if (account.getName().equalsIgnoreCase(accountName)) {
				return account;
			}
		}
		return null;
	}

	public UserAccount getPrimaryAccount() {
		for (UserAccount account : userAccounts) {
			if (account.isPrimary()) {
				return account;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", firstname='" + firstname + '\'' +
				", lastname='" + lastname + '\'' +
				", dob='" + dob + '\'' +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", password=" + password +
				", userDevices=" + userDevices +
				", userAccounts=" + userAccounts +
				'}';
	}
}
