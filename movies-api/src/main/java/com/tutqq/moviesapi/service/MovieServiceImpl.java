package com.tutqq.moviesapi.service;

import com.tutqq.moviesapi.exception.MovieNotFoundException;
import com.tutqq.moviesapi.model.Movie;
import com.tutqq.moviesapi.repository.MovieRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public Movie validateAndGetMovie(String imdbId) {
        return movieRepository.findById(imdbId).orElseThrow(() -> new MovieNotFoundException(imdbId));
    }

    @Override
    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(Movie movie) {
        movieRepository.delete(movie);
    }
}