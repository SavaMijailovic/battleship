package app.battleship

import android.annotation.SuppressLint
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min
import kotlin.system.exitProcess

class ShipsPlacementActivity : AppCompatActivity() {

    companion object {
        const val DIMENSION = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ships_placement)
        hideSystemUI(window)

        val size = 10

        val layoutBoard = findViewById<LinearLayout>(R.id.layoutBoard).apply {
            layoutParams.apply {
                width = width / (size + 1) * (size + 1)
                height = height / (size + 1) * (size + 1)
            }
        }

        findViewById<LinearLayout>(R.id.layoutRight).apply {
            layoutParams.apply {
                width = layoutBoard.layoutParams.width
                height = layoutBoard.layoutParams.height
            }
        }

        val layoutShips = findViewById<LinearLayout>(R.id.layoutShips).apply {
            layoutParams.apply {
                width = width / (size + 1) * (size + 1)
                height = height / (size - 1) * (size - 1)
            }
        }

        try {
            val board = Board(size, this, layoutBoard)
            // val board = Board(size, this, layoutBoard, rightSide = true, active = true)
            // val boardRight = Board(size - 2, this, layoutShips, rightSide = true, active = true)

            val ships = generateShips(layoutShips)

            setShipsListeners(ships)
            setBoardListeners(board)
            setRandomButtonListener(ships, board)
        }
        catch (e: Exception) {
            e.printStackTrace(System.out)
            exitProcess(1)
        }

    }

    private var draggingStarted: Boolean = false
    private var activeShip: Ship? = null
    private var offset = 0

    private fun setShipsListeners(ships: List<Ship>) {
        setShipsDragListeners(ships)
        setShipsTouchListeners(ships)
    }

    private fun setBoardListeners(board: Board) {
        setBoardDragListeners(board)
        setBoardTouchListeners(board)
    }

    private fun setShipsDragListeners(ships: List<Ship>) {
        ships.forEach { ship ->
            ship.view?.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (ship == activeShip) {
                            ship.hide()
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        if (!event.result) {
                            activeShip?.show()
                            activeShip = null
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    // Klikom za randomizaciju postavimo nasumicnu poziciju za sve brodove
    private fun setRandomButtonListener(ships : List<Ship>, board : Board) {
        val randomButton = findViewById<Button>(R.id.btShipsPlacement)
        randomButton.setOnClickListener {
            randomPlacement(ships,board)
            for (ship in ships){
                ship.view?.visibility = View.INVISIBLE
            }
        }
        randomButton.text = "\uD83C\uDFB2"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setShipsTouchListeners(ships: List<Ship>) {
        ships.forEach { ship ->
            ship.view?.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startDraggingShip(ship, event)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun startDraggingShip(ship: Ship, event: MotionEvent) {
        val view = ship.view as LinearLayout

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX - location[0]
        val y = event.rawY - location[1]

        val shadowView = generateShipView(
            ship.size, view.width, view.height,
            orientation = LinearLayout.HORIZONTAL,
            setBorder = false
        )
        shadowView.measure(view.width, view.height)
        shadowView.layout(0, 0, shadowView.measuredWidth, shadowView.measuredHeight)

        val shadowBuilder = object : View.DragShadowBuilder(shadowView) {
            override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
                outShadowSize.set(shadowView.width, shadowView.height)
                outShadowTouchPoint.set(x.toInt(), y.toInt())
            }
        }

        this.offset = (x / (shadowView.width.toDouble() / ship.size)).toInt()
        this.activeShip = ship

        view.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
    }

    private fun setBoardDragListeners(board: Board) {
        board.forEach { field, view ->
            view.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (field.isShip() && field.ship == activeShip) {
                            field.ship?.clear()
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        field.background?.color = Color.TRANSPARENT
                        if (!event.result) {
                            activeShip?.show()
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENTERED -> {
                        processDragging(board, field) { field, _ ->
                            field.background?.color = Color.LTGRAY
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_EXITED -> {
                        processDragging(board, field) { field, _ ->
                            field.background?.color = Color.TRANSPARENT
                        }
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        processDragging(board, field) { field, ship ->
                            field.background?.color = Color.TRANSPARENT
                            ship.add(field)
                        }
                    }

                    else -> false
                }
            }
        }
    }

    private fun removeIndexFieldsElements(list : MutableList<Pair<Int,Int>>, x : Int, y : Int) : MutableList<Pair<Int,Int>> {
        for (i in max(x-1,0) until min(x+2,10)) {
            for (j in max(y-1,0) until min(y+1,10)) {
                list.remove(Pair(i,j))
            }
        }
        return list
    }

    fun randomPlacement(ships : List<Ship>, board : Board) {

        // inicijalizujemo listu parova prirodnih brojeva  { (x,y) | x ∈ [0,9], y ∈ [0,9] }
        var list : MutableList<Pair<Int, Int>> = mutableListOf()
        for (i in 0 until board.size) {
            for (j in 0 until board.size) {
                list.add(Pair(i,j))
            }
        }

        val directions = arrayOf("horizontal","vertical")

        for (ship in ships) {
            ship.clear()
            while (true) {
                // biramo nasumicno poziciju u nasoj listi
                val (x,y) = list.random()
                val n = ship.size

                // nasumicno biramo pravac
                val direction = directions.random()

                if (direction.equals("horizontal") && y + n <= board.size && board.checkForBoat(x,y,y+n)){
                    for (j in y until y + n) {
                        ship.add(board[x][j])
                        // nakon sto postavimo brod na tabli izbrisemo iz liste sve parove koje pripadaju brodu
                        // i sve one u njegovoj okolini kako ne bi izvukli nevalidno polje
                        removeIndexFieldsElements(list,x,j)
                    }
                    break
                }
                else if (direction.equals("vertical") && x + n <= board.size && board.checkForBoat2(y,x,x+n)){
                    for (i in x until x + n) {
                        ship.add(board[i][y])
                        removeIndexFieldsElements(list,i,y)
                    }
                    break
                }
            }
        }
    }

    private inline fun processDragging(board: Board, field: Field, action: (Field, Ship) -> Unit) : Boolean {
        if (activeShip == null) return false
        val ship = activeShip as Ship

        val (start, end) = if (ship.horizontal) {
            Pair(field.x - offset, field.x - offset + ship.size - 1)
        }
        else {
            Pair(field.y - offset, field.y - offset + ship.size - 1)
        }

        if (!board.isInside(start) || !board.isInside(end)) {
            return false
        }

        if (ship.horizontal) {
            if (!board.isAvailable(field.y - 1,field.y + 1, start - 1, end + 1)) {
                return false
            }
            for (k in start .. end) {
                action(board[field.y][k], ship)
            }
        }
        else {
            if (!board.isAvailable(start - 1, end + 1, field.x - 1, field.x + 1)) {
                return false
            }
            for (k in start .. end) {
                action(board[k][field.x], ship)
            }
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBoardTouchListeners(board: Board) {
        board.forEach { field, view ->
            view.setOnTouchListener { _, event ->
                if (!field.isShip()) {
                    return@setOnTouchListener false
                }

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        draggingStarted = false
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!draggingStarted) {
                            draggingStarted = true
                            startDraggingField(field, event)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!draggingStarted && field.isShip()) {
                            field.ship?.rotate(board)
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun startDraggingField(field: Field, event: MotionEvent) {
        val ship = field.ship as Ship
        val shipView = ship.view as LinearLayout

        val location = IntArray(2)
        field.view?.getLocationOnScreen(location)
        val x = event.rawX - location[0]
        val y = event.rawY - location[1]

        val offset = ship.fields.first().distanceTo(field)

        val shadowView = if (ship.horizontal) {
            val width = shipView.width
            val height = shipView.height

            val view = generateShipView(
                ship.size, width, height,
                orientation = LinearLayout.HORIZONTAL,
                setBorder = false
            )
            view.measure(width, height)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view
        }
        else {
            val width = shipView.height
            val height = shipView.width

            val view = generateShipView(
                ship.size, width, height,
                orientation = LinearLayout.VERTICAL,
                setBorder = false
            )
            view.measure(width, height)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view
        }

        val shadowBuilder = object : View.DragShadowBuilder(shadowView) {
            override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
                outShadowSize.set(shadowView.width, shadowView.height)
                if (ship.horizontal) {
                    outShadowTouchPoint.set(x.toInt() + offset * shadowView.height, y.toInt())
                } else {
                    outShadowTouchPoint.set(x.toInt(), y.toInt() + offset * shadowView.width)
                }
            }
        }

        this.offset = offset
        this.activeShip = ship

        field.view?.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
    }

    private fun generateShips(layout: LinearLayout) : List<Ship> {
        val size = DIMENSION + 1
        var shipSize = 4

        val ships = mutableListOf<Ship>()

        for (i in 0 until size - 2) {
            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    layout.layoutParams.width,
                    layout.layoutParams.height / (size - 2)
                )
                orientation = LinearLayout.HORIZONTAL
            }
            layout.addView(rowLayout)

            if (i % 2 == 1) {
                val n = 5 - shipSize

                for (j in 0 until n * 2 - 1) {
                    if (j % 2 == 0) {
                        val width = rowLayout.layoutParams.width / size * shipSize
                        val height = rowLayout.layoutParams.height
                        val ship = Ship(shipSize)
                        ship.view = generateShipView(shipSize, width, height)
                        rowLayout.addView(ship.view)
                        ships.add(ship)
                    }
                    else {
                        val shipLayout = LinearLayout(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                rowLayout.layoutParams.width / size,
                                rowLayout.layoutParams.height
                            )
                            orientation = LinearLayout.HORIZONTAL
                        }
                        rowLayout.addView(shipLayout)
                    }
                }
                --shipSize
                if (shipSize == 0) break
            }
        }
        return ships
    }

    private fun generateShipView(
        size: Int,
        width: Int,
        height: Int,
        orientation: Int = LinearLayout.HORIZONTAL,
        setBorder: Boolean = true
    ) : LinearLayout {

        val shipView = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            this.orientation = orientation
        }

        for (k in 0 until size) {
            val tv = TextView(this).apply {
                layoutParams = if (orientation == LinearLayout.HORIZONTAL) {
                    LinearLayout.LayoutParams(width / size, height)
                }
                else {
                    LinearLayout.LayoutParams(width, height / size)
                }
                setTextSize(TypedValue.COMPLEX_UNIT_PX, layoutParams.height * 0.7f)
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                text = Field.State.SHIP.toString()
            }
            shipView.addView(tv)

            if (setBorder) {
                if (orientation == LinearLayout.HORIZONTAL) {
                    tv.background = BorderDrawable(left = k == 0)
                }
                else {
                    tv.background = BorderDrawable(top = k == 0)
                }
            }
        }
        return shipView
    }
}