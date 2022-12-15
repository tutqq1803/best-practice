package com.tutqq.moviesapi.service.impl;

import com.tutqq.moviesapi.exception.MovieNotFoundException;
import com.tutqq.moviesapi.model.Movie;
import com.tutqq.moviesapi.repository.MovieRepository;
import com.tutqq.moviesapi.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class MovieServiceImpl implements MovieService {
    private static final String KEY = "Movie";
    private static final String PRE_HASH_KEY = "movie_";
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Movie> redisTemplate;
    private HashOperations<String, String, Movie> hashOperations;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Movie validateAndGetMovie(String imdbId) {
        var hashKey = PRE_HASH_KEY + imdbId;
        final boolean hasKey = Boolean.TRUE.equals(hashOperations.hasKey(KEY, hashKey));
        if (hasKey) {
            final Movie post = hashOperations.get(KEY, hashKey);
            log.info("MovieServiceImpl.validateAndGetMovie() : cache post >> {}", post);
            return post;
        }

        final Optional<Movie> employee = movieRepository.findById(imdbId);
        if (employee.isPresent()) {
            hashOperations.put(KEY, hashKey, employee.get());
            log.info("MovieServiceImpl.validateAndGetMovie() : cache insert >> {}", employee.get().toString());
            return employee.get();
        } else {
            throw new MovieNotFoundException(imdbId);
        }
    }

    @Override
    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie saveMovie(Movie movie) {
        final String hashKey = PRE_HASH_KEY + movie.getImdbId();
        final boolean hasKey = Boolean.TRUE.equals(hashOperations.hasKey(KEY, hashKey));
        if (hasKey) {
            hashOperations.delete(KEY, hashKey);
            log.info("MovieServiceImpl.saveMovie() : cache delete >> {}", movie.toString());
        }

        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(Movie movie) {
        final String hashKey = PRE_HASH_KEY + movie.getImdbId();
        final boolean hasKey = Boolean.TRUE.equals(hashOperations.hasKey(KEY, hashKey));
        if (hasKey) {
            hashOperations.delete(KEY, hashKey);
            log.info("MovieServiceImpl.deleteMovie() : cache delete ImdbId >> {}", movie.getImdbId());
        }
        movieRepository.delete(movie);
    }
}