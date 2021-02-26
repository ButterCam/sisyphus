package com.bybutter.sisyphus.middleware.jdbc.test

import com.bybutter.sisyphus.middleware.jdbc.support.proto.filter.JooqConditionBuilder
import org.jooq.Field
import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JooqFilterEngineTest {
    @Test
    fun `test jooq filter eval`() {
        val testBuilder = TestJooqConditionBuilder()
        val expect = """(
  member1 = '123'
  and (
    member2 like '4%'
    or member3 is not null
  )
  and member4 < 456
)"""
        Assertions.assertEquals(
            expect,
            testBuilder.build("member1 = '123' AND member2:'4*' OR member3 != null AND member4 < 456").toString()
        )
    }
}

class TestJooqConditionBuilder : JooqConditionBuilder() {

    override fun resolveMember(member: String): Field<*>? {
        return when (member) {
            "member1" -> DSL.field("member1")
            "member2" -> DSL.field("member2")
            "member3" -> DSL.field("member3")
            "member4" -> DSL.field("member4")
            else -> null
        }
    }
}
