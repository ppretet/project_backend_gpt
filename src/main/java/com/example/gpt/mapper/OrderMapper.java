package com.example.gpt.mapper;

import com.example.gpt.DAO.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    @Select("select id from `order` where order_id=#{order_id}")
    Integer findByOrderId(String order_id);
    @Select("select * from `order` where order_id=#{order_id}")
    Order findOrderByOrderId(String order_id);
    @Select("select * from `order` where user_id=#{user_id} and status='pending' order by created_at desc")
    List<Order> queryAllbyUserId(Integer user_id);

    @Select("SELECT g.detail, o.id,o.user_id from goods as g left join `order` as o on g.goods_id=o.goods_id where o.order_id = #{order_id}")
    Map<String, Object> getDetailAndOrderIdByOrderId(String orderId);

    @Insert("insert into `order`(order_id,status,created_at,user_id,goods_id,pay_link,finish_at)values(#{order_id},#{status},#{created_at},#{user_id},#{goods_id},#{pay_link},#{finish_at})")
    int save(Order order);

    @Update("update `order` set status='success' where id=#{id}")
    int updateStatus(Integer id);
    @Update("update `order` set pay_link=#{pay_link},finish_at=#{finish_at},status=#{status} where order_id=#{order_id}")
    void updataPayANDFinishTime(Order order);
}
