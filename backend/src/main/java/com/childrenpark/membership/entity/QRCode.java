package com.childrenpark.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("qrcode")
public class QRCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String qrcodeData;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime expireTime;

    private Integer status;

    private Integer used;

    private LocalDateTime useTime;

    private LocalDateTime updateTime;
}
