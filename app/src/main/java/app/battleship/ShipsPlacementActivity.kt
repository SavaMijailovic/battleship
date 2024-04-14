package app.battleship

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ShipsPlacementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ships_placement)
        hideSystemUI(window)

        val size = 10

        val layoutBoard = findViewById<LinearLayout>(R.id.layoutBoard)
        layoutBoard.layoutParams.width = layoutBoard.layoutParams.width / (size + 1) * (size + 1)
        layoutBoard.layoutParams.height = layoutBoard.layoutParams.height / (size + 1) * (size + 1)

        val layoutRight = findViewById<LinearLayout>(R.id.layoutRight)
        layoutRight.layoutParams.width = layoutBoard.layoutParams.width
        layoutRight.layoutParams.height = layoutBoard.layoutParams.height

        val layoutShips = findViewById<LinearLayout>(R.id.layoutShips)
        layoutShips.layoutParams.width = layoutShips.layoutParams.width / (size + 1) * (size + 1)
        layoutShips.layoutParams.height = layoutShips.layoutParams.height / (size - 1) * (size - 1)


        val board = Board(size, this, layoutBoard, active = true)
        // val board = Board(size, this, layoutBoard, leftSide = false)

        val ships = generateShips(size, layoutShips)
        // val boardLeft = Board(size - 2, this, layoutShips, false)

        board.toogleActiveState()

        for (ship in ships) {
            ship.view?.setOnClickListener() {
                board.setBoardClickListener(ship.health, ship)
                board.toogleActiveState()
                ship.view?.alpha = 0.5f
            }

        }

        val randomButton = findViewById<Button>(R.id.btShipsPlacement)
        randomButton.setOnClickListener {
            for (ship in ships) {
                ship.view?.alpha = 0.5f
                ship.view?.visibility = View.INVISIBLE
            }
            board.toogleActiveState()
            board.setRandomPlacement(ships)
        }

    }

    private fun generateShips(dimension: Int, layout: LinearLayout) : List<Ship> {
        val size = dimension + 1
        var shipSize = 4

        val ships = mutableListOf<Ship>()

        for (i in 0 until size - 2) {
            val rowLayout = LinearLayout(this)
            layout.addView(rowLayout)

            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams.width = layout.layoutParams.width
            rowLayout.layoutParams.height = layout.layoutParams.height / (size - 2)

            if (i % 2 == 1) {
                val n = 5 - shipSize

                for (j in 0 until n * 2 - 1) {
                    val shipLayout = LinearLayout(this)
                    rowLayout.addView(shipLayout)
                    shipLayout.orientation = LinearLayout.HORIZONTAL

                    if (j % 2 == 0) {
                        ships.add(Ship(shipSize, shipLayout))

                        shipLayout.layoutParams.width = rowLayout.layoutParams.width / size * shipSize
                        shipLayout.layoutParams.height = rowLayout.layoutParams.height
                        // shipLayout.background = Board.BorderDrawable()

                        for (k in 0 until shipSize) {
                            val tv = TextView(this)
                            shipLayout.addView(tv)

                            tv.layoutParams.width = shipLayout.layoutParams.width / shipSize
                            tv.layoutParams.height = shipLayout.layoutParams.height
                            tv.gravity = Gravity.CENTER
                            tv.setTextColor(Color.BLACK)
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.7f)
                            tv.text = "\uD83D\uDEA2"

                            if (k == 0) {
                                tv.background = BorderDrawable()
                            }
                            else {
                                tv.background = BorderDrawable(left = false)
                            }
                        }
                    }
                    else {
                        shipLayout.layoutParams.width = rowLayout.layoutParams.width / size
                        shipLayout.layoutParams.height = rowLayout.layoutParams.height
                    }
                }

                --shipSize
                if (shipSize == 0) break
            }

        }

        return ships
    }
}