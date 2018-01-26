package alloy.utilities.core

import com.fasterxml.jackson.databind.util.ClassUtil
import org.springframework.util.ClassUtils
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by jlutteringer on 1/17/18.
 */
object _Reflection {
    fun findMatchingTopLevelType(type: KClass<*>, topLevelTypes: Set<KClass<*>>): KClass<*> {
        val result = topLevelTypes.stream()
                .map { topLevelType ->
                    val javaAncestor = ClassUtils.determineCommonAncestor(type.java, topLevelType.java)
                    javaAncestor?.kotlin
                }
                .filter { ancestor ->
                    topLevelTypes.contains(ancestor) && ancestor != Any::class
                }
                .map { ancestor -> ancestor!! }
                .findFirst()

        return result.orElse(Any::class)
    }
}