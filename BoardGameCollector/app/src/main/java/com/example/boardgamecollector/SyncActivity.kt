package com.example.boardgamecollector

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        name = intent.getStringExtra("name")!!
        if(intent.getStringExtra("lastSync")!=null){
            lastSync = intent.getStringExtra("lastSync").toString()
            val syncText = findViewById<TextView>(R.id.syncDate)
            syncText.text = "Ostatnia synchronizacja:\n " + lastSync
        }
    }

    lateinit var name:String
    lateinit var lastSync:String

    @Suppress("DEPRECATION")
    private inner class Downloader: AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            progressBar.isVisible = true
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            progressBar.isVisible = false
        }

        override fun doInBackground(vararg p0: String): String {
            login(p0[0])
            return "success"
        }

        private fun login(name: String) {
            var gamesIdList: MutableList<Long> = mutableListOf()
            var data =
                getData(URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$name"))
            if (data.contains("<error")) {
                val intent2 = Intent(this@SyncActivity, LoginActivity::class.java)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent2)
                return
            }
            if (data.contains("<message")) {
                Thread.sleep(5000)
                Downloader().execute(name)
                return
            }
            while (data.isNotEmpty()) {
                val dataLine = data.substringBefore("\n")
                data = data.substringAfter("\n")
                if (dataLine.contains("subtype=\"boardgame\"")) {
                    gamesIdList.add(
                        dataLine.substringAfter("objectid=\"").substringBefore("\"").toLong()
                    )
                }
            }
            var counter = 0
            val userDbHandler = UserDBHandler(this@SyncActivity, null, null, 1)
            val gameDBHandler = GameDBHandler(this@SyncActivity, null, null, 1)
            val dlcDBHandler = DlcDBHandler(this@SyncActivity, null, null, 1)
            val tmpUser = User(name, 0, 0)
            for (gameID in gamesIdList) {
                var gameName = ""
                var originalName = ""
                var thumbnail = ""
                var year = 0
                var rank = 0
                var dlc = false

                data =
                    getData(URL("https://www.boardgamegeek.com/xmlapi2/thing?id=$gameID&stats=1"))
                while (data.isNotEmpty()) {
                    val dataLine = data.substringBefore("\n")
                    data = data.substringAfter("\n")
                    if (dataLine.contains("<item type=\"boardgameexpansion\"")) {
                        dlc = true
                    } else if (dataLine.contains("<thumbnail>")) {
                        thumbnail =
                            dataLine.substringAfter("<thumbnail>").substringBefore("</thumbnail>")
                    } else if (dataLine.contains("<name type=\"primary\"")) {
                        originalName = dataLine.substringAfter("value=\"").substringBefore("\"")
                    } else if (dataLine.contains("<name type=\"alternate\"") && gameName == "") {
                        gameName = dataLine.substringAfter("value=\"").substringBefore("\"")
                    } else if (dataLine.contains("<yearpublished")) {
                        year = dataLine.substringAfter("value=\"").substringBefore("\"").toInt()
                    } else if (dataLine.contains("<rank type=\"subtype\"") && dataLine.contains("name=\"boardgame\"")) {
                        val tmp = dataLine.substringAfter("value=\"").substringBefore("\"")
                        if (tmp != "Not Ranked") {
                            rank = tmp.toInt()
                        }
                    }
                }
                if (!dlc) {
                    gameDBHandler.addGame(
                        Game(
                            gameID,
                            gameName,
                            originalName,
                            thumbnail,
                            year,
                            rank
                        ), tmpUser.lastSync
                    )
                } else {
                    dlcDBHandler.addDlc(Dlc(gameID, gameName, originalName, thumbnail, year))
                }
                counter += 1
            }
            userDbHandler.newUser(
                User(
                    tmpUser.name,
                    gameDBHandler.getGamesNumber(),
                    dlcDBHandler.getDlcsNumber(),
                    tmpUser.lastSync
                )
            )
            val intent = Intent(this@SyncActivity, MainActivity::class.java)
            startActivity(intent)
        }

        private fun getData(url: URL): String {
            try {
                val connection = url.openConnection()
                connection.connect()
                val isStream = connection.getInputStream()
                val data = isStream.bufferedReader().use { it.readText() } + "\n"
                isStream.close()
                return data
            } catch (e: Exception) {
                return getData(url, 10)
            }
        }

        private fun getData(url: URL, time: Long): String {
            try {
                val connection = url.openConnection()
                connection.connect()
                Thread.sleep(time)
                val isStream = connection.getInputStream()
                val data = isStream.bufferedReader().use { it.readText() } + "\n"
                isStream.close()
                return data
            } catch (e: Exception) {
                if (time <= 50) {
                    return getData(url, time + 20)
                } else return ""
            }
        }
    }

    fun synchroniseClick(v: View){
        if(!this::lastSync.isInitialized)
        {
            Downloader().execute(name)
            return
        }
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        if(lastSync.take(10)==sdf.format(Date()).take(10)){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Na pewno chcesz zsynchronizować?")
                .setCancelable(false)
                .setPositiveButton("Tak") { dialog, id ->
                    Downloader().execute(name)
                }
                .setNegativeButton("Nie") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        else{
            Downloader().execute(name)
        }
    }
}