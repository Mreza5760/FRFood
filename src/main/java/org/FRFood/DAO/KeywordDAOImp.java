package org.FRFood.DAO;

import org.FRFood.entity.Keyword;
import org.FRFood.util.DBConnector;
import org.FRFood.util.DataAlreadyExistsException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeywordDAOImp implements KeywordDAO {
    @Override
    public Optional<Keyword> getKeywordById(int id) throws SQLException {
        String temp = "SELECT id, name FROM Keywords WHERE id = ?";
        Keyword keyword = null;

        try (Connection connection = DBConnector.gConnection();
             PreparedStatement statement = connection.prepareStatement(temp)) {
            statement.setString(1, Integer.toString(id));
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    keyword = new Keyword();
                    keyword.setId(result.getInt("id"));
                    keyword.setName(result.getString("name"));
                }
            }
        }
        return Optional.ofNullable(keyword);
    }

    @Override
    public Optional<Keyword> getKeywordByName(String name) throws SQLException {
        String temp = "SELECT id, name FROM Keywords WHERE name = ?";
        Keyword keyword = null;
        try (Connection connection = DBConnector.gConnection();
             PreparedStatement statement = connection.prepareStatement(temp)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    keyword = new Keyword();
                    keyword.setName(result.getString("name"));
                    keyword.setId(result.getInt("id"));
                }
            }
        }

        return Optional.ofNullable(keyword);
    }

    @Override
    public int insertKeyword(Keyword keyword) throws SQLException, DataAlreadyExistsException {
        int generatedId = -1;
        if (getKeywordByName(keyword.getName()).isPresent()) {
            throw new DataAlreadyExistsException("a Keyword with that name is already in the db");
        } else {
            String temp = "INSERT INTO Keywords (name) VALUES (?)";
            try (Connection connection = DBConnector.gConnection();
                 PreparedStatement statement = connection.prepareStatement(temp, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, keyword.getName());
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = statement.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                        } else {
                            throw new SQLException("Creating Keyword failed, no Id obtained.");
                        }
                    }
                } else {
                    throw new SQLException("Creating Keyword failed, no rows affected.");
                }
            } catch (SQLException e) {
                if (e.getSQLState().equals("23000")) {
                    throw new DataAlreadyExistsException("FAILED.");
                } else {
                    throw e;
                }
            }
        }
        return generatedId;
    }

    @Override
    public List<Keyword> getKeywordsByFoodId(int foodId) throws SQLException {
        String temp = "SELECT * FROM FoodItem_Keywords WHERE food_item_id = ?";
        List<Keyword> keywords = new ArrayList<>();
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(temp)
                ){
            statement.setInt(1, foodId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Keyword keyword = new Keyword();
                    if(getKeywordById(result.getInt("id")).isEmpty()){
                        //error
                        return null;
                    }else{
                        keyword = getKeywordById(result.getInt("id")).get();
                    }
                    keywords.add(keyword);
                }
            }
        }
        return keywords;
    }
}