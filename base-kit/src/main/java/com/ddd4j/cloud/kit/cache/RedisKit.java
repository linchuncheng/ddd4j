package com.ddd4j.cloud.kit.cache;

import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

@UtilityClass
@Slf4j
public class RedisKit {
    // 使用 ThreadLocal 来管理每个线程的 Jedis 实例
    private final ThreadLocal<Jedis> jedisThreadLocal = ThreadLocal.withInitial(RedisKit::createJedis);

    /**
     * 创建并返回一个Jedis连接实例
     *
     * @return Jedis连接实例
     */
    private Jedis createJedis() {
        String host = SpringContext.getEnv().getProperty("spring.redis.host");
        String port = SpringContext.getEnv().getProperty("spring.redis.port", "6379");
        String username = SpringContext.getEnv().getProperty("spring.redis.username");
        String password = SpringContext.getEnv().getProperty("spring.redis.password");

        Jedis jedis = new Jedis(host, Integer.parseInt(port));
        if (password != null && !password.isEmpty()) {
            if (username != null && !username.isEmpty()) {
                jedis.auth(username, password);
            } else {
                jedis.auth(password);
            }
        }
        return jedis;
    }

    /**
     * 获取当前线程的 Jedis 实例。
     *
     * @return 返回当前线程的 Jedis 实例
     */
    private Jedis jedis() {
        Jedis jedis = jedisThreadLocal.get();
        if (jedis == null || !jedis.isConnected()) {
            jedisThreadLocal.set(createJedis());
        }
        return jedisThreadLocal.get();
    }

    /**
     * 获取Jedis连接实例
     *
     * @return 返回Jedis连接实例
     */
    public Jedis getJedis() {
        return jedis();
    }

    /**
     * 将指定的键值对存储到Redis中，并设置过期时间。
     *
     * @param <T>            值的类型
     * @param key            键名
     * @param value          键值
     * @param expiredSeconds 过期时间（秒）
     * @return 设置成功返回true，否则返回false
     */
    public <T> Boolean set(String key, T value, long expiredSeconds) {
        try (Jedis jedis = jedis()) {
            String result = jedis.set(key, value instanceof String ? (String) value : JsonKit.toJson(value), SetParams.setParams().ex(expiredSeconds));
            return "OK".equals(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set key: " + key, e);
        }
    }

    /**
     * 如果指定的键不存在，则将其与给定的值关联。
     *
     * @param <T>            值的类型
     * @param key            键的名称
     * @param value          与键关联的值
     * @param expiredSeconds 键的过期时间（以秒为单位）
     * @return 如果键之前不存在并且设置成功，则返回 true；否则返回 false
     */
    public <T> Boolean setIfAbsent(String key, T value, long expiredSeconds) {
        try (Jedis jedis = jedis()) {
            String result = jedis.set(key, value instanceof String ? (String) value : JsonKit.toJson(value), SetParams.setParams().ex(expiredSeconds).nx());
            return "OK".equals(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setIfAbsent key: " + key, e);
        }
    }

    /**
     * 执行脚本，带参数版本
     * <p>
     * 等同于： public Object eval(final String script, final List<String> keys, final List<String> args)
     */
    public String eval(String script, List<String> keys, List<String> args) {
        try (Jedis jedis = jedis()) {
            return jedis.eval(script, keys, args).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to eval script: " + script, e);
        }
    }


    /**
     * 纯执行脚本，无参数版
     * <p>
     * 等同于：  public Object eval(final String script) ；
     */
    public String eval(String script) {
        try (Jedis jedis = jedis()) {
            return jedis.eval(script).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to eval script: " + script, e);
        }
    }


    /**
     * 将指定的键值对存储到 Redis 中。
     *
     * @param <T>   值的类型
     * @param key   要存储的键
     * @param value 要存储的值
     * @return 存储成功返回 true，否则返回 false
     */
    public <T> Boolean set(String key, T value) {
        try (Jedis jedis = jedis()) {
            String result = jedis.set(key, value instanceof String ? (String) value : JsonKit.toJson(value));
            return "OK".equals(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set key: " + key, e);
        }
    }

    /**
     * 如果指定的键不存在，则将其设置为指定的值，并返回true；如果键已存在，则不做任何操作，并返回false。
     *
     * @param key   要设置的键
     * @param value 要设置的值
     * @param <T>   值的类型
     * @return 如果键之前不存在并成功设置，则返回true；如果键已存在，则返回false
     */
    public <T> Boolean setIfAbsent(String key, T value) {
        try (Jedis jedis = jedis()) {
            Long result = jedis.setnx(key, value instanceof String ? (String) value : JsonKit.toJson(value));
            return result != null && result == 1L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to setIfAbsent key: " + key, e);
        }
    }

    /**
     * 对指定键的值进行自增操作。
     *
     * @param key 要进行自增操作的键
     * @return 自增后的值
     */
    public Long increment(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.incr(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to increment key: " + key, e);
        }
    }

    /**
     * 从Redis中获取指定键的值
     *
     * @param key 要获取的键
     * @return 键对应的值，如果键不存在则返回null
     */
    public String get(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.get(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get key: " + key, e);
        }
    }

    /**
     * 根据给定的键获取对应的值，如果找不到该键则返回默认值。
     *
     * @param key          要查找的键
     * @param defaultValue 如果找不到该键时返回的默认值
     * @return 如果找到对应的值则返回该值，否则返回默认值
     */
    public String get(String key, String defaultValue) {
        try {
            String value = get(key);
            if (value == null) {
                return defaultValue;
            }
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 根据给定的键名和类类型获取对应的列表。
     *
     * @param key   键名
     * @param clazz 类类型
     * @param <T>   泛型类型
     * @return 与键名对应的列表
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        return JsonKit.toList(get(key), clazz);
    }

    /**
     * 检查 Redis 中是否存在指定的键。
     *
     * @param key 要检查的键
     * @return 如果键存在，则返回 true；否则返回 false
     */
    public Boolean hasKey(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.exists(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get key: " + key, e);
        }
    }

    /**
     * 根据指定的键获取对应的值，并将其转换为指定类型的对象。
     *
     * @param key   键
     * @param clazz 要转换成的目标类类型
     * @param <T>   目标类的类型
     * @return 返回指定类型的对象，如果键不存在或转换失败则返回null
     */
    public <T> T get(String key, Class<T> clazz) {
        String value = get(key);
        if (value != null && value.contains("{") && value.endsWith("}")) {
            value = value.substring(value.indexOf("{"), value.lastIndexOf("}") + 1);
        }
        return JsonKit.toObject(value, clazz);
    }

    /**
     * 删除一个或多个Redis键
     *
     * @param keys 要删除的Redis键的数组
     * @return 被删除的键的数量
     */
    public Long delete(String... keys) {
        if (keys == null || keys.length == 0)
            return 0L;
        try (Jedis jedis = jedis()) {
            return jedis.del(keys);
        } catch (Exception e) {
            throw new RuntimeException("Failed to del keys: " + Arrays.toString(keys), e);
        }
    }

    /**
     * 根据给定的键列表从Redis中获取对应的值列表。
     *
     * @param keys 要获取值的键列表
     * @return 包含与给定键对应的值的列表
     */
    public List<String> get(List<String> keys) {
        try (Jedis jedis = jedis()) {
            List<String> values = new ArrayList<>();
            for (String key : keys) {
                values.add(jedis.get(key));
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get keys: " + keys, e);
        }
    }

    /**
     * 将给定的键列表转换为目标类型的列表。
     *
     * @param keys  需要转换的键列表
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 目标类型的列表
     */
    public <T> List<T> get(List<String> keys, Class<T> clazz) {
        return JsonKit.toList(get(keys), clazz);
    }

    /**
     * 设置键的过期时间
     *
     * @param key            键名
     * @param expiredSeconds 过期时间（秒）
     * @return 如果设置成功，返回 true；否则返回 false
     */
    public Boolean expire(String key, long expiredSeconds) {
        try (Jedis jedis = jedis()) {
            Long result = jedis.expire(key, expiredSeconds);
            return result != null && result == 1L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to expire key: " + key, e);
        }
    }

    /**
     * 根据给定的模式返回匹配的键的列表。
     *
     * @param pattern 键的模式，用于匹配Redis中的键
     * @return 匹配的键的列表
     */
    public List<String> keys(String pattern) {
        try (Jedis jedis = jedis()) {
            Set<String> keys = jedis.keys(pattern);
            return new ArrayList<>(keys);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get keys with pattern: " + pattern, e);
        }
    }

    //------------hash操作类--------------

    /**
     * 通过指定的键（key）和字段（field）获取存储在Redis哈希中的值（value）。
     *
     * @param key   存储数据的键
     * @param field 哈希中的字段名
     * @return 返回对应字段的值
     */
    public static String hget(String key, String field) {
        try (Jedis jedis = jedis()) {
            return jedis.hget(key, field);
        } catch (Exception e) {
            throw new ServiceException("Failed to hget value by key: ", key, e);
        }
    }

    /**
     * 通过指定的key和field给哈希表设置指定的值。如果key不存在，则先创建该key对应的哈希表。
     * <p>
     * 返回值：如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 ;
     * 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0 。
     *
     * @param key   哈希表的key
     * @param field 哈希表中要设置的字段
     * @param value 要设置的值
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 ;
     * 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0 。
     */
    public static Long hset(String key, String field, String value) {
        try (Jedis jedis = jedis()) {
            return jedis.hset(key, field, value);
        } catch (Exception e) {
            throw new ServiceException("Failed to hset value by key: ", key, e);
        }
    }

    /**
     * 通过指定的key和field判断是否存在对应的value。
     *
     * @param key   对应的key
     * @param field 对应的field
     * @return 如果存在则返回true，否则返回false
     */
    public static Boolean hexists(String key, String field) {

        try (Jedis jedis = jedis()) {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            throw new ServiceException("Failed to hexists value by key: ", key, e);
        }
    }

    /**
     * 通过key返回field的数量
     *
     * <p>通过给定的key从Redis中查询对应hash表中field的数量。</p>
     *
     * @param key Redis中存储数据的key
     * @return 返回field的数量
     */
    public static Long hlen(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.hlen(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to hlen value by key: ", key, e);
        }
    }

    /**
     * 通过给定的 key 删除指定的字段。
     *
     * <p>此方法使用 Jedis 连接池进行操作，通过 key 删除对应的字段，并返回被删除字段的数量。
     *
     * @param key    Redis 键
     * @param fields 要删除的字段，可以是一个或多个
     * @return 被删除字段的数量
     */
    public static Long hdel(String key, String... fields) {
        try (Jedis jedis = jedis()) {
            return jedis.hdel(key, fields);
        } catch (Exception e) {
            throw new ServiceException("Failed to hdel value by key: ", key, e);
        }
    }

    /**
     * 通过给定的key获取与之关联的所有field的集合。
     *
     * @param key 指定的key，用于查询其关联的field
     * @return 包含所有与指定key关联的field的集合
     */
    public static Set<String> hkeys(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.hkeys(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to hkeys value by key: ", key, e);
        }
    }

    /**
     * 通过key获取所有的field和value。
     *
     * @param key 需要获取field和value的key
     * @return 包含所有field和value的Map对象
     */
    public static Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to hgetAll value by key: ", key, e);
        }
    }

    /**
     * 通过给定的键向列表头部添加字符串。
     *
     * <p>通过给定的键向列表头部添加字符串，并返回执行 LPUSH 命令后列表的长度。</p>
     *
     * @param key  要添加字符串的列表的键
     * @param strs 要添加到列表头部的字符串数组
     * @return 执行 LPUSH 命令后列表的长度
     */
    public static Long lpush(String key, String... strs) {
        try (Jedis jedis = jedis()) {
            return jedis.lpush(key, strs);
        } catch (Exception e) {
            throw new ServiceException("Failed to lpush value by key: ", key, e);
        }
    }

    /**
     * 通过key向list尾部添加字符串
     *
     * <p>该方法通过指定的key将一个或多个值插入到列表的尾部（最右边）。
     * 如果key不存在，一个空列表会被创建并执行RPUSH操作。
     * 当key存在但不是列表类型时，返回一个错误。
     *
     * <p>返回值：执行RPUSH命令后，列表的长度
     *
     * @param key  列表的键
     * @param strs 要添加到列表尾部的字符串数组
     * @return 返回列表的长度
     */
    public static Long rpush(String key, String... strs) {
        try (Jedis jedis = jedis()) {
            return jedis.rpush(key, strs);
        } catch (Exception e) {
            throw new ServiceException("Failed to rpush value by key: ", key, e);
        }
    }

    /**
     * 通过key设置list指定下标位置的value。如果下标超过list里面value的个数则报错。
     *
     * <p>返回值：操作成功返回 "ok"，否则返回错误信息。</p>
     *
     * @param key   list的key
     * @param index 需要设置value的下标
     * @param value 要设置的value
     * @return 操作结果
     */
    public static String lset(String key, Long index, String value) {
        try (Jedis jedis = jedis()) {
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            throw new ServiceException("Failed to lset value by key: ", key, e);
        }
    }

    /**
     * 通过给定的key从对应的list中删除指定数量的与value相同的元素
     *
     * @param key   要操作的key
     * @param count 要删除的元素数量，如果count大于0，从头开始删除count个匹配的元素；
     *              如果count小于0，从尾开始删除count个匹配的元素；
     *              如果count等于0，删除所有匹配的元素
     * @param value 要删除的元素值
     * @return 被删除的元素数量
     */
    public static Long lrem(String key, long count, String value) {
        try (Jedis jedis = jedis()) {
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            throw new ServiceException("Failed to lrem value by key: ", key, e);
        }
    }

    /**
     * 通过指定的key保留list中从start下标开始到end下标结束的value值。
     *
     * @param key   要操作的key
     * @param start 要保留的起始下标（包含）
     * @param end   要保留的结束下标（包含）
     * @return 操作成功返回 "OK"，否则返回错误信息
     */
    public static String ltrim(String key, long start, long end) {
        try (Jedis jedis = jedis()) {
            return jedis.ltrim(key, start, end);
        } catch (Exception e) {
            throw new ServiceException("Failed to ltrim value by key: ", key, e);
        }
    }

    /**
     * 通过给定的键从列表头部删除一个值，并返回该值。
     *
     * <p>此方法使用Jedis连接Redis服务器，并执行LPOP命令。LPOP命令从列表的头部移除并返回列表的第一个元素。
     * 如果列表为空，则返回null。
     *
     * @param key Redis中存储列表的键
     * @return 从列表中移除的元素的值，如果列表为空则返回null
     */
    public static String lpop(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.lpop(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to execute lpop operation for key: ", key, e);
        }
    }

    /**
     * 通过给定的key从Redis列表的尾部删除一个元素，并返回该元素的值。
     *
     * @param key Redis中的key值
     * @return 返回从Redis列表中删除的元素的值，若列表为空则返回null
     */
    public static String rpop(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.rpop(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to execute rpop operation for key: ", key, e);
        }
    }

    /**
     * 通过key获取list中指定下标位置的value
     *
     * @param key   用于标识Redis中的list的key
     * @param index 指定list中的下标位置
     * @return 返回list中指定下标位置的value值
     */
    public static String lindex(String key, long index) {
        try (Jedis jedis = jedis()) {
            return jedis.lindex(key, index);
        } catch (Exception e) {
            throw new ServiceException("Failed to get value from list by index: ", key, e);
        }
    }

    /**
     * 通过key返回list的长度
     *
     * @param key 要查询长度的list的key
     * @return list的长度
     */
    public static Long llen(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.llen(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to get list length for key: ", key, e);
        }
    }

    /**
     * 通过key获取list指定下标位置的value。如果start为0，end为-1，则返回全部的list中的value。
     *
     * @param key   键名
     * @param start 起始下标，从0开始
     * @param end   结束下标，-1表示到list的末尾
     * @return 返回一个包含指定范围内value的列表
     */
    public static List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = jedis()) {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            throw new ServiceException("Failed to execute lrange operation for key: ", key, e);
        }
    }

//--------------set操作类------------

    /**
     * 通过给定的key向指定的set中添加一个或多个member元素。
     *
     * <p>此方法首先尝试从Jedis连接池中获取一个Jedis连接，然后使用该连接调用Jedis的sadd方法，
     * 将给定的key和member添加到set中。如果添加成功，将返回添加的member数量。
     *
     * <p>如果在操作过程中出现异常，将抛出一个ServiceException异常，并附带异常信息和相关参数。
     *
     * @param key     指定的set的key
     * @param members 要添加到set中的member元素，可以是一个或多个
     * @return 添加成功的member元素个数
     */
    public static Long sadd(String key, String... members) {
        try (Jedis jedis = jedis()) {
            return jedis.sadd(key, members);
        } catch (Exception e) {
            throw new ServiceException("Failed to add value to set by key: ", key, e);
        }
    }

    /**
     * 通过给定的key删除set中对应的value值。
     *
     * @param key     要操作的set的key
     * @param members 要删除的value值，可以为多个
     * @return 删除成功的个数
     */
    public static Long srem(String key, String... members) {
        try (Jedis jedis = jedis()) {
            return jedis.srem(key, members);
        } catch (Exception e) {
            throw new ServiceException("Failed to remove value from set by key: ", key, e);
        }
    }

    /**
     * 通过指定的键从集合中获取值的数量。
     *
     * <p>该方法使用Jedis客户端连接Redis服务器，并通过键从Redis集合中获取值的数量。
     * 如果在操作过程中出现异常，将抛出ServiceException异常。
     *
     * @param key 指定的键
     * @return Redis集合中值的数量
     */
    public static Long scard(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.scard(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to get scard value by key: ", key, e);
        }
    }

    /**
     * 通过给定的键（key）判断值（value）是否存在于集合（set）中。
     *
     * @param key    用于查询的键
     * @param member 需要判断的值
     * @return 如果值存在于集合中，则返回true；否则返回false
     */
    public static Boolean sismember(String key, String member) {
        try (Jedis jedis = jedis()) {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            throw new ServiceException("Failed to sismember value by key: ", key, e);
        }
    }

    /**
     * 通过指定的key获取Redis集合中所有的value。
     *
     * @param key Redis集合的key
     * @return 返回所有value的集合
     */
    public static Set<String> smembers(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to smembers value by key: ", key, e);
        }
    }

    //---------------zset操作类----------------

    /**
     * 通过key向zset中添加value和score，其中score用于排序。如果该value已存在，则根据score更新元素。
     *
     * @param key    zset的键
     * @param score  用于排序的分数
     * @param member 要添加到zset的成员
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员
     */
    public static Long zadd(String key, double score, String member) {
        try (Jedis jedis = jedis()) {
            return jedis.zadd(key, score, member);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key向zset中添加value,score，key为：", key, e);
        }
    }

    /**
     * 通过key删除在zset中指定的value。
     *
     * <p>此方法使用指定的key在zset中删除一个或多个指定的value。
     * 如果删除成功，将返回被删除的value的数量；如果删除失败，将抛出ServiceException异常。
     *
     * @param key     指定的zset的key
     * @param members 需要删除的value数组
     * @return 被删除的value的数量
     */
    public static Long zrem(String key, String... members) {
        try (Jedis jedis = jedis()) {
            return jedis.zrem(key, members);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key删除在zset中指定的value，key为：", key, e);
        }
    }

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key    zset的key
     * @param score  需要增加的分数值
     * @param member 需要增加分数的value
     * @return member 成员的新分数值
     */
    public static Double zincrby(String key, double score, String member) {
        try (Jedis jedis = jedis()) {
            return jedis.zincrby(key, score, member);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key增加该zset中value的score的值，key为：", key, e);
        }
    }

    /**
     * 通过key返回zset中value的排名，下标从小到大排序
     *
     * @param key    zset的key
     * @param member zset中的成员
     * @return member在zset中的排名（从0开始）
     */
    public static Long zrank(String key, String member) {
        try (Jedis jedis = jedis()) {
            return jedis.zrank(key, member);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key返回zset中value的排名，key为：", key, e);
        }
    }

    /**
     * 通过指定的key，从zset中获取score从start到end之间的元素，并按score从大到小排序。
     * 当start为0且end为-1时，返回zset中的所有元素。
     *
     * @param key   要查询的zset的key
     * @param start 查询的起始位置（包含）
     * @param end   查询的结束位置（包含）
     * @return 返回包含排序后元素的集合
     */
    public static Set<String> zrevrange(String key, long start, long end) {
        try (Jedis jedis = jedis()) {
            return new HashSet<>(jedis.zrevrange(key, start, end));
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key获取score从start到end中zset的value，key为 ", key, e);
        }
    }

    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key Redis中zset的键
     * @param min 区间最小值
     * @param max 区间最大值
     * @return 返回指定区间内zset中value的数量
     */
    public static Long zcount(String key, String min, String max) {
        try (Jedis jedis = jedis()) {
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key返回指定区间内zset中value的数量，key为：", key, e);
        }
    }

    /**
     * 通过给定的key返回zset中元素的个数。
     *
     * @param key zset的key
     * @return 返回zset中元素的个数
     */
    public static Long zcard(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.zcard(key);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to 通过key返回zset中的value个数，key为：", key, e);
        }
    }

    /**
     * 根据key获取有序集合中指定范围内的元素
     *
     * @param key   有序集合的键
     * @param start 起始位置，从0开始计数，包含该位置
     * @param end   结束位置，从0开始计数，不包含该位置
     * @return 指定范围内的元素集合
     * @throws ServiceException 如果获取元素过程中发生异常，抛出该异常
     */
    public static Set<String> zrange(String key, long start, long end) {
        try (Jedis jedis = jedis()) {
            return new HashSet<>(jedis.zrange(key, start, end));
        } catch (Exception e) {
            throw new ServiceException("zset=> Failed to 通过key获取有序集合中指定范围内的元素，key为：", key, e);
        }
    }

    /**
     * 通过key判断值的类型
     *
     * <p>该方法使用Jedis客户端连接Redis服务器，根据给定的key判断其对应的值的类型，并返回该类型。
     * 如果在操作过程中出现异常，将抛出ServiceException异常，并携带错误信息。
     *
     * @param key 用于判断值的类型的key
     * @return 返回值的类型，可能的返回值为：string、list、set、zset、hash
     */
    public static String type(String key) {
        try (Jedis jedis = jedis()) {
            return jedis.type(key);
        } catch (Exception e) {
            throw new ServiceException("zset=>Failed to type value by key: ", key, e);
        }
    }

    /**
     * 分布式锁同步执行
     *
     * @param key      锁Key
     * @param expiry   过期时间，单位秒
     * @param supplier 执行逻辑
     * @param <T>      返回类型
     * @return 运行结果
     */
    public <T> T sync(String key, long expiry, Supplier<T> supplier) {
        String lockValue = String.valueOf(System.currentTimeMillis());
        try {
            // 获取分布式锁
            while (!RedisKit.setIfAbsent(key, lockValue, expiry)) {
                Thread.sleep(100); // 等待100ms后重试
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            // 释放锁
            String currentValue = RedisKit.get(key);
            if (currentValue != null && currentValue.equals(lockValue)) {
                RedisKit.delete(key);
            }
        }
    }
}