package app.battleship

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

@SuppressLint("MissingPermission")
class ShipsPlacementActivity : BaseActivity(R.layout.activity_ships_placement) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun finish() {
        leaving = true
        BluetoothManager.connection?.close()
        super.finish()
    }

    private var leaving = false
    private val ready = AtomicBoolean(false)

    private var player1: Player? = GameManager.player1
    private var player2: Player? = GameManager.player2

    private val dragging = object {
        var started = false
        var x = 0f
        var y = 0f
        var dropped = false
        var ship: Ship? = null
        var offset = 0
        private val eps = 10f
        fun moved(x: Float, y: Float) = abs(this.x - x) >= eps || abs(this.y - y) >= eps
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayers(board: Board) {
        when (GameManager.gamemode) {
            Gamemode.SINGLEPLAYER -> {
                player1 = DevicePlayer("Player1", board)
                player2 = BotPlayer("Bot")
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

            Gamemode.MULTIPLAYER_BLUETOOTH -> {
                if (BluetoothManager.connection == null) {
                    finish()
                    return
                }

                val connection = BluetoothManager.connection!!

                player1 = DevicePlayer(BluetoothManager.adapter.name ?: "Player1", board)
                findViewById<TextView>(R.id.tvPlayer1).text = player1?.name

                val btPlayer = BluetoothPlayer(connection.socket.remoteDevice.name ?: "Other player", connection)
                player2 = btPlayer

                Thread {
                    val tvPlayer2: TextView = findViewById(R.id.tvPlayer2)

                    runOnUiThread {
                        tvPlayer2.setTextColor(getColor(R.color.red))
                        tvPlayer2.text = "Other player is not ready"
                    }

                    try {
                        while (true) {
                            btPlayer.ready = connection.read().toBooleanStrictOrNull() ?: continue

                            if (btPlayer.ready) {
                                if (ready.get()) {
                                    connection.write(true)
                                    runOnUiThread {
                                        startGame()
                                    }
                                    break
                                }
                                else {
                                    runOnUiThread {
                                        tvPlayer2.setTextColor(getColor(R.color.green))
                                        tvPlayer2.text = "Other player is ready"
                                    }
                                }
                            }
                            else {
                                runOnUiThread {
                                    tvPlayer2.setTextColor(getColor(R.color.red))
                                    tvPlayer2.text = "Other player is not ready"
                                }
                            }
                        }
                    }
                    catch (_: Exception) {
                        if (!leaving) {
                            runOnUiThread {
                                tvPlayer2.setTextColor(getColor(R.color.text))
                                tvPlayer2.text = "Connection lost"

                                AlertDialog.Builder(this)
                                    .setTitle("Connection lost")
                                    .setNegativeButton("Cancel") { _, _ -> }
                                    .create()
                                    .show()
                            }
                        }
                        connection.close()
                    }
                }.start()
            }
        }
    }

    private fun checkIfReady() {
        ready.set(player1?.isReady() == true)
        if (!ready.get()) return

        val connection = BluetoothManager.connection!!
        if (!connection.isConnected()) throw RuntimeException()

        connection.write(true)

        if (player2?.isReady() != true) {
            AlertDialog.Builder(this)
                .setTitle("Waiting for ${connection.socket.remoteDevice.name ?: "other player"}...")
                .setNegativeButton("Cancel") { _, _ ->
                    ready.set(false)
                    connection.write(false)
                }
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                }
                .show()
        }
    }

    private fun nextPlayer() {
        if (player1?.isReady() != true) return

        val name = findViewById<TextView>(R.id.tvPlayer1).text.toString().trim()
        if (name.isNotEmpty()) {
            player1?.name = name
        }

        GameManager.setPlayers(player1, player2)
        startActivity(Intent(this, ShipsPlacementActivity::class.java))
        super.finish()
    }

    private fun startGame() {
        GameManager.setPlayers(player1, player2)

        if (!GameManager.isReady()) return

        val name = findViewById<TextView>(R.id.tvPlayer1).text.toString().trim()
        if (name.isNotEmpty()) {
            when (GameManager.gamemode) {
                Gamemode.SINGLEPLAYER, Gamemode.MULTIPLAYER_BLUETOOTH -> {
                    player1?.name = name
                }
                Gamemode.MULTIPLAYER_DEVICE -> {
                    player2?.name = name
                }
            }
        }

        startActivity(Intent(this, GameActivity::class.java))
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
            ships.forEach { ship ->
                if (ship.fields.isEmpty()) {
                    ship.view?.animate()?.alpha(0.2f)?.withEndAction {
                        ship.view?.animate()?.alpha(1f)
                    }
                }
            }

            if (GameManager.gamemode == Gamemode.MULTIPLAYER_DEVICE && player2 == null) {
                nextPlayer()
            }
            else if (GameManager.gamemode == Gamemode.MULTIPLAYER_BLUETOOTH) {
                try {
                    checkIfReady()
                }
                catch (_: Exception) {
                    AlertDialog.Builder(this)
                        .setTitle("Connection lost")
                        .setNegativeButton("Cancel") { _, _ -> }
                        .create()
                        .show()
                    BluetoothManager.connection?.close()
                }
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
                        if (ship == dragging.ship) {
                            ship.hide()
                            dragging.dropped = false
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        if (!event.result || !dragging.dropped) {
                            dragging.ship?.show()
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
                        startDraggingShip(ship, event.rawX, event.rawY)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun startDraggingShip(ship: Ship, posX: Float, posY: Float) {
        val view = ship.view as LinearLayout

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = abs(posX - location[0])
        val y = abs(posY - location[1])

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

        dragging.offset = (x / (shadowView.width.toDouble() / ship.size)).toInt()
        dragging.ship = ship

        view.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
    }

    private fun setBoardDragListeners(board: Board) {
        board.forEach { field, view ->
            view.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        if (field.isShip() && field.ship == dragging.ship) {
                            field.ship?.clear()
                            dragging.dropped = false
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        field.background?.color = Color.TRANSPARENT
                        if (!event.result || !dragging.dropped) {
                            dragging.ship?.show()
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
                        dragging.dropped = processDragging(board, field) { field, ship ->
                            field.background?.color = Color.TRANSPARENT
                            ship.add(field)
                        }
                        dragging.dropped
                    }

                    else -> false
                }
            }
        }
    }

    private fun processDragging(board: Board, field: Field, action: (Field, Ship) -> Unit) : Boolean {
        if (dragging.ship == null) return false
        val ship = dragging.ship as Ship

        if (ship.horizontal) {
            val (start, end) = Pair(field.col - dragging.offset, field.col - dragging.offset + ship.size - 1)
            return board.set(Field(field.row, start), Field(field.row, end), ship, action)
        }
        else {
            val (start, end) = Pair(field.row - dragging.offset, field.row - dragging.offset + ship.size - 1)
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
                        dragging.started = false
                        dragging.x = event.rawX
                        dragging.y = event.rawY
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!dragging.started && dragging.moved(event.rawX, event.rawY)) {
                            dragging.started = true
                            startDraggingField(field, dragging.x, dragging.y)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!dragging.started && field.isShip()) {
                            field.ship?.rotate(board)
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun startDraggingField(field: Field, posX: Float, posY: Float) {
        val ship = field.ship as Ship
        val shipView = ship.view as LinearLayout

        val location = IntArray(2)
        field.view?.getLocationOnScreen(location)
        val x = abs(posX - location[0])
        val y = abs(posY - location[1])

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

        dragging.offset = offset
        dragging.ship = ship

        field.view?.startDragAndDrop(null, shadowBuilder, null, View.DRAG_FLAG_OPAQUE)
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

    override fun resize() {
        val size = getUnitSize()

        arrayOf(R.id.tvPlayer1, R.id.tvPlayer2).forEach { id ->
            findViewById<TextView>(id).apply {
                layoutParams.apply {
                    width = 11 * size
                    height = size
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.6f)
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
            }
        }
    }
}