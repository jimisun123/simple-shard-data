package com.jimisun.simplesharddata.mapper;

import java.util.List;
import java.util.Map;

/**
 * 需要被水平分表的dao层相关操作
 */
public interface MasterMapper {

    /**
     * 查询被水平分表的数据库中的某个数据表的最大自增ID
     *
     * @return
     */
    Long selectMaxInfoId();

    /**
     * 根据参数获取被分表的表中的数据
     *
     * @param start 开始记录
     * @param end   结束记录
     * @return
     */
    List<Map> selectDataByParams(Long start, Long end);

}
