package alloy.utilities.wright

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Created by jlutteringer on 1/19/18.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table (
        val name: String
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(
        val name: String
)

annotation class JoinColumn(
        val name: String = "default",
        val joinType: KClass<*> = Any::class
)

annotation class OneToMany(
        val mappedBy: String
)