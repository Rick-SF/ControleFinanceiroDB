package com.projeto.controlefinanceirodb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

class BancoLite(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "financeiro.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_GASTOS = "gastos"
        const val COLUMN_ID = "id"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_DESCRICAO = "descricao"
        const val COLUMN_VALOR = "valor"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = ("CREATE TABLE $TABLE_GASTOS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TIPO TEXT,"
                + "$COLUMN_DESCRICAO TEXT,"
                + "$COLUMN_VALOR REAL)")
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GASTOS")
        onCreate(db)
    }

    fun adicionarGastos (tipo: String, descricao: String, valor: Double){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIPO, tipo)
            put(COLUMN_DESCRICAO, descricao)
            put(COLUMN_VALOR, valor)
        }
        db.insert(TABLE_GASTOS, null, values)
        db.close()
    }

    fun buscarGastos(): List<String> {
        val lista = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GASTOS", null)

        if (cursor.moveToFirst()){
            do {
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO))
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_VALOR))
                val item = "$tipo: $descricao - R$%.2f".format(valor)
                lista.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }


}