package com.childrenpark.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("consume_record")
public class ConsumeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String recordNo;

    private Long userId;

    private Long projectId;

    private String projectName;

    private Integer quantity;

    private Integer amount;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;

    private Integer deleted;

    @TableField(exist = false)
    private String type = "consume";
}
