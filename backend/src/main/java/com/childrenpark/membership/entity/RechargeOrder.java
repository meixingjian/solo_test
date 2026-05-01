package com.childrenpark.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_order")
public class RechargeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;

    private Integer coinsReceived;

    private Integer giftCoins;

    private String paymentMethod;

    private String prepayId;

    private String transactionId;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
