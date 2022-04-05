package com.jimisun.simplesharddata.thread;

import cn.hutool.core.util.StrUtil;
import com.jimisun.simplesharddata.mapper.SlaveMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * 消费数据线程
 */
public class ConsumptionDataThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ConsumptionDataThread.class);

    private RedisTemplate redisTemplate;

    private SlaveMapper slaveMapper;

    private List<Map> dataList = new ArrayList<>();

    public ConsumptionDataThread() {
    }

    public ConsumptionDataThread(RedisTemplate redisTemplate, SlaveMapper slaveMapper, List<Map> mapList) {
        this.redisTemplate = redisTemplate;
        this.dataList = mapList;
        this.slaveMapper = slaveMapper;
    }

    @Override
    public void run() {
        for (Map map : dataList) {
            try {
                //获取业务参数
                String zjhm = map.get("YW_ID_CARD").toString();
                //根据业务参数获取要将此表写入到哪张表·
                map.put("tableName", getPartitionTable(zjhm));
                //写入
                slaveMapper.copyData(map);
            } catch (RuntimeException runtimeException) {
                runtimeException.printStackTrace();
                continue;
            }
        }
        //将redis中到线程锁-1
        redisTemplate.opsForValue().increment("simplesharddata:lock", -1);
    }

    /**
     * 此处根据业务需要 返回要将此数据写入水平拆分表的表名
     *
     * @param zjhm 证件号码 根据证件号码最后一位判断要将此数据写入到哪个数据库
     * @return 数据库表名
     */
    private String getPartitionTable(String zjhm) {
        if (StrUtil.isNotEmpty(zjhm)) {
            String lastIndex = zjhm.substring(zjhm.length() - 1).toLowerCase(Locale.ROOT);
            if (lastIndex.equals("0")) {
                return "hs0";
            } else if (lastIndex.equals("1")) {
                return "hs1";
            } else if (lastIndex.equals("2")) {
                return "hs2";
            } else if (lastIndex.equals("3")) {
                return "hs3";
            } else if (lastIndex.equals("4")) {
                return "hs4";
            } else if (lastIndex.equals("5")) {
                return "hs5";
            } else if (lastIndex.equals("6")) {
                return "hs6";
            } else if (lastIndex.equals("7")) {
                return "hs7";
            } else if (lastIndex.equals("8")) {
                return "hs8";
            } else if (lastIndex.equals("9")) {
                return "hs9";
            } else if (lastIndex.equals("x") || lastIndex.equals("X")) {
                return "hsx";
            }
        }
        return "hs0";
    }

}
