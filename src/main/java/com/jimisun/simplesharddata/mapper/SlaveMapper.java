package com.jimisun.simplesharddata.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.util.Map;

/**
 * 存储水平分表的数据
 */
@DS("slave")
public interface SlaveMapper {

    void copyData(Map params);

}
