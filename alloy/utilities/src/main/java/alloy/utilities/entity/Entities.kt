package alloy.utilities.entity

import alloy.utilities.core._Functions
import alloy.utilities.core._Reflection
import alloy.utilities.core.toOptional
import alloy.utilities.core.toSet
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * Created by jlutteringer on 1/17/18.
 */

data class Entity(
        val id: String,
        val name: String,
        val fields : Set<EntityField>,
        val metadata: Map<String, Any> = mapOf()): ConcreteType {

    fun getField(name : String) : EntityField {
        return requireNotNull(fields.find { it.name == name }, { "Couldn't find field by name: $name" })
    }
}

data class EntityField(
        val name: String,
        val type: EntityType,
        val metadata: Map<String, Any> = mapOf()) {

}

interface EntityType
interface ConcreteType: EntityType

data class SimpleType(val name: String): ConcreteType

data class ParameterizedType(val root: ConcreteType, val typeParameters: List<EntityType>): EntityType

typealias TypeResolver = (KType) -> Optional<EntityType>
typealias TypeAlias = (EntityType) -> EntityType

data class EntityReaderConfiguration(val typeAlias: TypeAlias = _Functions::identity)

class EntityReader(val configuration: EntityReaderConfiguration = Entities.DEFAULT_ENTITY_READER_CONFIGURATION) {

    val typeResolvers: MutableList<TypeResolver> = mutableListOf()

    init {
        typeResolvers.add(this.buildPrimitiveTypeResolver())
        typeResolvers.add(this.buildObjectTypeResolver())
    }

    fun describe(type: Class<*>) = describe(type.kotlin)

    fun describe(type: KClass<*>): Entity {
        val id = type.qualifiedName!!
        val name = type.simpleName!!
        val fields = type.memberProperties.stream().map { this.mapProperty(it) }.toSet()

        return Entity(id, name, fields)
    }

    fun mapProperty(property: KProperty1<*, *>): EntityField {
        val name = property.name
        val type = this.mapType(property.returnType)

        return EntityField(name, type)
    }

    fun mapType(type: KType): EntityType {
        val result = typeResolvers.stream()
                .map { it.invoke(type) }
                .filter { it.isPresent }
                .map { it.get() }
                .findFirst()

        if(!result.isPresent) {
            throw RuntimeException()
        }

        return configuration.typeAlias.invoke(result.get())
    }

    internal fun buildPrimitiveTypeResolver(): TypeResolver {
        val typeResolverMap: Map<KClass<*>, (KType) -> EntityType> = mapOf(
                Any::class to { _ -> EntityTypes.Any },
                String::class to { _ -> EntityTypes.String },
                Long::class to { _ -> EntityTypes.Long },
                BigDecimal::class to { _ -> EntityTypes.Decimal },
                Instant::class to { _ -> EntityTypes.Instant },
                Double::class to { _ -> EntityTypes.Double },
                Map::class to { type ->
                    if (type.arguments.isEmpty()) {
                        EntityTypes.Map
                    }
                    else {
                        EntityTypes.Map(keyType = this.mapType(type.arguments[0].type!!), valueType = this.mapType(type.arguments[1].type!!))
                    }
                },
                List::class to { type ->
                    if (type.arguments.isEmpty()) {
                        EntityTypes.List
                    }
                    else {
                        EntityTypes.List(type = this.mapType(type.arguments[0].type!!))
                    }
                },
                Set::class to { type ->
                    if (type.arguments.isEmpty()) {
                        EntityTypes.Set
                    }
                    else {
                        EntityTypes.Set(type = this.mapType(type.arguments[0].type!!))
                    }
                }
        )

        val topLevelTypes = typeResolverMap.keys

        return typeResolver@{ type ->
            if(type.jvmErasure == Any::class) {
                return@typeResolver typeResolverMap[Any::class]!!.invoke(type).toOptional()
            }

            val matchingType = _Reflection.findMatchingTopLevelType(type.jvmErasure, topLevelTypes)
            if(matchingType != Any::class) {
                return@typeResolver typeResolverMap[matchingType]!!.invoke(type).toOptional()
            }

            Optional.empty()
        }
    }

    internal fun buildObjectTypeResolver(): TypeResolver = { type -> Optional.of(this.describe(type.jvmErasure)) }
}

object Entities {
    val DEFAULT_ENTITY_READER_CONFIGURATION = EntityReaderConfiguration()

    val SIMPLE_TYPE_ALIAS = aliasTypes(mapOf(
            EntityTypes.Long to EntityTypes.Number,
            EntityTypes.Decimal to EntityTypes.Number,
            EntityTypes.Double to EntityTypes.Number,
            EntityTypes.Instant to EntityTypes.Date,
            EntityTypes.Set to EntityTypes.List
    ))

    val DEFAULT_ENTITY_READER = EntityReader()
    val SIMPLE_ENTITY_READER = EntityReader(configuration = EntityReaderConfiguration(SIMPLE_TYPE_ALIAS))

    fun aliasTypes(map: Map<ConcreteType, ConcreteType>): TypeAlias {
        val typeAlias: TypeAlias = { type ->
            if(type is ConcreteType && map.containsKey(type)) {
                map[type]!!
            }
            else if(type is ParameterizedType && map.containsKey(type.root)) {
                val conversionType = map[type.root]!!
                type.copy(root = conversionType)
            }
            else {
                type
            }
        }

        return typeAlias
    }
}

object EntityTypes {
    val Any = SimpleType("Any")
    val String = SimpleType("String")
    val Number = SimpleType("Number")
    val Long = SimpleType("Long")
    val Decimal = SimpleType("Decimal")
    val Date = SimpleType("Date")
    val Instant = SimpleType("Instant")
    val Double = SimpleType("Double")
    val Map = SimpleType("Map")
    val List = SimpleType("List")
    val Set = SimpleType("Set")

    fun Map(keyType: EntityType, valueType: EntityType): EntityType = ParameterizedType(EntityTypes.Map, listOf(keyType, valueType))
    fun List(type: EntityType): EntityType = ParameterizedType(EntityTypes.List, listOf(type))
    fun Set(type: EntityType): EntityType = ParameterizedType(EntityTypes.Set, listOf(type))
}