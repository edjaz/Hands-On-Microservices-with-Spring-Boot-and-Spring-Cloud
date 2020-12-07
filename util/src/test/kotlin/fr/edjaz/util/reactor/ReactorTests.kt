package fr.edjaz.util.reactor

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.util.ArrayList

class ReactorTests {
    @Test
    fun TestFlux() {
        val list: MutableList<Int> = ArrayList()
        Flux.just(1, 2, 3, 4)
            .filter { n: Int -> n % 2 == 0 }
            .map { n: Int -> n * 2 }
            .log()
            .subscribe { e: Int -> list.add(e) }
        Assertions.assertThat(list).containsExactly(4, 8)
    }

    @Test
    fun TestFluxBlocking() {
        val list = Flux.just(1, 2, 3, 4)
            .filter { n: Int -> n % 2 == 0 }
            .map { n: Int -> n * 2 }
            .log()
            .collectList().block()
        Assertions.assertThat(list).containsExactly(4, 8)
    }
}
