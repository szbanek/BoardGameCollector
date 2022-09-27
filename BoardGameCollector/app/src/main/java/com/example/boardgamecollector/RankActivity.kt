package com.example.boardgamecollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class Ranking(
    var row: Int,
    var rank: Int,
    var date: String
)

class RankActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)
        val id = intent.getStringExtra("id")!!.toLong()
        val gameDBHandler = GameDBHandler(this, null, null, 1)
        firstRow(gameDBHandler.findGame(id))
        for(ranking in gameDBHandler.getRankingList(id)){
            addRow(ranking)
        }
    }

    val table: TableLayout by lazy {findViewById(R.id.main_table)}
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

    fun firstRow(game: Game){
        var tbrow = TableRow(this)

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

        table.addView(tbrow)

        tbrow = TableRow(this)

        val textRow = TextView(this)
        textRow.text = "Nr"
        textRow.gravity = Gravity.CENTER
        textRow.textSize = 8f
        textRow.layoutParams = param2
        tbrow.addView(textRow)

        val textRank2 = TextView(this)
        textRank2.text = "Rank"
        textRank2.gravity = Gravity.CENTER
        textRank2.textSize = 8f
        textRank2.layoutParams = param2
        tbrow.addView(textRank2)

        val textDate = TextView(this)
        textDate.text = "Data"
        textDate.gravity = Gravity.CENTER
        textDate.textSize = 8f
        textDate.layoutParams = param5
        tbrow.addView(textDate)

        table.addView(tbrow)
    }

    fun addRow(ranking: Ranking){
        val tbrow = TableRow(this)

        val textRow = TextView(this)
        textRow.text = ranking.row.toString()
        textRow.gravity = Gravity.CENTER
        textRow.textSize = 8f
        textRow.layoutParams = param2
        tbrow.addView(textRow)

        val textRank = TextView(this)
        textRank.text = ranking.rank.toString()
        textRank.gravity = Gravity.CENTER
        textRank.textSize = 8f
        textRank.layoutParams = param2
        tbrow.addView(textRank)

        val textDate = TextView(this)
        textDate.text = ranking.date
        textDate.gravity = Gravity.CENTER
        textDate.textSize = 8f
        textDate.layoutParams = param5
        tbrow.addView(textDate)

        table.addView(tbrow)
    }
}