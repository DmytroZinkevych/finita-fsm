package io.github.dmytrozinkevych.finitafsm.utils;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

    void accept(A a, B b, C c);
}
