package org.FRFood.DAO;

import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.util.DBConnector;
import org.FRFood.util.DataAlreadyExistsException;
import org.FRFood.util.Role;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class RestaurantDAOImp implements RestaurantDAO {
    @Override
    public int insert (Restaurant restaurant,int userId)throws SQLException, DataAlreadyExistsException {
        String sql = "INSERT INTO restaurants (owner_id , name , address , phone , logo , tax_fee , additional_fee) VALUES (?,?,?,?,?,?,?)";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
                )
        {
            preparedStatement.setInt(1,userId );
            preparedStatement.setString(2, restaurant.getName());
            preparedStatement.setString(3, restaurant.getAddress());
            preparedStatement.setString(4, restaurant.getPhone());
            preparedStatement.setString(5, restaurant.getLogo());
            preparedStatement.setInt(6, restaurant.getTaxFee());
            preparedStatement.setInt(7, restaurant.getAdditionalFee());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID generated.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    @Override
    public Optional<Restaurant> getById(int id) throws SQLException{
        String sql =  "SELECT * FROM restaurants WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                )
        {
            preparedStatement.setInt(1,id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("id"));
                    restaurant.setName(rs.getString("name"));
                    restaurant.setAddress(rs.getString("address"));
                    restaurant.setPhone(rs.getString("phone"));
                    restaurant.setLogo(rs.getString("logo"));
                    restaurant.setTaxFee(rs.getInt("tax_fee"));
                    restaurant.setAdditionalFee(rs.getInt("additional_fee"));
                    return Optional.of(restaurant);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> searchByString(String search) throws SQLException {
        return List.of();
    }

    @Override
    public void DeleteById(int id) throws SQLException{
        String sql = "DELETE FROM restaurants WHERE id = ?";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ){
            preparedStatement.setInt(1,id);
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }
        }
    }

    @Override
    public void UpdateById(Restaurant restaurant) throws SQLException {
        String sql = "INSERT INTO restaurants (id,owner_id , name , address , phone , logo , tax_fee , additional_fee) VALUES (?,?,?,?,?,?,?,?)";
        try(
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        )
        {
            preparedStatement.setInt(1,restaurant.getId());
            preparedStatement.setInt(2,restaurant.getOwner().getId() );
            preparedStatement.setString(3, restaurant.getName());
            preparedStatement.setString(4, restaurant.getAddress());
            preparedStatement.setString(5, restaurant.getPhone());
            preparedStatement.setString(6, restaurant.getLogo());
            preparedStatement.setInt(7, restaurant.getTaxFee());
            preparedStatement.setInt(8, restaurant.getAdditionalFee());

            DeleteById(restaurant.getId());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
        }
    }
}