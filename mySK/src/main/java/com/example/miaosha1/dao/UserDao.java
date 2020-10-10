package com.example.miaosha1.dao;

import com.example.miaosha1.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    public User getById(@Param("id") int id);


    @Insert("insert into user(id,name) values(#{id},#{name})")
    public boolean insert(User user);
}



