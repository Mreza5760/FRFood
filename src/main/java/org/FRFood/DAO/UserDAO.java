package org.FRFood.DAO;

import org.FRFood.entity.User;
import org.FRFood.util.DataAlreadyExistsException;

import java.util.Optional;
import java.sql.SQLException;

public interface UserDAO {
    boolean deleteById(int id)throws SQLException;
    Optional<User> getById(int id) throws SQLException;
    public boolean update(User currentUser) throws SQLException;
    Optional<User> getByPhone(String phoneNumber) throws SQLException;
    int insert(User category) throws SQLException, DataAlreadyExistsException;
}