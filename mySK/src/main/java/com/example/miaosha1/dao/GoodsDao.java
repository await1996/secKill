package com.example.miaosha1.dao;

import com.example.miaosha1.domain.MiaoshaGoods;
import com.example.miaosha1.domain.User;
import com.example.miaosha1.vo.GoodsVo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface GoodsDao {
    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();//将Goods和MiaoshaGoods整合成一个对象

    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id" +
            " where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    //写操作数据库自带锁，不会出问题
    //@Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(MiaoshaGoods goods);

    @Update("update miaosha_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
    void resetStock(MiaoshaGoods g);
}



