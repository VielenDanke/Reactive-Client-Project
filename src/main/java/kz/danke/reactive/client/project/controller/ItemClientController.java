package kz.danke.reactive.client.project.controller;

import kz.danke.reactive.client.project.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemClientController {

    private final WebClient webClient;

    @Autowired
    public ItemClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve() {
        return webClient
                .get()
                .uri("/api/rest/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log("Items in client project: ");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange() {
        return webClient
                .get()
                .uri("/api/rest/items")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log("Items in client project: ");
    }

    @GetMapping("/client/retrieve/{id}")
    public Mono<Item> getItemByIdFromRetrieve(@PathVariable(name = "id") String id) {
        return webClient
                .get()
                .uri("/api/rest/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Items in client project (single item): ");
    }

    @GetMapping("/client/exchange/{id}")
    public Mono<Item> getItemByIdFromExchange(@PathVariable(name = "id") String id) {
        return webClient
                .get()
                .uri("/api/rest/items/{id}", id)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class));
    }

    @PostMapping("/client")
    public Mono<Item> createItem(@RequestBody Item item) {
        Mono<Item> monoItem = Mono.just(item);

        return webClient
                .post()
                .uri("/api/rest/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(monoItem, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Created item: ");
    }

    @PutMapping("/client/{id}")
    public Mono<Item> updateItem(@PathVariable(name = "id") String id,
                                 @RequestBody Item item) {
        Mono<Item> itemMono = Mono.just(item);

        return webClient
                .put()
                .uri("/api/rest/items/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemMono, Item.class)
                .retrieve()
                .bodyToMono(Item.class);
    }

    @DeleteMapping("/client/{id}")
    public Mono<Void> deleteItem(@PathVariable(name = "id") String id) {
        return webClient
                .delete()
                .uri("/api/rest/items", id)
                .retrieve()
                .bodyToMono(Void.class)
                .log();
    }

    @GetMapping("/client/retrieve/exception/occurred")
    public Flux<Item> errorRetrieve() {
        return webClient
                .get()
                .uri("/api/rest/items/exception/occurred")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                    return errorMono
                            .flatMap(msg -> {
                                log.error(msg);
                                return Mono.error(new RuntimeException(msg));
                            });
                })
                .bodyToFlux(Item.class);
    }

    @GetMapping("/client/exchange/exception/occurred")
    public Flux<Item> errorExchange() {
        return webClient
                .get()
                .uri("/api/rest/items/exception/occurred")
                .exchange()
                .flatMapMany(clientResponse -> {
                    boolean is5xxError = clientResponse.statusCode().is5xxServerError();

                    if (is5xxError) {
                        return clientResponse
                                .bodyToMono(String.class)
                                .flatMap(msg -> {
                                    log.error(msg);
                                    return Mono.error(new RuntimeException(msg));
                                });
                    }
                    return clientResponse.bodyToFlux(Item.class);
                });
    }
}
