package org.FRFood.DAO;

import org.FRFood.entity.User;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    Optional<User> getById(int id) throws SQLException;
    int insert(User user) throws SQLException, DataAlreadyExistsException;
    boolean deleteById(int id)throws SQLException;
    Optional<User> getByPhone(String phoneNumber) throws SQLException;
    void update(User currentUser) throws SQLException;
}
