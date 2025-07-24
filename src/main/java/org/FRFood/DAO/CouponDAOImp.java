package org.FRFood.DAO;

import org.FRFood.entity.Coupon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CouponDAOImp implements CouponDAO {
    @Override
    public int insert(Coupon coupon) throws SQLException {
        return 0;
    }

    @Override
    public Optional<Coupon> getById(int id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public Optional<Coupon> getByCode(String code) throws SQLException {
        return Optional.empty();
    }

    @Override
    public int getUserCount(int couponId, int userId) throws SQLException {
        return 0;
    }

    @Override
    public List<Coupon> getAllCoupons() throws SQLException {
        return List.of();
    }

    @Override
    public void delete(int id) throws SQLException {

    }
}