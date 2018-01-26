package alloy.utilities.web

import alloy.utilities.core.Json
import alloy.utilities.core.TypeConverters
import alloy.utilities.web.Cookies.DEFAULT_COOKIE_DEFINITION
import alloy.utilities.web.uri.UriLocation
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import mu.KLogging
import org.springframework.web.context.request.ServletWebRequest
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Function
import javax.servlet.http.Cookie

/**
 * Created by jlutteringer on 1/16/18.
 */

data class CookieDefinition(val age: Duration, val secure: Boolean)

data class CookieContext<T>(val name: String, val type: Class<T>, val definition: CookieDefinition): MutableWebRequestAttribute<T> {
    override fun read(request: ServletWebRequest): Optional<T> = Cookies.read(name, type, request)

    override fun setValue(request: ServletWebRequest, value: T?) = Cookies.write(name, value, definition, request)
}

fun <T> cookie(name: String, type: Class<T>, definition: CookieDefinition = DEFAULT_COOKIE_DEFINITION): WebRequestAttribute<T> = CookieContext(name, type, definition)

object Cookies: KLogging() {
    private val DEFINITION_REGISTRY = mutableMapOf<String, CookieDefinition>()

    val DEFAULT_COOKIE_DEFINITION = CookieDefinition(Duration.ofMinutes(30), true)
    val EMPTY_COOKIE_DEFINITION = CookieDefinition(Duration.ofSeconds(0), true)

    @JvmStatic
    fun <T> read(name: String, type: Class<T>, request: ServletWebRequest): Optional<T> {
        val cookies = request.request.cookies.toList()

        val passthrough = Function<T, Optional<T>> { Optional.of(it) }
        val exceptionHandler = Function<IllegalArgumentException, Optional<T>> { exception ->
            logger.warn(exception) { "Unable to parse cookie with name: $name" }
            remove(name, request)
            Optional.empty()
        }

        return cookies.stream()
                .filter { cookie -> cookie.name == name }
                .findFirst()
                .map { cookie -> TypeConverters.convert(cookie.value, type) }
                .flatMap { either -> either.either(passthrough, exceptionHandler) }
    }

    @JvmStatic
    fun write(name: String, value: Any?, definition: CookieDefinition = DEFINITION_REGISTRY.getOrDefault(name, DEFAULT_COOKIE_DEFINITION), request: ServletWebRequest) {
        if (request.response.isCommitted) {
            throw RuntimeException("Cannot set a cookie if the response is committed.")
        }

        val cookie = Cookie(name, Json.marshall(value))
        cookie.maxAge = java.lang.Long.valueOf(definition.age.get(ChronoUnit.SECONDS)).toInt()
        cookie.path = UriLocation.from(request.contextPath).toString()
        cookie.secure = definition.secure

        request.response.addCookie(cookie)
    }

    @JvmStatic
    fun remove(name: String, request: ServletWebRequest) = write(name, null, EMPTY_COOKIE_DEFINITION, request)
}