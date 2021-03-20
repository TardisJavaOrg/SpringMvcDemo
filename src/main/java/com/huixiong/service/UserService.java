package com.huixiong.service;

import com.huixiong.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class UserService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void init() {
        System.out.println("init success");
    }

    public User login(String email, String password) {
        return (User) jdbcTemplate.execute("select * from user where email = ? and password = ?", new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setObject(1, email);
                ps.setObject(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new User(rs.getString("email"), rs.getString("name"),null);
                }
                throw new RuntimeException("email or passowrd not invalid");
            }
        });
    }

    public List<User> getUsers() {
        return null;
    }

    public User getUserById(long id) {
        return null;
    }

    public User signin(String email, String password) {
        return null;
    }
}
