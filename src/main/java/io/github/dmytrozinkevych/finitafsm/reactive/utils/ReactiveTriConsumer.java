package io.github.dmytrozinkevych.finitafsm.reactive.utils;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveTriConsumer<A, B, C> {

    Mono<Void> accept(A a, B b, C c);
}
