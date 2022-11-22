package common.cache.minister;

import common.cache.minister.service.BloomService;
import common.cache.minister.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class MinisterApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    RedisConnectionFactory factory;
    @Resource
    BloomService bloomService;
    @Resource
    RedisService redisService;
    @Test
    public void test(){
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory();
        RedisConnection connection = factory.getConnection();
//        connection.execute("bf.exists", "haha".getBytes(),"user1".getBytes());
        redisService.setEx("haha","haha",500);
//        redisService.setEx("haha","haha",50000);
    }
    @Test
    public void blomTest(){
        bloomService.test();
    }

}
