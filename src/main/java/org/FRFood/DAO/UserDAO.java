package org.FRFood.DAO;

import org.FRFood.entity.User;

import java.util.Optional;

public interface UserDAO {
    Optional<User> getUserById(int id);
    int insertUser(User category);
}
