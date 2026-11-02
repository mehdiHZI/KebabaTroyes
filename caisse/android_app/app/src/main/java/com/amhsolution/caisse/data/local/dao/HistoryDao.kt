package com.amhsolution.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amhsolution.caisse.data.local.entity.CashClosureEntity
import com.amhsolution.caisse.data.local.entity.FiscalArchiveEntity
import com.amhsolution.caisse.data.local.entity.HistoryEntryEntity
import com.amhsolution.caisse.data.local.entity.PerpetualTotalsEntity
import com.amhsolution.caisse.data.local.entity.TicketCounterEntity
import com.amhsolution.caisse.domain.history.nextDailyTicketNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_entries ORDER BY paidAtEpochMillis DESC")
    fun observeAll(): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries ORDER BY paidAtEpochMillis ASC")
    suspend fun getAllAscending(): List<HistoryEntryEntity>

    @Query("SELECT * FROM history_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HistoryEntryEntity?

    @Query(
        """
        SELECT * FROM history_entries
        WHERE integrityHash IS NOT NULL AND integrityHash != ''
        ORDER BY paidAtEpochMillis DESC, ticketNumber DESC
        LIMIT 1
        """
    )
    suspend fun getLatestChained(): HistoryEntryEntity?

    @Query("SELECT COUNT(*) FROM history_entries WHERE linkedTicketId = :ticketId")
    suspend fun countCorrectionsFor(ticketId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: HistoryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: HistoryEntryEntity): Long
}

@Dao
interface TicketCounterDao {
    @Query("SELECT * FROM ticket_counter WHERE businessDay = :businessDay LIMIT 1")
    suspend fun getForDay(businessDay: String): TicketCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(counter: TicketCounterEntity)

    @Transaction
    suspend fun allocateTicketNumber(businessDay: String): Int {
        val existing = getForDay(businessDay)
        val nextNumber = nextDailyTicketNumber(
            businessDay = businessDay,
            counterDay = existing?.businessDay,
            lastIssuedNumber = existing?.lastIssuedNumber
        )
        upsert(TicketCounterEntity(businessDay = businessDay, lastIssuedNumber = nextNumber))
        return nextNumber
    }
}

@Dao
interface CashClosureDao {
    @Query("SELECT * FROM cash_closures ORDER BY closedAtEpochMillis DESC")
    fun observeAll(): Flow<List<CashClosureEntity>>

    @Query("SELECT * FROM cash_closures ORDER BY closedAtEpochMillis DESC")
    suspend fun getAll(): List<CashClosureEntity>

    @Query(
        """
        SELECT * FROM cash_closures
        WHERE businessDay = :businessDay AND type = :type
        LIMIT 1
        """
    )
    suspend fun getForBusinessDay(businessDay: String, type: String): CashClosureEntity?

    @Query("SELECT * FROM cash_closures ORDER BY closedAtEpochMillis DESC LIMIT 1")
    suspend fun getLatest(): CashClosureEntity?

    @Query(
        """
        SELECT * FROM cash_closures
        WHERE type = :type AND isFinal = 1
        ORDER BY closedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestFinalOfType(type: String): CashClosureEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(closure: CashClosureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(closure: CashClosureEntity)

    @Query("DELETE FROM cash_closures WHERE businessDay = :businessDay AND type = :type AND isFinal = 0")
    suspend fun deleteProvisionalForBusinessDay(businessDay: String, type: String)
}

@Dao
interface PerpetualTotalsDao {
    @Query("SELECT * FROM perpetual_totals WHERE id = 'default' LIMIT 1")
    fun observe(): Flow<PerpetualTotalsEntity?>

    @Query("SELECT * FROM perpetual_totals WHERE id = 'default' LIMIT 1")
    suspend fun get(): PerpetualTotalsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PerpetualTotalsEntity)
}

@Dao
interface FiscalArchiveDao {
    @Query("SELECT * FROM fiscal_archives ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<FiscalArchiveEntity>>

    @Query("SELECT * FROM fiscal_archives ORDER BY createdAtEpochMillis DESC")
    suspend fun getAll(): List<FiscalArchiveEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: FiscalArchiveEntity)
}
