package common.cache.minister.service;

import io.lettuce.core.RedisClient;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Order(1)
public class RedisService {
    public static long RANDOM_TIMEOUT_MS = 5000;
    @Resource(name="redisTemplate")
    private RedisTemplate<String,Object> template;
    private ValueOperations<String, Object> redisString;
    private ZSetOperations<String, Object> redisZSet;
    private SetOperations<String, Object> redisSet;
    private ListOperations<String, Object> redisList;
    private HashOperations<String, Object, Object> redisHash;
    private HyperLogLogOperations<String, Object> redisHyperLogLog;
    private GeoOperations<String, Object> redisGeo;

    @PostConstruct
    private void init(){
        redisString = template.opsForValue();
        redisZSet = template.opsForZSet();
        redisSet = template.opsForSet();
        redisList = template.opsForList();
        redisHash = template.opsForHash();
        redisHyperLogLog = template.opsForHyperLogLog();
        redisGeo = template.opsForGeo();
    }
    public void expire(String key, long realTimeoutMs){
        /**
         * 防止缓存雪崩
         */
        template.expire(key,realTimeoutMs,TimeUnit.MILLISECONDS);
    }

    public void setEx(String key, Object value, long timeoutMs) {
        redisString.set(key,value,randomTimeoutMs(timeoutMs),TimeUnit.MILLISECONDS);
    }
    public Boolean setExNx(String key, Object value, long timeoutMs){
        return redisString.setIfPresent(key, value, randomTimeoutMs(timeoutMs), TimeUnit.MILLISECONDS);
    }
    public Object get(String key){
        return redisString.get(key);
    }
    public <T> T get(String key, Class<T> clazz){
        Object result = redisString.get(key);
        return (T)result;
    }
    public Boolean del(String key){
        return template.delete(key);
    }

    //Set的方法

    /**
     * 往set中加入原生并重设超时时间
     * @param key
     * @param timeoutMs
     * @param values
     */
    @Transactional
    public Long sAddEx(String key, long timeoutMs, Object ...values){
        Long successCnt = redisSet.add(key,values);
        expire(key,randomTimeoutMs(timeoutMs));
        return successCnt;
    }

    public Boolean sContains(String key, Object obj){
        return redisSet.isMember(key,obj);
    }

    public Long sRemove(String key, Object ...objs){
        return redisSet.remove(key, objs);
    }

    public Long sSize(String key){
        return redisSet.size(key);
    }

    public static class ScoreObj{
        private Object obj;
        private double score;
        public ScoreObj(Object obj, double score){
            this.obj = obj;
            this.score = score;
        }
    }

    //ZSet方法
    public Long zAddEx(String key, Set<ZSetOperations.TypedTuple<Object>> args, long timeoutMs){
        template.multi();
        Long ans = redisZSet.add(key,args);
        expire(key,randomTimeoutMs(timeoutMs));
        template.exec();
        return ans;
    }
    @Transactional
    public Boolean zAddEx(String key, Object obj, double score, long timeoutMs){
        template.multi();
        Boolean ans = redisZSet.add(key, obj, score);
        expire(key, randomTimeoutMs(timeoutMs));
        template.exec();
        return ans;
    }
//    public Boolean zContains(String key, Object obj){
//        return redisZSet.
//    }
    public Long zSize(String key){
        return redisZSet.size(key);
    }
    public Long zCount(String key, double min, double max){
        return redisZSet.count(key,min,max);
    }
    public Set<Object> zRange(String key, long start, long end){
        return redisZSet.range(key,start,end);
    }
    public Set<Object> zRangeByScore(String key, double min, double max){
        return redisZSet.rangeByScore(key,min,max);
    }
    public Long zRank(String key, Object obj){
        return redisZSet.rank(key,obj);
    }
    public Long zRem(String key, Object ...objs){
        return redisZSet.remove(key,objs);
    }
    public Long zRemByRank(String key, long start, long end){
        return redisZSet.removeRange(key,start,end);
    }
    public Long zRemByScore(String key, double min, double max){
        return redisZSet.removeRangeByScore(key,min,max);
    }
    public Double zScore(String key, Object obj){
        return redisZSet.score(key,obj);
    }



    private long randomTimeoutMs(long timeoutMs){
        return timeoutMs+(int)(Math.random()*RANDOM_TIMEOUT_MS);
    }

}
