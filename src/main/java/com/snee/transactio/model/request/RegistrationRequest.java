package com.snee.transactio.model.request;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationRequest implements RequestModel {
	private static final String PHONE_NUMBER_REGEX = "^\\+(?:[0-9] ?){6,14}[0-9]$";
	private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

	private static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	@Expose
	private String firstname;

	@Expose
	private String lastname;

	@Expose
	private String dateOfBirth;

	@Expose
	private String userName;

	@Expose
	private String phoneNumber;

	@Expose
	private String email;

	@Expose
	private String password;

	@Expose
	private String passwordConfirmation;

	@Expose
	private Device deviceInfo;

	public void validate() {
		if (firstname == null || firstname.isEmpty()) {
			throw new RequestValidationException("The first name is required");
		}

		if (lastname == null || lastname.isEmpty()) {
			throw new RequestValidationException("The last name is required");
		}

		if (userName == null || userName.isEmpty()) {
			throw new RequestValidationException("The username is required");
		}

		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw new RequestValidationException("The phone number is required");
		} else if (!isPhoneNumberValid(phoneNumber)) {
			throw new RequestValidationException("The phone number is invalidly formatted");
		}

		if (email == null || email.isEmpty()) {
			throw new RequestValidationException("The email is required");
		} else if (!isEmailValid(email))  {
			throw new RequestValidationException("The email is invalidly formatted");
		}

		if (dateOfBirth == null || dateOfBirth.isEmpty()) {
			throw new RequestValidationException("The date of birth is required");
		} else {
			// The age of the user must be 21+
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDateTime now = LocalDateTime.now();
			dtf.format(now);

			int year = Integer.parseInt(dateOfBirth.split("-")[0]);
			int currentYear = Integer.parseInt(dtf.format(now).split("-")[0]);
			if ((currentYear - year) < 21) {
				throw new RequestValidationException("The legal registration age should be 21+");
			}
		}

		if (password == null || password.isEmpty()) {
			throw new RequestValidationException("The password is required");
		}

		if (passwordConfirmation == null || passwordConfirmation.isEmpty()) {
			throw new RequestValidationException("The password confirmation is required");
		}

		if (!password.equals(passwordConfirmation)) {
			throw new RequestValidationException("Password confirmation mismatch!");
		}

		if (deviceInfo != null) {
			deviceInfo.validate();
		} else {
			throw new RequestValidationException("Device information is missing confirmation mismatch!");
		}
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public String getUserName() {
		return userName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public Device getDeviceInfo() {
		return deviceInfo;
	}

	private boolean isPhoneNumberValid(String phoneNumber) {
		Matcher phoneNumberMatcher = PHONE_NUMBER_PATTERN.matcher(phoneNumber);
		return phoneNumberMatcher.matches();
	}

	private boolean isEmailValid(String email) {
		Matcher phoneNumberMatcher = EMAIL_PATTERN.matcher(email);
		return phoneNumberMatcher.matches();
	}
}
