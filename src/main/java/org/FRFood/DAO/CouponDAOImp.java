package org.FRFood.DAO;

import org.FRFood.entity.Coupon;
import org.FRFood.util.DBConnector;
import org.FRFood.entity.CouponType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponDAOImp implements CouponDAO {
    @Override
    public int insert(Coupon coupon) throws SQLException {
        String temp = "INSERT INTO Coupons (coupon_code,`type`,`value`,min_price,user_count,start_date,end_date) VALUES (?,?,?,?,?,?,?)";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(temp, Statement.RETURN_GENERATED_KEYS);
        ) {
            preparedStatement.setString(1, coupon.getCouponCode());
            preparedStatement.setString(2, coupon.getType().toString());
            preparedStatement.setInt(3, coupon.getValue());
            preparedStatement.setInt(4, coupon.getMinPrice());
            preparedStatement.setInt(5, coupon.getUserCount());
            preparedStatement.setString(6, coupon.getStartDate());
            preparedStatement.setString(7, coupon.getEndDate());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    coupon.setId(generatedKeys.getInt(1));
                    return coupon.getId();
                }
            }
        }
        return 0;
    }

    @Override
    public Optional<Coupon> getById(int id) throws SQLException {
        String temp = "SELECT * FROM Coupons WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(temp);
        ) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Coupon coupon = new Coupon();
                    coupon.setId(id);
                    coupon.setCouponCode(resultSet.getString("coupon_code"));
                    coupon.setType(CouponType.valueOf(resultSet.getString("type")));
                    coupon.setValue(resultSet.getInt("value"));
                    coupon.setMinPrice(resultSet.getInt("min_price"));
                    coupon.setUserCount(resultSet.getInt("user_count"));
                    coupon.setStartDate(resultSet.getString("start_date"));
                    coupon.setEndDate(resultSet.getString("end_date"));

                    return Optional.of(coupon);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Coupon> getByCode(String code) throws SQLException {
        Coupon coupon = null;
        String sql = "SELECT * FROM Coupons WHERE coupon_code = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    coupon = getById(resultSet.getInt("id")).orElse(null);
                }
            }
        }
        return Optional.ofNullable(coupon);
    }

    @Override
    public int getUserCount(int couponId, int userId) throws SQLException {
        String temp = "SELECT * FROM Coupon_User WHERE coupon_id = ? AND user_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(temp);
        ) {
            preparedStatement.setInt(1, couponId);
            preparedStatement.setInt(2, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("used_time");
                }
            }
        }
        return 0;
    }

    @Override
    public List<Coupon> getAllCoupons() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM Coupons";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Coupon coupon = getById(resultSet.getInt("id")).orElse(null);
                    coupons.add(coupon);
                }
            }
        }
        return coupons;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Coupons WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void update(int id, Coupon coupon) throws SQLException {
        String sql = "UPDATE Coupons SET coupon_code=? , type=? , value=? , min_price=? , user_count=? , start_date=? ,end_date=?  WHERE id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, coupon.getCouponCode());
            preparedStatement.setString(2, coupon.getType().toString());
            preparedStatement.setInt(3, coupon.getValue());
            preparedStatement.setInt(4, coupon.getMinPrice());
            preparedStatement.setInt(5, coupon.getUserCount());
            preparedStatement.setString(6, coupon.getStartDate());
            preparedStatement.setString(7, coupon.getEndDate());
            preparedStatement.setInt(8, id);

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("update coupon failed, no rows affected");
            }
        }
    }

    @Override
    public void useCoupon(int couponId, int userId) throws SQLException {
        String sql = "UPDATE Coupon_User SET used_time=used_time+1 WHERE coupon_id = ? AND user_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, couponId);
            preparedStatement.setInt(2, userId);

            if(preparedStatement.executeUpdate() == 0){
                String sql2 = "INSERT INTO Coupon_User (coupon_id, user_id , used_time) VALUES (?, ? ,?)";
                try(
                        Connection connection2 = DBConnector.gConnection();
                        PreparedStatement preparedStatement2 = connection2.prepareStatement(sql2);
                        ){
                    preparedStatement2.setInt(1, couponId);
                    preparedStatement2.setInt(2, userId);
                    preparedStatement2.setInt(3, 1);

                    preparedStatement2.executeUpdate();
                }
            }


        }
    }
}