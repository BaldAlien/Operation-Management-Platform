package com.hrm.dao;

import com.hrm.pojo.Balance;

/**
 * @author zjw
 * @package backend
 * @Date 2021/7/13
 * @Time 16:46
 */
public interface BalanceDao {

    //通过user_id查找余额
    int checkMoneyByID(int user_id);

    //更新余额
    int updateMoneyById(Balance balance);
}
