package com.bybutter.sisyphus.middleware.jdbc.test

import com.bybutter.sisyphus.middleware.jdbc.transaction.nestTransaction
import com.bybutter.sisyphus.middleware.jdbc.transaction.newTransaction
import com.bybutter.sisyphus.middleware.jdbc.transaction.noTransaction
import com.bybutter.sisyphus.middleware.jdbc.transaction.transaction
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@SpringBootApplication
class TransactionTest {
    @Test
    fun `test sisyphus transaction`(@Jdbc.Test dsl: DSLContext): Unit = runBlocking {
        initializeTable(dsl)

        assertRollback {
            transaction {
                createUser(dsl, "foo")
                noTransaction {
                    assertUserNotExist(dsl, "foo")
                }
                assertUserExist(dsl, "foo")
                rollbackException()
            }
        }
        assertUserNotExist(dsl, "foo")
    }

    @Test
    fun `test sisyphus nest transaction`(@Jdbc.Test dsl: DSLContext): Unit = runBlocking {
        initializeTable(dsl)

        transaction {
            createUser(dsl, "foo")
            assertUserExist(dsl, "foo")

            assertRollback {
                nestTransaction {
                    createUser(dsl, "bar")
                    assertUserExist(dsl, "foo")
                    assertUserExist(dsl, "bar")
                    rollbackException()
                }
            }

            assertUserExist(dsl, "foo")
            assertUserNotExist(dsl, "bar")
        }
    }

    @Test
    fun `test sisyphus new transaction`(@Jdbc.Test dsl: DSLContext): Unit = runBlocking {
        initializeTable(dsl)

        transaction {
            createUser(dsl, "foo")
            assertUserExist(dsl, "foo")

            assertRollback {
                newTransaction {
                    createUser(dsl, "bar")
                    assertUserNotExist(dsl, "foo")
                    assertUserExist(dsl, "bar")
                    rollbackException()
                }
            }

            assertUserExist(dsl, "foo")
            assertUserNotExist(dsl, "bar")
        }
    }

    @Test
    fun `test sisyphus nest jooq transaction`(@Jdbc.Test dsl: DSLContext): Unit = runBlocking {
        initializeTable(dsl)

        transaction {
            createUser(dsl, "foo")
            assertUserExist(dsl, "foo")

            assertRollback {
                dsl.transaction { config ->
                    val dsl = config.dsl()
                    createUser(dsl, "bar")
                    assertUserExist(dsl, "foo")
                    assertUserExist(dsl, "bar")
                    rollbackException()
                }
            }

            assertUserExist(dsl, "foo")
            assertUserNotExist(dsl, "bar")
        }
    }

    private fun createUser(dsl: DSLContext, name: String) {
        dsl.insertInto(table("USER"))
                .set(field("NAME", String::class.java), name)
                .execute()
    }

    private fun assertUserExist(dsl: DSLContext, name: String) {
        Assertions.assertNotNull(getUser(dsl, name))
    }

    private fun assertUserNotExist(dsl: DSLContext, name: String) {
        Assertions.assertNull(getUser(dsl, name))
    }

    private fun getUser(dsl: DSLContext, name: String): Record? {
        return dsl.selectFrom(table("USER"))
                .where(field("NAME", String::class.java).eq(name))
                .fetchOne()
    }

    private fun rollbackException() {
        throw RuntimeException("Rollback")
    }

    private inline fun assertRollback(block: () -> Unit) {
        try {
            block()
        } catch (ex: RuntimeException) {
            assertThrows<RuntimeException> { throw ex }
        }
    }

    private fun initializeTable(dsl: DSLContext) {
        dsl.dropTableIfExists("USER").execute()
        dsl.createTable("USER")
                .column("ID", SQLDataType.INTEGER.identity(true))
                .column("NAME", SQLDataType.VARCHAR.length(64))
                .execute()
    }
}
