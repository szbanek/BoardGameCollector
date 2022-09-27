package com.example.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class User{
    var name: String = ""
    var gamesNumber: Int = 0
    var extensionNumber: Int = 0
    var lastSync: String = ""

    constructor(name: String, gamesNumber: Int, extensionNumber: Int, lastSync: String){
        this.name = name
        this.gamesNumber = gamesNumber
        this.extensionNumber = extensionNumber
        this.lastSync = lastSync
    }

    constructor(name: String, gamesNumber: Int, extensionNumber: Int){
        this.name = name
        this.gamesNumber = gamesNumber
        this.extensionNumber = extensionNumber
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        this.lastSync = sdf.format(Date())
    }
}

class UserDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "userDB.db"
        val TABLE_USER = "user"
        val COLUMN_NAME = "name"
        val COLUMN_GAMESNUMBER = "gamesNumber"
        val COLUMN_EXTENSION_NUMBER = "extensionNumber"
        val COLUMN_LASTSYNC = "lastSync"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "(" +
                COLUMN_NAME + " TEXT," +
                COLUMN_GAMESNUMBER + " INTEGER," +
                COLUMN_EXTENSION_NUMBER + " INTEGER," +
                COLUMN_LASTSYNC + " TEXT" + ")")
        db.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER)
        onCreate(db)
    }

    fun delUser(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_USER)
    }

    fun getUser(): User? {
        val query = "SELECT * FROM $TABLE_USER"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var user: User? = null
        if(cursor.moveToLast()){
            val name = cursor.getString(0)
            val gamesNumber = Integer.parseInt(cursor.getString(1))
            val extensionNumber = Integer.parseInt(cursor.getString(2))
            val lastSync = cursor.getString(3)
            user = User(name, gamesNumber, extensionNumber, lastSync)
        }
        return user
    }

    fun newUser(user:User){
        val values = ContentValues()
        values.put(COLUMN_NAME, user.name)
        values.put(COLUMN_GAMESNUMBER, user.gamesNumber)
        values.put(COLUMN_EXTENSION_NUMBER, user.extensionNumber)
        values.put(COLUMN_LASTSYNC, user.lastSync)
        val db = this.writableDatabase
        db.insert(TABLE_USER, null, values)
    }

}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        login()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        refresh()
    }

    private fun login(){
        val dbHandler = UserDBHandler(this, null, null, 1)
        if(dbHandler.getUser()==null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
        }
        else{
            refresh()
        }
    }

    private fun refresh(){
        val userName: TextView by lazy {findViewById(R.id.userName)}
        val numberOfGames: TextView by lazy {findViewById(R.id.numberOfGames)}
        val numberOfExtensions : TextView by lazy {findViewById(R.id.numberOfExtensions)}
        val lastSync: TextView by lazy {findViewById(R.id.lastSync)}
        val userDBHandler = UserDBHandler(this, null, null, 1)
        val user = userDBHandler.getUser()!!

        userName.text = "Username:\n" + user.name + "\n"
        numberOfGames.text = "Liczba posiadanych gier:\n " + user.gamesNumber.toString() + "\n"
        numberOfExtensions.text = "Liczba posiadanych dodatków:\n " + user.extensionNumber.toString() + "\n"
        lastSync.text = "Ostatnia synchronizacja:\n " + user.lastSync + "\n"
    }

    fun gamesClick(v: View){
        val intent = Intent(this, GamesActivity::class.java)
        startActivity(intent)
    }

    fun extensionClick(v: View){
        val intent = Intent(this, ExtensionsActivity::class.java)
        startActivity(intent)
    }

    fun syncClick(v: View){
        val user = UserDBHandler(this, null, null, 1).getUser()!!
        val intent = Intent(this, SynchronizationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.putExtra("name", user.name)
        intent.putExtra("lastSync", user.lastSync)
        startActivity(intent)
    }

    fun delClick(v: View){
        val userDBHandler = UserDBHandler(this, null ,null, 1)
        val gamesDBHandler = GameDBHandler(this, null ,null, 1)
        val extensionsDBHandler = ExtensionsDBHandler(this, null ,null, 1)
        gamesDBHandler.close()
        extensionsDBHandler.close()
        userDBHandler.close()
        this.deleteDatabase("gamesDB.db")
        this.deleteDatabase("extensionDB.db")
        this.deleteDatabase("userDB.db")
        finish()
        exitProcess(0)
    }
}