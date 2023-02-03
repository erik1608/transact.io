package com.snee.transactio.db.entities.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GenerationType;

@Entity
@Table(name = "user_device")
public class UserDevice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String deviceId;
	private String model;
	private String platform;
	private String manufacturer;
	private String version;
	private String pushRegistrationId;

	public String getModel() {
		return model;
	}

	public String getPushRegistrationId() {
		return pushRegistrationId;
	}

	public Integer getId() {
		return id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getPlatform() {
		return platform;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getVersion() {
		return version;
	}

	public UserDevice setModel(String model) {
		this.model = model;
		return this;
	}

	public UserDevice setPushRegistrationId(String pushRegistrationId) {
		this.pushRegistrationId = pushRegistrationId;
		return this;
	}

	public UserDevice setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}

	public UserDevice setPlatform(String platform) {
		this.platform = platform;
		return this;
	}

	public UserDevice setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
		return this;
	}

	public UserDevice setVersion(String version) {
		this.version = version;
		return this;
	}

	@Override
	public String toString() {
		return "UserDevice{" +
				"id=" + id +
				", deviceId='" + deviceId + '\'' +
				", model='" + model + '\'' +
				", platform='" + platform + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", version='" + version + '\'' +
				", pushRegistrationId='" + pushRegistrationId + '\'' +
				'}';
	}
}
