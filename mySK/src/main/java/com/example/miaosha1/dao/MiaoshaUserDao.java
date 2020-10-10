package com.example.miaosha1.dao;

import com.example.miaosha1.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from miaosha_user where id = #{id}")
    public MiaoshaUser getById(long id);

    //只更新需要更新的字段
    @Update("update miaosha_user set password = #{password} where id = #{id}")
    void update(MiaoshaUser newUser);
}
