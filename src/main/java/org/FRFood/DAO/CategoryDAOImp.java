package org.FRFood.DAO;

import org.FRFood.entity.Category;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.DatabaseConnector;

import java.sql.*;
import java.util.Optional;


public class CategoryDAOImp implements CategoryDAO{
    @Override
    public Optional<Category> getCategoryById(int id) throws SQLException {
        String temp = "SELECT id, name FROM Categories WHERE id = ?";
        Category category = null;

        try(Connection connection = DatabaseConnector.gConnection();
            PreparedStatement statement = connection.prepareStatement(temp)){
            statement.setString(1 , Integer.toString(id));
            try(ResultSet result = statement.executeQuery()){
                if(result.next()){
                    category = new Category();
                    category.setId(result.getInt("id"));
                    category.setName(result.getString("name"));
                }
            }
        }
        return Optional.ofNullable(category);
    }

    @Override
    public Optional<Category> getCategoryByName(String name) throws SQLException{
        String temp = "SELECT id, name FROM Categories WHERE name = ?";
        Category category = null;
        try (Connection connection = DatabaseConnector.gConnection();
            PreparedStatement statement = connection.prepareStatement(temp)){
            statement.setString(1, name);
            try(ResultSet result = statement.executeQuery()){
                if(result.next()){
                    category = new Category();
                    category.setName(result.getString("name"));
                    category.setId(result.getInt("id"));
                }
            }
        }

        return Optional.ofNullable(category);
    }

    @Override
    public int insertCategory(String name) throws SQLException , DataAlreadyExistsException {
        int generatedId = -1;
        if (getCategoryByName(name).isPresent()) {
            throw new DataAlreadyExistsException("a Category with that name is already in the db");
        }else{
            String temp = "INSERT INTO Categories (name) VALUES (?)";
            try(Connection connection = DatabaseConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(temp , Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1 , name);
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = statement.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                        } else {
                            throw new SQLException("Creating category failed, no Id obtained.");
                        }
                    }
                }else{
                    throw new SQLException("Creating category failed, no rows affected.");
                }
            }catch (SQLException e){
                if(e.getSQLState().equals("23000")){
                    throw new DataAlreadyExistsException("FAILED. the category was added into the db by another thread");
                }else{
                    throw e;
                }
            }
        }
        return generatedId;
    }
}
