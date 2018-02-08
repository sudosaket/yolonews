package com.yolonews.common;

import com.google.common.base.Preconditions;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.yolonews.common.JedisProvider.JEDIS_POOL;

/**
 * @author saket.mehta
 */
public abstract class AbstractDaoRedis<Entity, ID> implements CrudDao<Entity, ID> {
    @Override
    public ID save(Entity entity) {
        Preconditions.checkNotNull(entity, "Entity is null");
        return tryWithJedis(jedis -> handleSave(jedis, entity));
    }

    @Override
    public Optional<Entity> findById(ID entityId) {
        Preconditions.checkNotNull(entityId, "ID is null");
        return tryWithJedis(jedis -> handleFindById(jedis, entityId));
    }

    @Override
    public void delete(ID entityId) {
        Preconditions.checkNotNull(entityId, "ID is null");
        tryWithJedis(jedis -> handleDelete(jedis, entityId));
    }

    protected <E> E tryWithJedis(Function<Jedis, E> function) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            return function.apply(jedis);
        }
    }

    protected abstract ID handleSave(Jedis jedis, Entity entity);

    protected abstract Optional<Entity> handleFindById(Jedis jedis, ID entityId);

    protected abstract Void handleDelete(Jedis jedis, ID entityId);

    protected abstract Entity fromMap(Map<String, String> map);

    protected abstract Map<String, String> toMap(Entity entity);
}
