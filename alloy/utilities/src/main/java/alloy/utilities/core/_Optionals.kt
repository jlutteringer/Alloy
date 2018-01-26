package alloy.utilities.core

import java.util.*
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * Created by jlutteringer on 1/16/18.
 */

object _Optionals {
    @JvmStatic
    fun <T> firstSome(optionals: Collection<Optional<T>>): Optional<T> = firstSome(optionals.stream())

    @JvmStatic
    fun <T> firstSome(optionals: Stream<Optional<T>>): Optional<T> = optionals.filter({ it.isPresent }).map<T>({ it.get() }).findFirst()

    @JvmStatic
    fun <T> lift(operation: Supplier<T?>): Supplier<Optional<T>> = Supplier { operation.get().toOptional() }
}

fun <T> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

fun <T> Optional<T>.orNull(): T? = orElse(null)