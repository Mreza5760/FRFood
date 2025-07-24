package org.FRFood.DAO;

import org.FRFood.entity.Coupon;

import java.sql.SQLException;
import java.util.Optional;

public class CouponDAOImp implements CouponDAO {
    @Override
    public Optional<Coupon> getByCode(String code) throws SQLException {
        return Optional.empty();
    }

    @Override
    public int getUserCount(int couponId, int userId) throws SQLException {
        return 0;
    }
}