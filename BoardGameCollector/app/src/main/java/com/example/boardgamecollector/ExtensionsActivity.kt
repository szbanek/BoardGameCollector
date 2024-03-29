package com.example.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class Extension {
    var row: Int = 0
    var id: Long = 0
    var gameName: String? = null
    var originalName: String? = null
    var thumbnail: String? = null
    var year: Int = 0

    constructor(
        id: Long,
        gameName: String,
        originalName: String,
        thumbnail: String,
        year: Int
    ) {
        this.id = id
        this.gameName = gameName
        this.originalName = originalName
        this.thumbnail = thumbnail
        this.year = year
    }

    constructor(
        row: Int,
        id: Long,
        gameName: String,
        originalName: String,
        thumbnail: String,
        year: Int
    ) {
        this.row = row
        this.id = id
        this.gameName = gameName
        this.originalName = originalName
        this.thumbnail = thumbnail
        this.year = year
    }


}

class ExtensionsDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "extensionDB.db"
        val TABLE_EXTENSION = "extension"
        val COLUMN_ROW = "row"
        val COLUMN_ID = "id"
        val COLUMN_EXTENSION_NAME = "extensionname"
        val COLUMN_ORIGINALNAME = "originalname"
        val COLUMN_THUMBNAIL = "thumbnail"
        val COLUMN_YEAR = "year"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAMES_TABLE = ("CREATE TABLE " + TABLE_EXTENSION + "(" +
                COLUMN_ROW + " INTEGER PRIMARY KEY," +
                COLUMN_ID + " INTEGER," +
                COLUMN_EXTENSION_NAME + " TEXT," +
                COLUMN_ORIGINALNAME + " TEXT," +
                COLUMN_THUMBNAIL + " TEXT," +
                COLUMN_YEAR + " INTEGER, UNIQUE($COLUMN_ID))")
        db.execSQL(CREATE_GAMES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXTENSION)
        onCreate(db)
    }

    fun addExtension(extension: Extension){
        val values = ContentValues()
        values.put(COLUMN_ID, extension.id)
        values.put(COLUMN_EXTENSION_NAME, extension.gameName)
        values.put(COLUMN_ORIGINALNAME, extension.originalName)
        values.put(COLUMN_THUMBNAIL, extension.thumbnail)
        values.put(COLUMN_YEAR, extension.year)
        val db = this.writableDatabase
        db.insert(TABLE_EXTENSION, null, values)
        db.close()
    }

    fun getExtensions(sortBy: String, reverse: Boolean): MutableList<Extension> {
        var COL_NAME: String
        var extensions:MutableList<Extension> = mutableListOf()
        if(sortBy == "gameName"){
            COL_NAME = COLUMN_ORIGINALNAME
        }
        else if(sortBy == "year"){
            COL_NAME = COLUMN_YEAR
        }
        else return extensions
        if(reverse){
            COL_NAME = COL_NAME + " DESC"
        }
        val query = "SELECT * FROM $TABLE_EXTENSION ORDER BY $COL_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()){
            val row = Integer.parseInt(cursor.getString(0))
            val id = cursor.getString(1).toLong()
            val extensionName = cursor.getString(2)
            val originalName = cursor.getString(3)
            val thumbnail = cursor.getString(4)
            val year = Integer.parseInt(cursor.getString(5))
            extensions.add(Extension(row, id, extensionName, originalName, thumbnail, year))
        }
        return extensions
    }

    fun getExtensionsNumber(): Int {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EXTENSION, null)
        cursor.moveToFirst()
        return Integer.parseInt(cursor.getString(0))
    }
}

class ExtensionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extensions)
        updateList("gameName")
    }

    val table: TableLayout by lazy {findViewById(R.id.main_table)}
    var currentTable:MutableList<TableRow> = mutableListOf()
    var currentSort: String = ""
    val param2 = TableRow.LayoutParams(
        0,
        LinearLayout.LayoutParams.MATCH_PARENT,
        2.0f
    )
    val param3 = TableRow.LayoutParams(
        0,
        LinearLayout.LayoutParams.MATCH_PARENT,
        3.0f
    )
    val param5 = TableRow.LayoutParams(
        0,
        LinearLayout.LayoutParams.MATCH_PARENT,
        5.0f
    )

    private fun updateList(type: String){
        if(!(type=="gameName"||type=="year")){
            return
        }
        val dbHandler = ExtensionsDBHandler(this, null, null, 1)
        val games:MutableList<Extension>?
        var tmp = type
        var reverse = false
        if(currentSort==type) {
            reverse = true
            tmp = "-" + type
        }
        currentSort = tmp
        for(row in currentTable){
            table.removeView(row)
        }
        currentTable = mutableListOf()
        games = dbHandler.getExtensions(type, reverse)
        for(extension in games){
            addRow(extension)
        }
    }

    private fun addRow(extension: Extension){
        val tbrow = TableRow(this)
        val textNumber = TextView(this)
        textNumber.text = (currentTable.size+1).toString()
        textNumber.setTextColor(Color.WHITE)
        textNumber.gravity = Gravity.CENTER
        textNumber.textSize = 8f
        textNumber.layoutParams = param2
        tbrow.addView(textNumber)

        val textThumbnail = ImageView(this)
        DownloadImageFromInternet(textThumbnail).execute(extension.thumbnail)
        textThumbnail.layoutParams = param3
        tbrow.addView(textThumbnail)

        val textName = TextView(this)
        textName.text = extension.originalName
        textName.setTextColor(Color.WHITE)
        textName.gravity = Gravity.CENTER
        textName.textSize = 8f
        textName.layoutParams = param5
        tbrow.addView(textName)

        val textYear = TextView(this)
        textYear.text = extension.year.toString()
        textYear.setTextColor(Color.WHITE)
        textYear.gravity = Gravity.CENTER
        textYear.textSize = 8f
        textYear.layoutParams = param2
        tbrow.addView(textYear)

        currentTable.add(tbrow)
        table.addView(tbrow)
    }

    fun namesClick(v: View){
        updateList("gameName")
    }

    fun yearClick(v: View){
        updateList("year")
    }
}