package common.cache.minister.service;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BloomService {
    @Resource
    private RedissonClient client;
    public void test(){
        RBloomFilter<Long> filter = client.getBloomFilter("haha");
        filter.tryInit(10000,0.1);
        filter.add(12L);
    }
}
