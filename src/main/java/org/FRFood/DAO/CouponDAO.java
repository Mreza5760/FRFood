package org.FRFood.DAO;

import org.FRFood.entity.Coupon;

import java.sql.SQLException;
import java.util.Optional;

public interface CouponDAO {
    Optional<Coupon> getByCode(String code) throws SQLException;
    int getUserCount(int couponId, int userId) throws SQLException;
}