package com.jimisun.simplesharddata.task;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jimisun.simplesharddata.mapper.MasterMapper;
import com.jimisun.simplesharddata.mapper.SlaveMapper;
import com.jimisun.simplesharddata.thread.ConsumptionDataThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 处理水平分表核心服务
 */
@Component
public class CoreTask {

    private static final Logger logger = LoggerFactory.getLogger(CoreTask.class);

    @Resource
    private MasterMapper masterMapper;

    @Resource
    private SlaveMapper slaveMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 初始被分表的数据id
     */
    @Value("${simplesharddata.config.initId}")
    private Long initId;

    /**
     * 每次任务需要启动多少个线程处理
     */
    @Value("${simplesharddata.config.threadNum}")
    private Integer threadNum;


    /**
     * 每次任务执行完毕需要休眠多长时间
     */
    @Value("${simplesharddata.config.threadSleepTime}")
    private Long threadSleepTime;

    /**
     * 每次任务处理多少个数据
     */
    @Value("${simplesharddata.config.batchNum}")
    private Long batchNum;

    /**
     * step 1 分批次获取被分表库表中的数据
     * step 2 将数据交给线程去消费数据【按照某种规则】
     * step 3 循环第一步
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void coreTask() throws InterruptedException {
        redisTemplate.opsForValue().set("simplesharddata:lock", "0"); //初始化锁为0
        while (true) {
            if (redisTemplate.opsForValue().get("simplesharddata:lock") == null || Integer.valueOf(redisTemplate.opsForValue().get("simplesharddata:lock")) == 0) {
                logger.info("【主任务开始开始执行................");
                logger.info("【消费数据任务已经启动，初始ID为{}】", initId);
                String redisInitIdString = redisTemplate.opsForValue().get("simplesharddata:initId");
                if (ObjectUtil.isNotEmpty(redisInitIdString)) {
                    logger.info("【检测到redis中存在初始ID，初始ID为{},已设置为{}】", initId, Long.valueOf(redisInitIdString));
                    initId = Long.valueOf(redisInitIdString);
                }
                Long maxInfoId = masterMapper.selectMaxInfoId();

                if (initId + batchNum <= maxInfoId) {
                    List<Map> maps = masterMapper.selectDataByParams(initId, initId + batchNum);
                    consumptionData(maps);
                    initId = initId + batchNum;
                } else if (initId < maxInfoId) {
                    List<Map> maps = masterMapper.selectDataByParams(initId, maxInfoId);
                    consumptionData(maps);
                    initId = maxInfoId;
                }
                redisTemplate.opsForValue().set("simplesharddata:initId", String.valueOf(initId));
                logger.info("【消费任务已经结束，初始ID已经变更为{}】", initId);
                logger.info("【主任务开始进入休眠中................】", initId);
                Thread.currentThread().sleep(threadSleepTime);
            } else {
                logger.info("【未满足触发条件................】", initId);
            }
            Thread.currentThread().sleep(3000);
        }

    }


    /**
     * 将数据分派给线程消费
     *
     * @param dataList
     */
    void consumptionData(List<Map> dataList) {
        List<List<Map>> lists = ListUtil.splitAvg(dataList, threadNum);
        redisTemplate.opsForValue().set("simplesharddata:lock", String.valueOf(lists.size()));
        for (List<Map> e : lists) {
            new ConsumptionDataThread(redisTemplate, slaveMapper, e).start();

        }
    }
}
