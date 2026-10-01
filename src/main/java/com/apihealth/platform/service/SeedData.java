package com.apihealth.platform.service;

import com.apihealth.platform.model.ApiTarget;
import com.apihealth.platform.repository.ApiTargetRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements CommandLineRunner {

    private final ApiTargetRepository targetRepository;

    public SeedData(ApiTargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @Override
    public void run(String... args) {
        seedMissingTargets();
    }

    public void seedMissingTargets() {
        for (ApiTarget target : defaults()) {
            if (!targetRepository.findByNameIgnoreCase(target.getName()).isPresent()) {
                targetRepository.save(target);
            }
        }
    }

    private List<ApiTarget> defaults() {
        return Arrays.asList(
                new ApiTarget("ViaCEP", "https://viacep.com.br/ws/01001000/json/", "GET"),
                new ApiTarget("JSONPlaceholder", "https://jsonplaceholder.typicode.com/posts/1", "GET"),
                new ApiTarget("GitHub API", "https://api.github.com/", "GET"),
                new ApiTarget("Open-Meteo", "https://api.open-meteo.com/v1/forecast?latitude=-23.55&longitude=-46.63&current_weather=true", "GET"),
                new ApiTarget("PokeAPI", "https://pokeapi.co/api/v2/pokemon/pikachu", "GET"),
                new ApiTarget("Cat Facts", "https://catfact.ninja/fact", "GET"),
                new ApiTarget("Dog CEO", "https://dog.ceo/api/breeds/image/random", "GET"),
                new ApiTarget("DummyJSON", "https://dummyjson.com/products/1", "GET"),
                new ApiTarget("Fake Store API", "https://fakestoreapi.com/products/1", "GET"),
                new ApiTarget("Random User", "https://randomuser.me/api/", "GET"),
                new ApiTarget("Agify", "https://api.agify.io/?name=maria", "GET"),
                new ApiTarget("Genderize", "https://api.genderize.io/?name=maria", "GET"),
                new ApiTarget("Nationalize", "https://api.nationalize.io/?name=maria", "GET"),
                new ApiTarget("REST Countries", "https://restcountries.com/v3.1/name/brazil", "GET"),
                new ApiTarget("IPify", "https://api.ipify.org?format=json", "GET"),
                new ApiTarget("IPapi", "https://ipapi.co/json/", "GET"),
                new ApiTarget("Frankfurter", "https://api.frankfurter.app/latest?from=USD", "GET"),
                new ApiTarget("Open-Meteo Geocoding", "https://geocoding-api.open-meteo.com/v1/search?name=Lisbon&count=1", "GET"),
                new ApiTarget("Chuck Norris Jokes", "https://api.chucknorris.io/jokes/random", "GET"),
                new ApiTarget("CocktailDB", "https://www.thecocktaildb.com/api/json/v1/1/random.php", "GET"),
                new ApiTarget("World Time API", "https://worldtimeapi.org/api/timezone/Etc/UTC", "GET"),
                new ApiTarget("Official Joke API", "https://official-joke-api.appspot.com/random_joke", "GET")
        );
    }
}