package alloy.test.utilities.entity

import alloy.utilities.core.Json
import alloy.utilities.entity.*
import alloy.utilities.wright.*
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.primitives.Primitives
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

/**
 * Created by jlutteringer on 1/17/18.
 */

class EntityTest {
    @Table(name = "PRODUCT")
    data class Product(
            @Id @Column(name = "PRODUCT_ID") val id: Long,
            @Column(name = "NAME") val name: String,
            @Column(name = "PRICE") val price: BigDecimal,
            @Column(name = "PERCENT_OFF") val percentOff: Double,
            @Column(name = "EXPIRATION") val expiration: Instant,
            @JoinColumn val category: Category,
            val attributes: Map<Set<String>, Category>
    )

    @Table(name = "PRODUCT_2")
    data class Product2(
            @Id @Column(name = "PRODUCT_ID") val id: Long,
            @Column(name = "NAME") val name: String,
            @Column(name = "PRICE") val price: BigDecimal,
            @Column(name = "PERCENT_OFF") val percentOff: Double,
            @Column(name = "EXPIRATION") val expiration: Instant,
            @JoinColumn(joinType = Category::class) val categoryId: Long,
            val attributes: Map<Set<String>, Category>
    )

    @Table(name = "CATEGORY")
    data class Category(
            @Id @Column(name = "CATEGORY_ID") val id: Long,
            @Column(name = "NAME") val name: String
    )

    @Test
    fun test() {
        run {
            val categoryEntity = Entity(
                    id = "alloy.test.utilities.entity.EntityTest.Category",
                    name = "Category",
                    fields = setOf(
                            EntityField(name = "name", type = EntityTypes.String)
                    ))

            val productEntity = Entity(
                    id = "alloy.test.utilities.entity.EntityTest.Product",
                    name = "Product",
                    fields = setOf(
                            EntityField(name = "id", type = EntityTypes.Long),
                            EntityField(name = "name", type = EntityTypes.String),
                            EntityField(name = "price", type = EntityTypes.Decimal),
                            EntityField(name = "percentOff", type = EntityTypes.Double),
                            EntityField(name = "expiration", type = EntityTypes.Instant),
                            EntityField(name = "category", type = categoryEntity),
                            EntityField(name = "attributes", type = EntityTypes.Map(EntityTypes.Set(EntityTypes.String), categoryEntity))
                    ))

            val reader = Entities.DEFAULT_ENTITY_READER
            val describedProductEntity = reader.describe(Product::class)

            Assert.assertEquals(productEntity, describedProductEntity)
        }

        run {
            val categoryEntity = Entity(
                    id = "alloy.test.utilities.entity.EntityTest.Category",
                    name = "Category",
                    fields = setOf(
                            EntityField(name = "name", type = EntityTypes.String)
                    ))

            val productEntity = Entity(
                    id = "alloy.test.utilities.entity.EntityTest.Product",
                    name = "Product",
                    fields = setOf(
                            EntityField(name = "id", type = EntityTypes.Number),
                            EntityField(name = "name", type = EntityTypes.String),
                            EntityField(name = "price", type = EntityTypes.Number),
                            EntityField(name = "percentOff", type = EntityTypes.Number),
                            EntityField(name = "expiration", type = EntityTypes.Date),
                            EntityField(name = "category", type = categoryEntity),
                            EntityField(name = "attributes", type = EntityTypes.Map(EntityTypes.List(EntityTypes.String), categoryEntity))
                    ))

            val reader = Entities.SIMPLE_ENTITY_READER
            val describedProductEntity = reader.describe(Product::class)

            Assert.assertEquals(productEntity, describedProductEntity)
        }
    }
}