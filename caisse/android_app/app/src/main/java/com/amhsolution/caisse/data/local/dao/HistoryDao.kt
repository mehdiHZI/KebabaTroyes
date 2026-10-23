package com.amhsolution.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amhsolution.caisse.data.local.entity.CashClosureEntity
import com.amhsolution.caisse.data.local.entity.HistoryEntryEntity
import com.amhsolution.caisse.data.local.entity.TicketCounterEntity
import com.amhsolution.caisse.domain.history.nextDailyTicketNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_entries ORDER BY paidAtEpochMillis DESC")
    fun observeAll(): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HistoryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: HistoryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: HistoryEntryEntity): Long

    @Query("UPDATE history_entries SET paymentsJson = :paymentsJson WHERE id = :id")
    suspend fun updatePaymentsJson(id: String, paymentsJson: String)
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(closure: CashClosureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(closure: CashClosureEntity)

    @Query("DELETE FROM cash_closures WHERE businessDay = :businessDay AND type = :type")
    suspend fun deleteForBusinessDay(businessDay: String, type: String)
}
