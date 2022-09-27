package com.example.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep

@Suppress("DEPRECATION")
class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
    override fun doInBackground(vararg urls: String): Bitmap? {
        val imageURL = urls[0]
        var image: Bitmap? = null
        try {
            val `in` = java.net.URL(imageURL).openStream()
            image = BitmapFactory.decodeStream(`in`)
        }
        catch (e: Exception) {
            Log.e("Error Message", e.message.toString())
            e.printStackTrace()
        }
        return image
    }
    override fun onPostExecute(result: Bitmap?) {
        imageView.setImageBitmap(result)
    }
}

class Game {
    var row:Int = 0
    var id:Long = 0
    var gameName: String? = null
    var originalName: String? = null
    var thumbnail: String? = null
    var year: Int = 0
    var rank: Int = 0

    constructor(id:Long, gameName: String, originalName: String, thumbnail: String, year:Int, rank: Int){
        this.id = id
        this.gameName = gameName
        this.originalName = originalName
        this.thumbnail = thumbnail
        this.year = year
        this.rank = rank
    }

    constructor(row:Int, id:Long, gameName: String, originalName: String, thumbnail: String, year:Int, rank: Int){
        this.row = row
        this.id = id
        this.gameName = gameName
        this.originalName = originalName
        this.thumbnail = thumbnail
        this.year = year
        this.rank = rank
    }

}

class GameDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
        companion object{
            private val DATABASE_VERSION = 1
            private val DATABASE_NAME = "gamesDB.db"
            val TABLE_GAMES = "games"
            val COLUMN_ROW = "row"
            val COLUMN_ID = "id"
            val COLUMN_GAMENAME = "gamename"
            val COLUMN_ORIGINALNAME = "originalname"
            val COLUMN_THUMBNAIL = "thumbnail"
            val COLUMN_YEAR = "year"
            val COLUMN_RANK = "rank"
            val COLUMN_DATE = "date"
        }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAMES_TABLE = ("CREATE TABLE " + TABLE_GAMES + "(" +
                COLUMN_ROW + " INTEGER PRIMARY KEY," +
                COLUMN_ID + " INTEGER," +
                COLUMN_GAMENAME + " TEXT," +
                COLUMN_ORIGINALNAME + " TEXT," +
                COLUMN_THUMBNAIL + " TEXT," +
                COLUMN_YEAR + " INTEGER," +
                COLUMN_RANK + " INTEGER, UNIQUE($COLUMN_ID))")
        db.execSQL(CREATE_GAMES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES)
        onCreate(db)
    }

    fun addGame(game: Game, date: String){
        val values = ContentValues()
        values.put(COLUMN_ID, game.id)
        values.put(COLUMN_GAMENAME, game.gameName)
        values.put(COLUMN_ORIGINALNAME, game.originalName)
        values.put(COLUMN_THUMBNAIL, game.thumbnail)
        values.put(COLUMN_YEAR, game.year)
        values.put(COLUMN_RANK, game.rank)
        val db = this.writableDatabase
        db.insert(TABLE_GAMES, null, values)
        updateRankingList(game, date)
    }

    fun getGames(sortBy: String, reverse: Boolean): MutableList<Game> {
        var COL_NAME: String
        var games:MutableList<Game> = mutableListOf()
        if(sortBy == "gameName"){
            COL_NAME = COLUMN_ORIGINALNAME
        }
        else if(sortBy == "rank"){
            COL_NAME = COLUMN_RANK
        }
        else if(sortBy == "year"){
            COL_NAME = COLUMN_YEAR
        }
        else return games
        if(reverse){
            COL_NAME = COL_NAME + " DESC"
        }
        val query = "SELECT * FROM $TABLE_GAMES ORDER BY $COL_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()){
            val row = Integer.parseInt(cursor.getString(0))
            val id = cursor.getString(1).toLong()
            val gameName = cursor.getString(2)
            val originalName = cursor.getString(3)
            val thumbnail = cursor.getString(4)
            val year = Integer.parseInt(cursor.getString(5))
            val rank = Integer.parseInt(cursor.getString(6))
            games.add(Game(row, id, gameName,originalName, thumbnail, year, rank))
        }
        return games
    }

    fun findGame(id: Long): Game {
        val query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ID=$id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val row = Integer.parseInt(cursor.getString(0))
        val id = cursor.getString(1).toLong()
        val gameName = cursor.getString(2)
        val originalName = cursor.getString(3)
        val thumbnail = cursor.getString(4)
        val year = Integer.parseInt(cursor.getString(5))
        val rank = Integer.parseInt(cursor.getString(6))
        return Game(row, id, gameName,originalName, thumbnail, year, rank)
    }

    fun updateRankingList(game: Game, date: String){
        val db = this.writableDatabase
        val tmpId = "game" + game.id.toString()
        val CREATE_GAMES_TABLE = ("CREATE TABLE IF NOT EXISTS $tmpId(" +
                COLUMN_ROW + " INTEGER PRIMARY KEY, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_RANK + " INTEGER " + ")")
        db.execSQL(CREATE_GAMES_TABLE)
        val values = ContentValues()
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_RANK, game.rank)
        db.insert(tmpId, null, values)
    }

    fun getRankingList(id: Long): MutableList<Ranking> {
        var rankings:MutableList<Ranking> = mutableListOf()
        val db = this.writableDatabase
        val query = "SELECT * FROM game$id ORDER BY $COLUMN_DATE"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()){
            val row = Integer.parseInt(cursor.getString(0))
            val date = cursor.getString(1)
            val rank = Integer.parseInt(cursor.getString(2))
            rankings.add(Ranking(row, rank, date))
        }
        return rankings
    }

    fun getGamesNumber(): Int {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GAMES, null)
        cursor.moveToFirst()
        return Integer.parseInt(cursor.getString(0))
    }
}

class GamesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
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
        if(!(type=="gameName"||type=="year"||type=="rank")){
            return
        }
        val dbHandler = GameDBHandler(this, null, null, 1)
        val games:MutableList<Game>?
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
        games = dbHandler.getGames(type, reverse)
        for(game in games){
            addRow(game)
        }
    }

    private fun addRow(game: Game){
        val tbrow = TableRow(this)
        val textNumber = TextView(this)
        textNumber.text = (currentTable.size+1).toString()
        textNumber.gravity = Gravity.CENTER
        textNumber.textSize = 8f
        textNumber.layoutParams = param2
        tbrow.addView(textNumber)

        val textThumbnail = ImageView(this)
        DownloadImageFromInternet(textThumbnail).execute(game.thumbnail)
        textThumbnail.layoutParams = param3
        tbrow.addView(textThumbnail)

        val textName = TextView(this)
        textName.text = game.originalName
        textName.gravity = Gravity.CENTER
        textName.textSize = 8f
        textName.layoutParams = param5
        tbrow.addView(textName)

        val textYear = TextView(this)
        textYear.text = game.year.toString()
        textYear.gravity = Gravity.CENTER
        textYear.textSize = 8f
        textYear.layoutParams = param2
        tbrow.addView(textYear)

        val textRank = TextView(this)
        textRank.text = game.rank.toString()
        textRank.gravity = Gravity.CENTER
        textRank.textSize = 8f
        textRank.layoutParams = param2
        tbrow.addView(textRank)

        tbrow.setOnClickListener{
            val intent = Intent(this, RankActivity::class.java)
            intent.putExtra("id", game.id.toString())
            startActivity(intent)
        }
        currentTable.add(tbrow)
        table.addView(tbrow)
    }

    fun namesClick(v: View){
        updateList("gameName")
    }

    fun yearClick(v: View){
        updateList("year")
    }

    fun rankClick(v: View){
        updateList("rank")
    }
}