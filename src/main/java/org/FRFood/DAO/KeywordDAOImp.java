package org.FRFood.DAO;

import org.FRFood.entity.Keyword;
import org.FRFood.util.DBConnector;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class KeywordDAOImp implements KeywordDAO {
    @Override
    public List<Keyword> getKeywordsByFoodId(int foodId) throws SQLException {
        String temp = "SELECT * FROM keywords WHERE food_id = ?";
        List<Keyword> keywords = new ArrayList<>();

        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement statement = connection.prepareStatement(temp)
                ){
            statement.setInt(1, foodId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Keyword keyword = new Keyword();
                    keyword.setName(result.getString("name"));
                    keywords.add(keyword);
                }
            }
        }
        return keywords;
    }
}