package com.woopaca.webfluxdemo.web;

import com.woopaca.webfluxdemo.domain.Customer;
import com.woopaca.webfluxdemo.domain.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
public class CustomerController {

    private final CustomerRepository customerRepository;

    // A 요청 -> Flux - Stream
    // B 요청 -> Flux - Stream
    // -> Flux.merge -> sink
    private final Sinks.Many<Customer> sink; // 모든 클라이언트의 Flux 요청을 sink

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    // 데이터가 한 건이면 Mono, 여러 개면 Flux

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3, 4, 5)
                .delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/flux-stream",
            produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxStream() {
        return Flux.just(1, 2, 3, 4, 5)
                .delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/customer",
            produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer> findAll() {
        return customerRepository.findAll()
                .delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer/{id}")
    public Mono<Customer> findById(@PathVariable Long id) {
        return customerRepository.findById(id).log();
    }

    // 데이터가 모두 전달되어도 종료 X
    @GetMapping(value = "/customer/sse"/*, produces = MediaType.TEXT_EVENT_STREAM_VALUE*/)
    public Flux<ServerSentEvent<Customer>> findAllSSE() {
        return sink.asFlux().map(customer ->
                        ServerSentEvent.builder(customer).build())
                .publishOn(Schedulers.boundedElastic())
                .doOnCancel(() -> sink.asFlux().blockLast());
    }

    @PostMapping(value = "/customer")
    public Mono<Customer> save() {
        return customerRepository.save(new Customer("Chanwoo", "Ji"))
                .doOnNext(sink::tryEmitNext);
    }
}
