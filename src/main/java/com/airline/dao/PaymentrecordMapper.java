package com.airline.dao;

import com.airline.bean.Paymentrecord;
import com.airline.bean.PaymentrecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PaymentrecordMapper {
    long countByExample(PaymentrecordExample example);

    int deleteByExample(PaymentrecordExample example);

    int deleteByPrimaryKey(Integer paymentid);

    int insert(Paymentrecord record);

    int insertSelective(Paymentrecord record);

    List<Paymentrecord> selectByExample(PaymentrecordExample example);

    Paymentrecord selectByPrimaryKey(Integer paymentid);

    int updateByExampleSelective(@Param("record") Paymentrecord record, @Param("example") PaymentrecordExample example);

    int updateByExample(@Param("record") Paymentrecord record, @Param("example") PaymentrecordExample example);

    int updateByPrimaryKeySelective(Paymentrecord record);

    int updateByPrimaryKey(Paymentrecord record);
}