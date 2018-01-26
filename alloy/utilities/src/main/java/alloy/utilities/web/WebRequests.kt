package alloy.utilities.web

import alloy.utilities.core.TypeConverters
import alloy.utilities.core._Optionals
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * Created by jlutteringer on 1/16/18.
 */

interface WebRequestAttribute<T> {
    fun read(request: HttpServletRequest): Optional<T> {
        return this.read(ServletWebRequest(request))
    }

    fun read(request: ServletWebRequest): Optional<T>
}

interface MutableWebRequestAttribute<T>: WebRequestAttribute<T> {
    fun setValue(request: HttpServletRequest, value: T?) {
        this.setValue(ServletWebRequest(request), value)
    }

    fun setValue(request: ServletWebRequest, value: T?)
}

object WebRequests {
    fun <T> chain(vararg attributes: WebRequestAttribute<T>): WebRequestAttribute<T> = chain(attributes.asList())

    fun <T> chain(attributes: List<WebRequestAttribute<T>>): WebRequestAttribute<T> =
            object: WebRequestAttribute<T> {
                override fun read(request: ServletWebRequest): Optional<T> = _Optionals.firstSome(attributes.map { attribute -> attribute.read(request) })
            }

    fun <T> headerAccessor(name: String, type: Class<T>): WebRequestAttribute<T> =
            object: WebRequestAttribute<T> {
                override fun read(request: ServletWebRequest): Optional<T> =
                        Optional.ofNullable(request.getHeader(name)).flatMap { a -> TypeConverters.convertOptional(a, type) }
            }

    fun <T> cookieAccessor(name: String, type: Class<T>): WebRequestAttribute<T> =
            object: WebRequestAttribute<T> {
                override fun read(request: ServletWebRequest): Optional<T> =
                        Cookies.read(name, type, request)
            }
}