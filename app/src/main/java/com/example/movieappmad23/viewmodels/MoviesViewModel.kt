package com.example.movieappmad23.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.example.movieappmad23.models.Genre
import com.example.movieappmad23.models.ListItemSelectable
import com.example.movieappmad23.models.Movie
import com.example.movieappmad23.models.getMovies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// inherit from ViewModel class
class MoviesViewModel: ViewModel() {
    private val _movieListState = MutableStateFlow(listOf<Movie>())
    val movieListState: StateFlow<List<Movie>> = _movieListState.asStateFlow()

    private val _addMovieValidationState = MutableStateFlow(AddMovieValidationState())
    val addMovieValidationState: StateFlow<AddMovieValidationState> = _addMovieValidationState

    val favoriteMovies: List<Movie>
        get() = _movieListState.value.filter { it.isFavorite }

    // validation fields
    var isEnabledSaveButton: MutableState<Boolean> = mutableStateOf(false)
    var title = mutableStateOf("")
    var year = mutableStateOf("")
    var director = mutableStateOf("")
    var actors = mutableStateOf("")
    var plot = mutableStateOf("")
    var rating = mutableStateOf("")
    var selectableGenreItems = Genre.values().toList().map {genre ->
        ListItemSelectable(title = genre.toString())
    }.toMutableStateList()

    init {
        _movieListState.value = getMovies()
    }

    fun updateMovies(movie: Movie) = _movieListState.value.find { it.id == movie.id }?.let { movie ->
        movie.isFavorite = !movie.isFavorite
    }

    fun selectGenre(selectedItem: ListItemSelectable) = selectableGenreItems.find { it.title == selectedItem.title }?.let { genre ->
        genre.isSelected = !genre.isSelected
    }

    private fun shouldEnableAddButton() {
        isEnabledSaveButton.value = title.value.trim().isNotEmpty()
                && year.value.trim().isNotEmpty()
                && director.value.trim().isNotEmpty()
                && actors.value.trim().isNotEmpty()
                && rating.value.trim().isNotEmpty() && _addMovieValidationState.value.ratingErrMsg.isEmpty()
                && _addMovieValidationState.value.genreErrMsg.isEmpty()
    }

    fun validateTitle() {
        if(title.value.trim().isNotEmpty()){
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isTitleValid = true,
                    titleErrMsg = ""
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isTitleValid = false,
                    titleErrMsg = "Title is required"
                )
            }
        }
        shouldEnableAddButton()
    }

    fun validateYear() {
        if (year.value.trim().isNotEmpty()) {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isYearValid = true,
                    yearErrMsg = ""
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isYearValid = false,
                    yearErrMsg = "Year is required"
                )
            }
        }
        shouldEnableAddButton()
    }

    fun validateDirector() {
        if (director.value.trim().isNotEmpty()) {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isDirectorValid = true,
                    directorErrMsg = ""
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isDirectorValid = false,
                    directorErrMsg = "Director is required"
                )
            }
        }
        shouldEnableAddButton()
    }

    fun validateActors() {
        if (actors.value.trim().isNotEmpty()) {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isActorsValid = true,
                    actorsErrMsg = ""
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isActorsValid = false,
                    actorsErrMsg = "Actors is required"
                )
            }
        }
        shouldEnableAddButton()
    }

    fun validateRating(){
        val ratingVal = rating.value.toFloatOrNull()

        if(rating.value.trim().isNotEmpty()
            && !rating.value.startsWith("0")
            && (ratingVal != null)
            && (ratingVal >= 0)
            && (ratingVal <= 10)
        ){
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isRatingValid = true,
                    ratingErrMsg = ""
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    isRatingValid = false,
                    ratingErrMsg = "Rating is required and must be valid decimal format."
                )
            }
        }

        shouldEnableAddButton()
    }

    fun validateGenres(){
        if(selectableGenreItems.filter { item -> item.isSelected }.isEmpty()) {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    genreErrMsg = "Genre is required"
                )
            }
        } else {
            _addMovieValidationState.update { currentState ->
                currentState.copy(
                    genreErrMsg = ""
                )
            }
        }
        shouldEnableAddButton()
    }

    fun addMovie() {
        val selectedGenres = selectableGenreItems.filter { item -> item.isSelected }
            .map { listItemSelectable ->
                Genre.valueOf(listItemSelectable.title)
            }

        val movie = Movie(
            title = title.value,
            director = director.value,
            actors = actors.value,
            plot = plot.value.trim(),
            genre = selectedGenres,
            year = year.value,
            images = listOf(),
            rating = rating.value.toFloat()
        )

        _movieListState.update {
            val list: MutableList<Movie> = _movieListState.value.toMutableList()
            list.add(movie)
            list
        }

        reset()
    }

    fun reset() {
        isEnabledSaveButton.value = false
        _addMovieValidationState.value = AddMovieValidationState()
        title.value = ""
        director.value = ""
        actors.value = ""
        plot.value = ""
        year.value = ""
        rating.value = ""

        selectableGenreItems.find { it.isSelected }?.let { genre ->
            genre.isSelected = false
        }
    }
}