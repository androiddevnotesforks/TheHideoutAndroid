package com.austinhodak.tarkovapi.room.dao

import androidx.room.*
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE id = :id")
    fun getByID(id: String): Flow<Item>

    @Query("SELECT * FROM items WHERE id IN (:ids)")
    suspend fun getByID(ids: List<String>): List<Item>

    @Transaction
    @Query("SELECT * FROM items WHERE itemType = :type")
    fun getByType(type: ItemTypes): Flow<List<Item>>

    @Transaction
    @Query("SELECT * FROM items WHERE itemType in (:type)")
    suspend fun getByTypes(type: List<ItemTypes>): List<Item>

    @Transaction
    @Query("SELECT * FROM items WHERE itemType in (:type)")
    fun getByTypesArmor(type: List<ItemTypes>): Flow<List<Item>>

    @Transaction
    @Query("SELECT id, itemType, parent, Name, ShortName, pricing, Width, Height, BackgroundColor FROM items WHERE pricing IS NOT NULL")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Ammo)

    @Query("UPDATE items SET pricing = :tt WHERE id = :id")
    suspend fun updateItemsTable(id: String, tt: Pricing)

    @Query("UPDATE ammo SET pricing = :tt WHERE id = :id")
    suspend fun updateAmmoTable(id: String, tt: Pricing)

    @Query("UPDATE weapons SET pricing = :tt WHERE id = :id")
    suspend fun updateWeaponTable(id: String, tt: Pricing)

    @Transaction
    suspend fun updateAllPricing(id: String?, pricing: Pricing) {
        if (id != null) {
            updateItemsTable(id, pricing)
            updateAmmoTable(id, pricing)
            updateWeaponTable(id, pricing)
        }
    }

}