package app.battleship

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.Selection
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class ShipsPlacementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ships_placement)
        hideSystemUI(window)

        resize()

        val layoutBoard = findViewById<LinearLayout>(R.id.layoutBoard)
        val layoutShips = findViewById<LinearLayout>(R.id.layoutShips)

        val board = Board(context = this, layout = layoutBoard)
        val ships = generateShips(layoutShips)

        setPlayers(board)

        setShipsListeners(ships)
        setBoardListeners(board)
        setButtonListeners(board, ships)
        setTextViewListeners()
    }

    private val gamemode = GameManager.gamemode
    private var player1: Player? = GameManager.player1
    private var player2: Player? = GameManager.player2

    private var draggingStarted = false
    private var draggingX = 0f
    private var draggingY = 0f
    private var draggingDropped = false
    private var draggingShip: Ship? = null
    private var offset = 0

    private fun setPlayers(board: Board) {
        when (gamemode) {
            Gamemode.SINGLEPLAYER -> {
                player1 = DevicePlayer("Player1", board)

                val board2 = Board()
                val ships2 = generateShips()
                board2.randomPlacement(ships2)
                player2 = BotPlayer("Bot", board2)
            }

            Gamemode.MULTIPLAYER_DEVICE -> {
                if (player1 == null) {
                    player1 = DevicePlayer("Player1", board)
                    findViewById<Button>(R.id.btBattle).apply {
                        text = "Next"
                    }
                }
                else {
                    player2 = DevicePlayer("Player2", board)
                    findViewById<TextView>(R.id.tvPlayer1).apply {
                        text = player2?.name ?: ""
                    }
                }
            }
        }
    }

    private fun nextPlayer() {
        GameManager.setPlayers(player1, player2)

        if (player1?.isReady() != true) {
            return
        }

        val name = findViewById<TextView>(R.id.tvPlayer1).text.toString().trim()
        if (name.isNotEmpty()) {
            player1?.name = name
        }

        startActivity(Intent(this, ShipsPlacementActivity::class.java))
    }

    private fun startGame() {
        GameManager.setPlayers(player1, player2)

        if (!GameManager.isReady()) return

        val name = findViewById<TextView>(R.id.tvPlayer1).text.toString().trim()
        if (name.isNotEmpty()) {
            if (gamemode == Gamemode.MULTIPLAYER_DEVICE && player2 != null) {
                player2?.name = name
            }
            else {
                player1?.name = name
            }
        }

        startActivity(Intent(this, GameActivity::class.java))
    }

    override fun finish() {
        GameManager.reset()
        super.finish()
    }

    private fun setButtonListeners(board: Board, ships: List<Ship>) {
        findViewById<ImageButton>(R.id.btBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btRandomPlacement).setOnClickListener {
            board.randomPlacement(ships)
        }

        findViewById<Button>(R.id.btClear).setOnClickListener {
            board.forEach { field -> field.ship = null }
            ships.forEach { ship -> ship.clear(); ship.show() }
        }

        findViewById<Button>(R.id.btBattle).setOnClickListener {
            if (gamemode == Gamemode.MULTIPLAYER_DEVICE && player2 == null) {
                nextPlayer()
            }
            else {
                startGame()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTextViewListeners() {
        findViewById<TextView>(R.id.tvPlayer1).apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                Selection.setSelection(editableText, editableText.length)
            }
        }
    }

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
                        if (ship == draggingShip) {
                            ship.hide()
                            draggingDropped = false
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        if (!event.result || !draggingDropped) {
                            draggingShip?.show()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
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
        this.draggingShip = ship

        view.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
    }

    private fun setBoardDragListeners(board: Board) {
        board.forEach { field, view ->
            view.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (field.isShip() && field.ship == draggingShip) {
                            field.ship?.clear()
                            draggingDropped = false
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        field.background?.color = Color.TRANSPARENT
                        if (!event.result || !draggingDropped) {
                            draggingShip?.show()
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
                        draggingDropped = processDragging(board, field) { field, ship ->
                            field.background?.color = Color.TRANSPARENT
                            ship.add(field)
                        }
                        draggingDropped
                    }

                    else -> false
                }
            }
        }
    }

    private fun processDragging(board: Board, field: Field, action: (Field, Ship) -> Unit) : Boolean {
        if (draggingShip == null) return false
        val ship = draggingShip as Ship

        if (ship.horizontal) {
            val (start, end) = Pair(field.col - offset, field.col - offset + ship.size - 1)
            return board.set(Field(field.row, start), Field(field.row, end), ship, action)
        }
        else {
            val (start, end) = Pair(field.row - offset, field.row - offset + ship.size - 1)
            return board.set(Field(start, field.col), Field(end, field.col), ship, action)
        }
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
                        draggingX = event.rawX
                        draggingY = event.rawY
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!draggingStarted && (event.rawX != draggingX || event.rawY != draggingY)) {
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
        this.draggingShip = ship

        field.view?.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
    }

    private fun generateShips() : List<Ship> {
        val maxShipSize = 4; val minShipSize = 1
        val ships = ArrayList<Ship>()
        for (shipSize in maxShipSize downTo minShipSize) {
            for (n in 0 until 5 - shipSize) {
                ships.add(Ship(shipSize))
            }
        }
        return ships
    }

    private fun generateShips(layout: LinearLayout) : List<Ship> {
        val size = Board.DIMENSION + 1
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

    private fun resize() {
        val size = getUnitSize(resources)

        arrayOf(R.id.tvPlayer1, R.id.tvPlayer2).forEach { id ->
            findViewById<TextView>(id).apply {
                layoutParams.apply {
                    width = 11 * size
                    height = size
                }
            }
        }

        findViewById<LinearLayout>(R.id.layoutBoard).apply {
            layoutParams.apply {
                width = 11 * size
                height = 11 * size
            }
        }

        findViewById<LinearLayout>(R.id.layoutShips).apply {
            layoutParams.apply {
                width = 11 * size
                height = 9 * size
            }
        }

        findViewById<LinearLayout>(R.id.layoutMiddle).apply {
            layoutParams.apply {
                width = 2 * size
                height = 12 * size
            }
        }

        findViewById<ConstraintLayout>(R.id.layoutButtons).apply {
            layoutParams.apply {
                width = 11 * size
                height = 2 * size
            }
        }

        findViewById<Button>(R.id.btRandomPlacement).apply {
            layoutParams.apply {
                width = 2 * size
                height = 2 * size
            }
        }

        findViewById<Button>(R.id.btClear).apply {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = 2 * size
                height = 2 * size
                leftMargin = size
            }
        }

        findViewById<Button>(R.id.btBattle).apply {
            layoutParams.apply {
                width = 3 * size
                height = 2 * size
            }
        }

        findViewById<ImageButton>(R.id.btBack).apply {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = 2 * size
                height = 2 * size
                topMargin = size
            }
        }
    }
}