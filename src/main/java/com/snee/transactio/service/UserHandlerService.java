package com.snee.transactio.service;

import com.snee.transactio.config.PasswordHashConfig;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.db.entities.user.UserDevice;
import com.snee.transactio.db.entities.user.UserPassword;
import com.snee.transactio.db.entities.user.UserRelationMapping;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.db.repo.UserFriendsRepo;
import com.snee.transactio.db.repo.UsersAccountRepo;
import com.snee.transactio.db.repo.UsersDeviceRepo;
import com.snee.transactio.db.repo.UsersPasswordRepo;
import com.snee.transactio.db.repo.UsersRepo;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.Device;
import com.snee.transactio.model.request.RegistrationRequest;
import com.snee.transactio.model.request.UserFriendRequest;
import com.snee.transactio.model.response.RegistrationResponse;
import com.snee.transactio.model.response.UserFriendsResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class UserHandlerService {

	public final static ExampleMatcher USERNAME_MATCHER = ExampleMatcher.matchingAny().withIgnorePaths("id").withMatcher("username", ignoreCase());

	public final static ExampleMatcher EMAIL_MATCHER = ExampleMatcher.matchingAny().withIgnorePaths("id", "username").withMatcher("email", ignoreCase());

	private final static ExampleMatcher ACCOUNT_NUMBER_MATCHER = ExampleMatcher.matchingAny().withIgnorePaths("id").withMatcher("number", ignoreCase());

	private final static ExampleMatcher DEVICE_MATCHER = ExampleMatcher.matchingAny().withIgnorePaths("id").withMatcher("deviceId", ignoreCase());

	private final UsersRepo mUsersRepo;
	private final UsersPasswordRepo mUserPwdRepo;
	private final UsersDeviceRepo mUserDeviceRepo;
	private final UsersAccountRepo mUserAccountsRepo;
	private final UserFriendsRepo mUserFriendsRepo;

	private final PushNotificationService mPushService;

	private final PasswordHashConfig mPwdHashConfig;

	@Value("${account.prefix.number}")
	private String DEFAULT_ACCOUNT_PREFIX;

	public UserHandlerService(Repos repos, PasswordHashConfig hashConfig, PushNotificationService pushService) {
		mPushService = pushService;
		mPwdHashConfig = hashConfig;
		mUsersRepo = repos.get(UsersRepo.class);
		mUserPwdRepo = repos.get(UsersPasswordRepo.class);
		mUserDeviceRepo = repos.get(UsersDeviceRepo.class);
		mUserAccountsRepo = repos.get(UsersAccountRepo.class);
		mUserFriendsRepo = repos.get(UserFriendsRepo.class);
	}


	/**
	 * Creates a user with the provided {@link RegistrationRequest}.
	 *
	 * @param request  The registration request.
	 * @param response The registration response.
	 */
	@Transactional
	public User registerUser(RegistrationRequest request, RegistrationResponse response) {
		User user = new User();
		user.setUsername(request.getUserName());
		Example<User> userNameExample = Example.of(user, USERNAME_MATCHER);
		if (mUsersRepo.exists(userNameExample)) {
			throw new RequestValidationException("User with the username already exists");
		}

		user.setEmail(request.getEmail());
		Example<User> emailExample = Example.of(user, EMAIL_MATCHER);
		if (mUsersRepo.exists(emailExample)) {
			throw new RequestValidationException("User with the email already exists");
		}

		user.setFirstname(request.getFirstname()).setLastname(request.getLastname()).setPhoneNumber(request.getPhoneNumber()).setDob(request.getDateOfBirth());
		UserPassword userPassword = new UserPassword();

		byte[] salt = new byte[Integer.parseInt(mPwdHashConfig.getSaltLength())];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(salt);
		userPassword.setAlgorithm(mPwdHashConfig.getAlgo()).setSalt(Base64.getEncoder().encodeToString(salt)).setIterCount(Integer.parseInt(mPwdHashConfig.getIterCount())).setHash(hashPassword(userPassword, request.getPassword()));

		mUserPwdRepo.save(userPassword);

		user.setPassword(userPassword);
		UserDevice deviceExample = new UserDevice();
		deviceExample.setDeviceId(request.getDeviceInfo().getDeviceId());
		Optional<UserDevice> device = mUserDeviceRepo.findOne(Example.of(deviceExample, DEVICE_MATCHER));
		if (device.isPresent()) {
			// If the device with the ID is already registered get it and add to the user devices.
			user.setUserDeviceList(new ArrayList<>(Collections.singleton(device.get())));
		} else {
			UserDevice newDevice = createUserDevice(request.getDeviceInfo());
			mUserDeviceRepo.save(newDevice);
			user.setUserDeviceList(new ArrayList<>(Collections.singleton(newDevice)));
		}

		UserAccount userAccount = new UserAccount().setName("Checking account").setPrimary(true).setBalance(10000).setNumber(generateAccountRecord());

		List<UserAccount> userAccounts = new ArrayList<>();
		userAccounts.add(userAccount);
		user.setUserAccounts(userAccounts);

		mUsersRepo.save(user);

		userAccount.setUserId(user.getId());
		mUserAccountsRepo.save(userAccount);

		response.setStatus(HttpStatus.CREATED).setMessage("User registered successfully");
		return user;
	}

	/**
	 * Checks whether the provided password is matching to the user's password.
	 *
	 * @param user     The user whom password to check against to.
	 * @param password The password to check.
	 * @return true if matching, otherwise false.
	 */
	public boolean isPasswordCorrect(User user, String password) {
		//noinspection EqualsBetweenInconvertibleTypes
		return user.getPassword().equals(hashPassword(user.getPassword(), password));
	}


	/**
	 * Get the user information, based on the userName and email search criteria.
	 *
	 * @param userId The username or email.
	 * @return {@link User} the object of the user information.
	 */
	public User getUser(String userId) {
		return mUsersRepo.findByUsername(userId).orElseGet(() -> mUsersRepo.findByEmail(userId).orElse(null));
	}

	/**
	 * Get the user information, based on the userName and email search criteria.
	 *
	 * @param userAccount The example of user account to find the user with.
	 * @return {@link User} the object of the user information.
	 */
	public User getUserByAccount(UserAccount userAccount) {
		Example<UserAccount> userAccountExample = Example.of(userAccount, ACCOUNT_NUMBER_MATCHER);
		Optional<UserAccount> optionalUserAccount = mUserAccountsRepo.findOne(userAccountExample);
		//noinspection OptionalGetWithoutIsPresent
		return optionalUserAccount.map(account -> mUsersRepo.findById(account.getUserId()).get()).orElse(null);
	}

	public UserFriendsResponse sendFriendRequest(Session cUserSession, UserFriendRequest request) {
		if (cUserSession.getSubject().equals(request.getUserName())) {
			throw new RequestValidationException("You cannot add yourself");
		}

		User currentUser = getUserInfo(cUserSession.getSubject());
		User userToRequest = getUserInfo(request.getUserName());

		List<UserRelationMapping> userToRequestFriendsList = userToRequest.getFriends();
		for (UserRelationMapping mapping : userToRequestFriendsList) {
			User friend = mapping.getFriend();
			if (friend.getUsername().equals(currentUser.getUsername())) {
				if (UserFriendRequest.REQUEST_STATUS_PENDING.equals(mapping.getStatus())) {
					throw new RequestValidationException("A friend request is already sent to the user");
				}

				throw new RequestValidationException("You are already friends");
			}
		}

		UserRelationMapping friendRequest = new UserRelationMapping();
		{
			mUserFriendsRepo.save(friendRequest.setAlias(currentUser.getFirstname()).setFriend(currentUser).setStatus(UserFriendRequest.REQUEST_STATUS_PENDING));
		}

		String detailsText = currentUser.getUsername() + " (" + currentUser.getFirstname() + " " + currentUser.getLastname() + ") " + " has sent you a friend request.";

		List<UserDevice> requestedUserDevices = userToRequest.getUserDevices();
		HashMap<String, String> details = new HashMap<>();
		details.put("view", "friends");
		for (UserDevice device : requestedUserDevices) {
			mPushService.sendNotification("New friend request", detailsText, details, device);
		}

		List<UserRelationMapping> userRelationMappingList = userToRequest.getFriends();
		userRelationMappingList.add(friendRequest);
		mUsersRepo.save(userToRequest);
		UserFriendsResponse response = new UserFriendsResponse();
		response.setStatus("SUCCESS");
		return response;
	}

	public UserFriendsResponse getFriendsList(Session cUserSession, UserFriendRequest request) {
		UserFriendsResponse response = new UserFriendsResponse();
		User currentUser = getUserInfo(cUserSession.getSubject());
		List<UserFriendsResponse.Friend> friends = new ArrayList<>();
		response.setStatus("SUCCESS");
		List<UserRelationMapping> userRelationMappingList = currentUser.getFriends();
		for (UserRelationMapping userRelationMapping : userRelationMappingList) {
			if (request.getStatus().equals(userRelationMapping.getStatus())) {
				UserFriendsResponse.Friend friend = new UserFriendsResponse.Friend();
				friend.setAlias(userRelationMapping.getAlias());
				friend.setUsername(userRelationMapping.getFriend().getUsername());
				friends.add(friend);
			}
		}
		response.setFriends(friends);
		return response;
	}

	public UserFriendsResponse updateUsersFriend(Session cUserSession, UserFriendRequest request) {
		UserFriendsResponse response = new UserFriendsResponse();
		User currentUser = getUserInfo(cUserSession.getSubject());
		List<UserRelationMapping> friends = currentUser.getFriends();
		UserRelationMapping foundFriendRequest = null;
		for (UserRelationMapping friend : friends) {
			if (friend.getFriend().getUsername().equals(request.getUserName())) {
				foundFriendRequest = friend;
			}
		}

		if (foundFriendRequest == null) {
			throw new RequestValidationException("The user was not found");
		}

		String action = request.getAction();
		if ("updateStatus".equals(action)) {
			if (!UserFriendRequest.REQUEST_STATUS_PENDING.equals(foundFriendRequest.getStatus())) {
				throw new RequestValidationException("The request is already " + foundFriendRequest.getStatus());
			}

			if (UserFriendRequest.REQUEST_STATUS_ACCEPTED.equals(request.getStatus())) {
				foundFriendRequest.setStatus(UserFriendRequest.REQUEST_STATUS_ACCEPTED);
				mUserFriendsRepo.save(foundFriendRequest);

				// Add the friend to the current user too.
				UserRelationMapping mapping = new UserRelationMapping();
				{
					mapping.setStatus(UserFriendRequest.REQUEST_STATUS_ACCEPTED);
					mapping.setAlias(currentUser.getFirstname());
					mapping.setFriend(currentUser);
					mUserFriendsRepo.save(mapping);
				}

				foundFriendRequest.getFriend().getFriends().add(mapping);
				mUsersRepo.save(foundFriendRequest.getFriend());
			} else if (UserFriendRequest.REQUEST_STATUS_DECLINED.equals(request.getStatus())) {
				mUserFriendsRepo.deleteById(foundFriendRequest.getId());
			}
			response.setStatus("SUCCESS");
		}
		return response;
	}

	public UserFriendsResponse deleteUsersFriend(Session cUserSession, UserFriendRequest request) {
		Assert.notNull(cUserSession, "The session cannot be null.");
		Assert.notNull(request, "The request cannot be null.");

		// TODO implement.
		return new UserFriendsResponse();
	}

	private User getUserInfo(String username) {
		User userInfo = getUser(username);
		if (userInfo == null) {
			throw new RequestValidationException("Requested user does not exist");
		}
		return userInfo;
	}

	/**
	 * Hashes the provided password using the options described in the {@link UserPassword}.
	 *
	 * @param options  the hash options.
	 * @param password the password to hash.
	 * @return {@link Base64} encoded password hash.
	 */
	private String hashPassword(UserPassword options, String password) {
		try {
			byte[] salt = Base64.getDecoder().decode(options.getSalt());
			SecretKeyFactory skf = SecretKeyFactory.getInstance(options.getAlgorithm());
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, options.getIterCount(), Integer.parseInt(mPwdHashConfig.getLength()));
			SecretKey key = skf.generateSecret(spec);
			return Base64.getEncoder().encodeToString(key.getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ignored) {
			return null;
		}
	}

	private String generateAccountRecord() {
		// Since this is the users first generated account, next is the ordinal # of it.
		return DEFAULT_ACCOUNT_PREFIX + new Date().getTime() / 1000 + "0001";
	}

	@Transactional
	public UserDevice updateUserDeviceIfNeededAndGet(User user, Device device) {
		List<UserDevice> userDevices = user.getUserDevices();
		if (userDevices == null) {
			return null;
		}

		for (UserDevice savedDevice : userDevices) {
			if (savedDevice.getDeviceId().equals(device.getDeviceId())) {
				if (!savedDevice.getPushRegistrationId().equals(device.getPush().getRegistrationId())) {
					savedDevice.setPushRegistrationId(device.getPush().getRegistrationId());
					mUserDeviceRepo.save(savedDevice);
				}

				return savedDevice;
			}
		}

		// Otherwise save the new device and return.
		UserDevice newDevice = createUserDevice(device);
		mUserDeviceRepo.save(newDevice);
		for (UserDevice userDevice : userDevices) {
			mPushService.sendNotification("Account interaction", "New login-in from " + newDevice.getManufacturer() + " " + newDevice.getModel() + " to account " + user.getUsername(), new HashMap<>(), userDevice);
		}
		userDevices.add(newDevice);
		mUsersRepo.save(user);
		return newDevice;
	}

	private UserDevice createUserDevice(Device device) {
		return new UserDevice().setDeviceId(device.getDeviceId()).setModel(device.getModel()).setManufacturer(device.getManufacturer()).setVersion(device.getVersion()).setPlatform(device.getPlatform()).setPushRegistrationId(device.getPush().getRegistrationId());
	}
}
