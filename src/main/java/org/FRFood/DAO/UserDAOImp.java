package org.FRFood.DAO;

import java.util.Optional;
import org.FRFood.entity.User;

public class UserDAOImp implements UserDAO{
    @Override
    public int insertUser(User category) {
        String temp = "INSERT TO Users (full_name , phone , email , password_hash , role , address , profile_image " ;

        return 0;
    }

    @Override
    public Optional<User> getUserById(int id){
        return Optional.empty();
    }
}
