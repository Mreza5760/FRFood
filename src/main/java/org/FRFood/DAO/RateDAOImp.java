package org.FRFood.DAO;

import org.FRFood.entity.Food;
import org.FRFood.entity.Rate;
import org.FRFood.util.DBConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RateDAOImp implements RateDAO {
    @Override
    public int insert(Rate rate) throws SQLException {
        int id = -1;
        String sql = "INSERT INTO ratings (order_id , user_id , rating , comment) VALUES (?,?,?,?)";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ){
                preparedStatement.setInt(1, rate.getOrderId());
                preparedStatement.setInt(2, rate.getUserId());
                preparedStatement.setInt(3, rate.getRating());
                preparedStatement.setString(4, rate.getComment());
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Inserting rate failed, no rows affected.");
                }
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rate.setId(generatedKeys.getInt(1));
                        id = rate.getId();
                    }
                }
            String sql2 = "INSERT INTO rating_images (rating_id,image_data) VALUES (?,?)";
            for(String imageData : rate.getImages()){
                try(
                        Connection connection2 = DBConnector.gConnection();
                        PreparedStatement stmt = connection2.prepareStatement(sql2);
                ){
                    stmt.setInt(1, id);
                    stmt.setString(2,imageData);
                    stmt.executeUpdate();
                }
            }
        }
        return id;
    }

    @Override
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1,id);
            int rows = preparedStatement.executeUpdate();
            if(rows == 0){
                throw new SQLException("no rows changed !");
            }
        }
    }

    @Override
    public void updateById(int id, Rate rate) throws SQLException {
        String sql = "UPDATE ratings SET  rating = ?, comment = ? WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1, rate.getRating());
            preparedStatement.setString(2, rate.getComment());
            preparedStatement.setInt(3, id);
            int rows = preparedStatement.executeUpdate();
            if(rows == 0){
                throw new SQLException("no rows changed !");
            }
        }

        String deleteSql = "DELETE FROM rating_images WHERE rating_id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement stmt = connection.prepareStatement(deleteSql);
        ){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        String sql2 = "INSERT INTO rating_images (rating_id,image_data) VALUES (?,?)";
        for(String imageData : rate.getImages()){
            try(
                    Connection connection = DBConnector.gConnection();
                    PreparedStatement stmt = connection.prepareStatement(sql2);
            ){
                stmt.setInt(1, id);
                stmt.setString(2,imageData);
                int rows = stmt.executeUpdate();
                if(rows == 0){
                    throw new SQLException("no rows changed !");
                }
            }
        }
    }

    @Override
    public Optional<Rate> getById(int id) throws SQLException {
        Rate rate = null;
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1,id);
            List<String> images = new ArrayList<>();
            String sql2 = "SELECT * FROM rating_images WHERE rating_id = ?";
            try(
                    Connection connection2 = DBConnector.gConnection();
                    PreparedStatement preparedStatement2 = connection2.prepareStatement(sql2);
            ){
                preparedStatement2.setInt(1,id);
                try(ResultSet resultSet = preparedStatement2.executeQuery()){
                    while(resultSet.next()){
                        images.add(resultSet.getString("image_data"));
                    }
                }
            }
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    rate = new Rate();
                    rate.setId(id);
                    rate.setImages(images);
                    rate.setRating(resultSet.getInt("rating"));
                    rate.setComment(resultSet.getString("comment"));
                    rate.setOrderId(resultSet.getInt("order_id"));
                    rate.setUserId(resultSet.getInt("user_id"));
                    rate.setCreatedAt(resultSet.getString("created_at"));
                }
            }
        }
        return Optional.ofNullable(rate);
    }

    @Override
    public List<Rate> getAllRates() throws SQLException {
        List<Rate> rates = new ArrayList<>();
        List<Integer> rateIds = new ArrayList<>();
        String sql = "SELECT * FROM ratings";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    rateIds.add(resultSet.getInt("id"));
                }
            }
        }
        for(int id : rateIds){
            rates.add(getById(id).orElse(null));
        }
        return rates;
    }

    @Override
    public List<Rate> getUserRateOnOrder(int userId, int orderId) throws SQLException {
        List<Rate> rates = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE user_id = ? AND order_id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ){
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, orderId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    rates.add(getById(resultSet.getInt("id")).orElse(null));

                }
            }
        }
        return rates;
    }
}