package com.amhsolution.caisse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amhsolution.caisse.data.local.dao.CashClosureDao
import com.amhsolution.caisse.data.local.dao.HistoryDao
import com.amhsolution.caisse.data.local.dao.SyncOutboxDao
import com.amhsolution.caisse.data.local.dao.TicketCounterDao
import com.amhsolution.caisse.data.local.entity.CashClosureEntity
import com.amhsolution.caisse.data.local.entity.HistoryEntryEntity
import com.amhsolution.caisse.data.local.entity.SyncOutboxEntity
import com.amhsolution.caisse.data.local.entity.TicketCounterEntity

@Database(
    entities = [
        HistoryEntryEntity::class,
        TicketCounterEntity::class,
        CashClosureEntity::class,
        SyncOutboxEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class CaisseDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun ticketCounterDao(): TicketCounterDao
    abstract fun cashClosureDao(): CashClosureDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        @Volatile
        private var instance: CaisseDatabase? = null

        fun getInstance(context: Context): CaisseDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CaisseDatabase::class.java,
                    "caisse.db"
                )
                    .addMigrations(Migration1To2, Migration2To3, Migration3To4, Migration4To5)
                    .build()
                    .also { instance = it }
            }
    }
}

private val Migration4To5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sync_outbox` (
                `id` TEXT NOT NULL,
                `ticketId` TEXT NOT NULL,
                `payloadJson` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `attemptCount` INTEGER NOT NULL,
                `lastError` TEXT,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_sync_outbox_ticketId` ON `sync_outbox` (`ticketId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_sync_outbox_status` ON `sync_outbox` (`status`)"
        )
    }
}

/** Les clôtures déjà présentes avant la V4 sont traitées comme définitives (auto). */
private val Migration3To4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `cash_closures` ADD COLUMN `isFinal` INTEGER NOT NULL DEFAULT 1"
        )
    }
}

private val Migration2To3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cash_closures_v3` (
                `id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `businessDay` TEXT NOT NULL,
                `closedAtEpochMillis` INTEGER NOT NULL,
                `ticketCount` INTEGER NOT NULL,
                `firstTicketNumber` INTEGER,
                `lastTicketNumber` INTEGER,
                `totalRevenueCentimes` INTEGER NOT NULL,
                `itemCount` INTEGER NOT NULL,
                `averageBasketCentimes` INTEGER NOT NULL,
                `paymentTotalsJson` TEXT NOT NULL,
                `paymentCountsJson` TEXT NOT NULL,
                `orderTypeTotalsJson` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `cash_closures_v3` (
                `id`,
                `type`,
                `businessDay`,
                `closedAtEpochMillis`,
                `ticketCount`,
                `firstTicketNumber`,
                `lastTicketNumber`,
                `totalRevenueCentimes`,
                `itemCount`,
                `averageBasketCentimes`,
                `paymentTotalsJson`,
                `paymentCountsJson`,
                `orderTypeTotalsJson`
            )
            SELECT
                `id`,
                `type`,
                `businessDay`,
                `closedAtEpochMillis`,
                `ticketCount`,
                `firstTicketNumber`,
                `lastTicketNumber`,
                `totalRevenueCentimes`,
                0,
                CASE
                    WHEN `ticketCount` > 0 THEN `totalRevenueCentimes` / `ticketCount`
                    ELSE 0
                END,
                `paymentTotalsJson`,
                '{}',
                `orderTypeTotalsJson`
            FROM `cash_closures`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `cash_closures`")
        db.execSQL("ALTER TABLE `cash_closures_v3` RENAME TO `cash_closures`")
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_cash_closures_businessDay_type`
            ON `cash_closures` (`businessDay`, `type`)
            """.trimIndent()
        )
    }
}

private val Migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cash_closures` (
                `id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `businessDay` TEXT NOT NULL,
                `closedAtEpochMillis` INTEGER NOT NULL,
                `ticketCount` INTEGER NOT NULL,
                `firstTicketNumber` INTEGER,
                `lastTicketNumber` INTEGER,
                `totalRevenueCentimes` INTEGER NOT NULL,
                `paymentTotalsJson` TEXT NOT NULL,
                `orderTypeTotalsJson` TEXT NOT NULL,
                `previousClosureHash` TEXT,
                `closureHash` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_cash_closures_businessDay_type`
            ON `cash_closures` (`businessDay`, `type`)
            """.trimIndent()
        )
    }
}
