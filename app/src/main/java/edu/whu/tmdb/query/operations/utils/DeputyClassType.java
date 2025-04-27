package edu.whu.tmdb.query.operations.utils;/*
 * className:DeputyClassType
 * Package:edu.whu.tmdb.Transaction.Transactions.utils
 * Description:
 * @Author: xyl
 * @Create:2023/9/10 - 15:21
 * @Version:v1
 */

public enum DeputyClassType {
    SELECT(0),  // 代理类0，表示select代理类
    JOIN(1),    // 代理类1，表示join代理类
    UNION(2),   // 代理类2，表示union代理类
    GROUPBY(3); // 代理类3，表示groupby代理类
    

    private final int value;

    DeputyClassType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
