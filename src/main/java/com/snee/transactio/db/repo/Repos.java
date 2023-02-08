package com.snee.transactio.db.repo;

import org.springframework.stereotype.Service;

/**
 * A service that holds all the repositories and provides an instance of the desired one.
 */
@Service
public final class Repos {
    private final UsersRepo mUsersRepo;

    private final UsersPasswordRepo mUserPwdRepo;

    private final UsersAccountRepo mUserAccountsRepo;

    private final UsersDeviceRepo mUserDeviceRepo;

    private final TransactionRepo mTransactionRepo;

    private final BiometricsRepo mBiometricsRepo;

    private final UserFriendsRepo mUserFriendsRepo;

    public Repos(UsersRepo usersRepo,
                 UsersPasswordRepo usersPwdRepo,
                 UsersAccountRepo usersAccountRepo,
                 UsersDeviceRepo usersDeviceRepo,
                 TransactionRepo transactionRepo,
                 BiometricsRepo biometricsRepo,
                 UserFriendsRepo userFriendsRepo) {

        mUsersRepo = usersRepo;
        mUserPwdRepo = usersPwdRepo;
        mUserAccountsRepo = usersAccountRepo;
        mUserDeviceRepo = usersDeviceRepo;
        mTransactionRepo = transactionRepo;
        mBiometricsRepo = biometricsRepo;
        mUserFriendsRepo = userFriendsRepo;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> cls) {
        if (UsersRepo.class.equals(cls)) {
            return (T) mUsersRepo;
        } else if (UsersPasswordRepo.class.equals(cls)) {
            return (T) mUserPwdRepo;
        } else if (UsersAccountRepo.class.equals(cls)) {
            return (T) mUserAccountsRepo;
        } else if (UsersDeviceRepo.class.equals(cls)) {
            return (T) mUserDeviceRepo;
        } else if (TransactionRepo.class.equals(cls)) {
            return (T) mTransactionRepo;
        } else if (BiometricsRepo.class.equals(cls)) {
            return (T) mBiometricsRepo;
        } else if (UserFriendsRepo.class.equals(cls)) {
            return (T) mUserFriendsRepo;
        } else {
            throw new IllegalArgumentException(
                    "The provided entity does not have a mapped repository."
            );
        }
    }
}
