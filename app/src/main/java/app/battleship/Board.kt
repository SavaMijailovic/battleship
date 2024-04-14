package app.battleship

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import org.w3c.dom.Text
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Board(
    size: Int,
    context: Context? = null,
    layout: LinearLayout? = null,
    leftSide: Boolean = true,
    active: Boolean = false
) {

    val size: Int

    private var fields: Array<Array<Field>>

    var tvBoard: Array<Array<TextView?>>? = null
        private set

    var active: Boolean

    init {
        this.size = size.coerceIn(1..20)
        fields = Array(this.size) { i -> Array(this.size) { j -> Field(j,i) } }

        if (context != null && layout != null) {
            generateBoard(context, layout, this.size, leftSide)
        }
        this.active = active
    }

    operator fun get(index: Int) : Array<Field> {
        return fields[index]
    }

    fun toogleActiveState() {
        active = !active
    }

    private fun checkForBoat(i1 : Int,j1 : Int, j2 : Int) : Boolean {
        for (i in max(i1-1,0) until min(i1+2,this.size)) {
            for (j in max(j1-1,0) until min(j2+1,this.size)) {
                if (this[i][j].state == Field.State.SHIP)
                    return false
            }
        }
        return true
    }

    private fun checkForBoat2(j1 : Int, i1 : Int, i2 : Int) : Boolean {
        for (j in max(j1-1,0) until min(j1+2,this.size)) {
            for (i in max(i1-1,0) until min(i2+1,this.size)) {
                if (this[i][j].state == Field.State.SHIP)
                    return false
            }
        }
        return true
    }

    fun restartBoard() {
        for(i in 0 until this.size) {
            for(j in 0 until this.size) {
                if (this[i][j].state != Field.State.UNKNOWN) {
                    this[i][j].state = Field.State.UNKNOWN
                }
                tvBoard?.get(i)?.get(j)?.text = this[i][j].state.toString()
            }
        }
    }

    fun setBoardClickListener(n : Int, ship : Ship){
        for (i in 0 until this.size) {
            for (j in 0 until this[i].size) {

                this.tvBoard?.get(i)?.get(j)?.setOnClickListener {
                    if (active) {
//                        if (this[i][j].state == Field.State.UNKNOWN)
//                            this[i][j].state = Field.State.SHIP
//                        else if (this[i][j].state == Field.State.SHIP)
//                            this[i][j].state = Field.State.UNKNOWN

                        if (j + n <= this.size && checkForBoat(i,j,j+n)) {
                            for (k in j until j + n) {
                                if (this[i][k].state == Field.State.UNKNOWN)
                                    this[i][k].state = Field.State.SHIP
                                ship.fileds?.add(fields[i][j])
                                tvBoard?.get(i)?.get(k)?.text = this[i][j].state.toString()
                            }
                            ship.placement = true
                            ship.view?.visibility = View.INVISIBLE
                        }
                        else {
                            ship.view?.alpha = 1f
                        }

                        // tvBoard?.get(i)?.get(j)?.text = this[i][j].state.toString()
                        toogleActiveState()
                    }
                }
            }
        }
    }

    private fun indexFields(): MutableList<Pair<Int, Int>> {

        var list : MutableList<Pair<Int, Int>> = mutableListOf()
        for (i in 0 until this.size) {
            for (j in 0 until this[i].size) {
                list.add(Pair(i,j))
            }
        }
        return list
    }



//    fun setRandomPlacement(ships : List<Ship>) {
//
//        while (true) {
//
//            var indexes = indexFields()
//
////            for (ship in ships) {
////
////                while (true) {
////
////                    var randColX = Random.nextInt(0, this.size)
////                    var randColY = Random.nextInt(0, this.size)
////
////                    var n = ship.length
////
////                    if (randColY + n <= this.size && checkForBoat(randColX,randColY,randColY + n)) {
////                        for(j in randColY until randColY + n) {
////                            this[randColX][j].state = Field.State.SHIP
////                            ship.fileds?.add(this[randColX][j])
////                            tvBoard?.get(randColX)?.get(j)?.text = this[randColX][j].state.toString()
////                        }
////                    }
////                    else if (randColX + n <= this.size && checkForBoat2(randColY,randColX,randColX + n)) {
////                        for(i in randColX until randColX + n) {
////                            this[i][randColY].state = Field.State.SHIP
////                            ship.fileds?.add(this[i][randColY])
////                            tvBoard?.get(i)?.get(randColY)?.text = this[i][randColY].state.toString()
////                        }
////                    }
////                    else if (randColY - n >= 0 && checkForBoat(randColX,randColY - n, randColY)) {
////                        for(j in randColY downTo randColY - n) {
////                            this[randColX][j].state = Field.State.SHIP
////                            ship.fileds?.add(this[randColX][j])
////                            tvBoard?.get(randColX)?.get(j)?.text = this[randColX][j].state.toString()
////                        }
////                    }
////                    else if (randColX - n >= 0 && checkForBoat2(randColY, randColX - n, randColX)) {
////                        for(i in randColX downTo  randColX - n) {
////                            this[i][randColY].state = Field.State.SHIP
////                            ship.fileds?.add(this[i][randColY])
////                            tvBoard?.get(i)?.get(randColY)?.text = this[i][randColY].state.toString()
////                        }
////                    }
////                    continue
////                }
////            }
//
//            indexes.clear()
//        }
//
//
//    }


//    fun setRandomPlacement(ships : List<Ship>){
//
//        restartBoard()
//
//        ships.sortedBy { ship ->  ship.health}
//
//        var randomPositions = mutableListOf<Pair<Int,Int>>()
//
//        var boardIsSet = false
//
//        do {
//            repeat(10) {
//                randomPositions.add(Pair(Random.nextInt(0, 10), Random.nextInt(0, 10)))
//            }
//
//
//            for ((i, j) in randomPositions) {
//                if (this[i][j].state == Field.State.UNKNOWN && checkForBoat(i,j,j+1)) {
//                    this[i][j].state = Field.State.SHIP
//                }
//                tvBoard?.get(i)?.get(j)?.text = this[i][j].state.toString()
//                boardIsSet = true
//            }
//            randomPositions.clear()
//        } while (!boardIsSet)
//
//    }

    private fun removeIndexFieldsElements(array : MutableList<Pair<Int,Int>>, coord : Pair<Int,Int>) : MutableList<Pair<Int,Int>> {
        val x = coord.first
        val y = coord.second

        for (i in max(x-1,0) until min(x+2,this.size)) {
            for (j in max(y-1,0) until min(y+1,this.size)) {
                array.remove(Pair(i,j))
            }
        }
        return array
    }

    fun setRandomPlacement(ships : List<Ship>) {
        restartBoard()

        var array = indexFields()
        val directions = arrayOf("horizontal","vertical")

        for (ship in ships) {
            while (true) {
                var (x,y) = array.random()
                val n = ship.length

                val direction = directions.random()

                if (direction.equals("horizontal") && y + n <= this.size &&  checkForBoat(x,y,y + n)) {
                    for (j in y until y + n) {
                        this[x][j].state = Field.State.SHIP
                        tvBoard?.get(x)?.get(j)?.text = this[x][j].state.toString()
                        removeIndexFieldsElements(array, Pair(x,j))
                    }
                        break
                }
                else if(direction.equals("vertical") && x + n <= this.size && checkForBoat2(y,x,x+n)) {
                    for (i in x until x + n) {
                        this[i][y].state = Field.State.SHIP
                        tvBoard?.get(i)?.get(y)?.text = this[i][y].state.toString()
                        removeIndexFieldsElements(array,Pair(i,y))
                    }
                    break
                }
            }
        }
    }

    fun generateBoard(
        context: Context,
        layout: LinearLayout,
        dimension: Int = this.size,
        leftSide: Boolean = true
    ) {
        val size = dimension + 1
        val tvBoard = Array<Array<TextView?>>(dimension) { arrayOfNulls(dimension) }

        for (i in 0 until size) {
            val rowLayout = LinearLayout(context)
            layout.addView(rowLayout)

            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams.width = layout.layoutParams.width
            rowLayout.layoutParams.height = layout.layoutParams.height / size
            rowLayout.gravity = Gravity.CENTER

            for (j in 0 until size) {
                val tv = TextView(context)
                if (i > 0 && j > 0) {
                    tvBoard[i-1][j-1] = tv
                }
                rowLayout.addView(tv)

                tv.layoutParams.width = rowLayout.layoutParams.width / size
                tv.layoutParams.height = rowLayout.layoutParams.height
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.BLACK)

                if (i > 1 && j > 1) {
                    tv.background = BorderDrawable(top = false, left = false)
                }
                else if (i > 1 && j == 1) {
                    tv.background = BorderDrawable(top = false)
                }
                else if (i == 1 && j > 1) {
                    tv.background = BorderDrawable(left = false)
                }
                else if (i == 1 && j == 1) {
                    tv.background = BorderDrawable()
                }

                if (i > 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.7f)
                }
                else if (i == 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.5f)
                    tv.text = j.toString()
                    // tv.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }
                else if (i > 0 && j == 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.5f)
                    tv.text = ('A' - 1 + i).toString()
                }

                // tv.setBackgroundColor(Color.rgb(i / (size - 1f), 0f, j / (size - 1f)))
            }
        }

        if (!leftSide) {
            for (child in layout.children) {
                val row = child as LinearLayout
                val view = row[0]
                row.removeViewAt(0)
                row.addView(view)
            }
        }

        this.tvBoard = tvBoard

//        for (i in 0 until this.size) {
//            for (j in 0 until this[i].size) {
//                this[i][j].view = tvBoard[i][j]
//                this[i][j].view?.text = this[i][j].state.toString()
//
//                tvBoard[i][j]?.setOnClickListener {
//                    if (active) {
//                        if (this[i][j].state == Field.State.UNKNOWN) {
//                            this[i][j].state = Field.State.EMPTY
//                        }
//                        else if (this[i][j].state == Field.State.EMPTY) {
//                            this[i][j].state = Field.State.SHIP
//                        }
//                        else if (this[i][j].state == Field.State.SHIP) {
//                            this[i][j].state = Field.State.DESTROYED_SHIP
//                        }
//                        else if (this[i][j].state == Field.State.DESTROYED_SHIP) {
//                            this[i][j].state = Field.State.UNKNOWN
//                        }
//                    }
//                }
//            }
//        }
    }
}