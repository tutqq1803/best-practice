package com.tutqq.moviesapi.service.impl;

import com.tutqq.moviesapi.exception.UserExtraNotFoundException;
import com.tutqq.moviesapi.model.UserExtra;
import com.tutqq.moviesapi.repository.UserExtraRepository;
import com.tutqq.moviesapi.service.UserExtraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserExtraServiceImpl implements UserExtraService {

    private final UserExtraRepository userExtraRepository;

    private static final String KEY = "UserExtra";
    private static final String PRE_HASH_KEY = "userExtra_";
    private final RedisTemplate<String, UserExtra> redisTemplate;
    private HashOperations<String, String, UserExtra> hashOperations;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public UserExtra validateAndGetUserExtra(String username) {
        final String hashKey = PRE_HASH_KEY + username;
        final boolean hasKey = Boolean.TRUE.equals(hashOperations.hasKey(KEY, hashKey));
        if (hasKey) {
            final UserExtra post = hashOperations.get(KEY, hashKey);
            log.info("UserExtraServiceImpl.validateAndGetUserExtra() : cache post >> : {}", post);
            return post;
        }

        final Optional<UserExtra> userExtra = userExtraRepository.findById(username);
        if (userExtra.isPresent()) {
            hashOperations.put(KEY, hashKey, userExtra.get());
            log.info("UserExtraServiceImpl.validateAndGetUserExtra() : cache insert >> : {}", userExtra.get().toString());
            return userExtra.get();
        } else {
            throw new UserExtraNotFoundException(username);
        }
    }

    @Override
    public Optional<UserExtra> getUserExtra(String username) {
        return userExtraRepository.findById(username);
    }

    @Override
    public UserExtra saveUserExtra(UserExtra userExtra) {
        final String hashKey = PRE_HASH_KEY + userExtra.getUsername();
        final boolean hasKey = Boolean.TRUE.equals(hashOperations.hasKey(KEY, hashKey));
        if (hasKey) {
            hashOperations.delete(KEY, hashKey);
            log.info("UserExtraServiceImpl.saveUserExtra() : cache delete username >> " + userExtra.getUsername());
        }
        return userExtraRepository.save(userExtra);
    }
}
