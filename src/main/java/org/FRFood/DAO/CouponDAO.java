package org.FRFood.DAO;

import org.FRFood.entity.Coupon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CouponDAO {
    int insert(Coupon coupon) throws SQLException;
    Optional<Coupon> getById(int id) throws SQLException;
    Optional<Coupon> getByCode(String code) throws SQLException;
    int getUserCount(int couponId, int userId) throws SQLException;
    List<Coupon> getAllCoupons() throws SQLException;
    void delete(int id) throws SQLException;
    void update(int id, Coupon coupon) throws SQLException;
}