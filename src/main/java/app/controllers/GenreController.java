package app.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.models.entity.Genre;
import app.repositories.GenreRepository;

/**
 * 
 * @author abdullah jamal
 *
 *         GET : /game/genre/{genre_id}
 *
 *         GET : /game/genre/list
 *
 * @Admin PUT : /game/genre/{genre_id}
 *
 * @Admin POST : /game/genre/list
 *
 * @Admin DELETE : game/genre/{genre_id}
 */

@RestController
@RequestMapping("/genres")
public class GenreController {

	@Autowired
	private GenreRepository genreRepo;

	@GetMapping("/{genre_id}")
	public Optional<Genre> displayGenreById(@PathVariable("genre_id") Long id) {

		return genreRepo.findById(id);
    }
    
	@GetMapping("")
    public Iterable<Genre> displayGenreList() {

		return genreRepo.findAll();
    }
	
	@PutMapping("/{genre_id}")
    public Genre editGenreById(@RequestBody Genre g, @PathVariable("genre_id") Long id) {
		
		return genreRepo.save(g);
    }	
	
	@PostMapping("")
    public Genre createNewGenreType(@RequestBody Genre g) {

		return genreRepo.save(g);
    }
	
	@DeleteMapping("/{genre_id}")
    public void deleteGenreById(@PathVariable Long id) {

		genreRepo.deleteById(id);		
    }	
}
