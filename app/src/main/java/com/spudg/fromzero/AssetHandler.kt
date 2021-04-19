package com.spudg.fromzero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AssetHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FZAssets.db"
        private const val TABLE_ASSETS = "assets"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_VALUE = "value"
        private const val KEY_NOTE = "note"
        private const val KEY_COLOUR = "colour"
        private const val KEY_DATE = "date"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTransactionsTable =
                ("CREATE TABLE $TABLE_ASSETS($KEY_ID INTEGER PRIMARY KEY,$KEY_NAME TEXT,$KEY_VALUE TEXT,$KEY_NOTE TEXT,$KEY_COLOUR TEXT,$KEY_DATE TEXT)")
        db?.execSQL(createTransactionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ASSETS")
        onCreate(db)
    }

    fun addAsset(asset: AssetModel): Long {
        val values = ContentValues()
        values.put(KEY_NAME, asset.name)
        values.put(KEY_VALUE, asset.value)
        values.put(KEY_NOTE, asset.note)
        values.put(KEY_COLOUR, asset.colour)
        values.put(KEY_DATE, asset.date)
        val db = this.writableDatabase
        val success = db.insert(TABLE_ASSETS, null, values)
        db.close()
        return success
    }

    fun updateAsset(asset: AssetModel): Int {
        val values = ContentValues()
        values.put(KEY_NAME, asset.name)
        values.put(KEY_VALUE, asset.value)
        values.put(KEY_NOTE, asset.note)
        values.put(KEY_COLOUR, asset.colour)
        values.put(KEY_DATE, asset.date)
        val db = this.writableDatabase
        val success = db.update(TABLE_ASSETS, values, KEY_ID + "=" + asset.id, null)
        db.close()
        return success
    }

    fun deleteAsset(asset: AssetModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_ASSETS, KEY_ID + "=" + asset.id, null)
        db.close()
        return success
    }

    fun getAllAssets(): ArrayList<AssetModel> {
        val list = ArrayList<AssetModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ASSETS", null)

        var id: Int
        var name: String
        var value: String
        var note: String
        var colour: String
        var date: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                val asset = AssetModel(
                        id = id,
                        name = name,
                        value = value,
                        note = note,
                        colour = colour,
                        date = date
                )
                list.add(asset)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list

    }

}
