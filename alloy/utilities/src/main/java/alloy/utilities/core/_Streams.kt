package alloy.utilities.core

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Stream

/**
 * Created by jlutteringer on 1/17/18.
 */
object _Streams {
    @JvmStatic
    fun <T, N> mapIndexed(iterable: Stream<T>, mapper: BiFunction<T, Int, N>): Stream<N> {
        val i = AtomicInteger(0)
        return iterable.map { v -> mapper.apply(v, i.getAndIncrement()) }
    }

    @JvmStatic
    fun <T, N> mapIndexed(iterable: Collection<T>, mapper: BiFunction<T, Int, N>): Stream<N> {
        return mapIndexed(iterable.stream(), mapper)
    }

    @JvmStatic
    fun <T> stream(element: T): Stream<T> {
        return _Lists.list(element).stream()
    }
}

fun <T> Stream<T>.toSet(): Set<T> = this.collect(_Collectors.toSet())
