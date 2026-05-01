package com.childrenpark.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer price;

    private Integer maxCapacity;

    private Integer status;

    private String icon;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
