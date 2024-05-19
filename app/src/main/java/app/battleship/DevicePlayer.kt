package app.battleship

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent

open class DevicePlayer(name: String, val board: Board) : Player(name) {

    override var opponentBoard: Board
        get() = super.opponentBoard
        set(value) {
            super.opponentBoard = value
            if (this::class == DevicePlayer::class) {
                setBoardListeners()
            }
        }

    private val lock = Object()

    private var target: Field? = null
        set(value) {
            synchronized(lock) {
                field = value
                lock.notify()
            }
        }

    override fun nextTarget() : Field {
        opponentBoard.active = true
        synchronized(lock) {
            while (target == null || !isValid(target!!)) {
                lock.wait()
            }
        }
        opponentBoard.active = false
        return target.also { target = null } as Field
    }

    override fun fire(target: Field) : Pair<Boolean, Boolean> {
        val field = board[target]
        if (field.isShip()) {
            field.ship!!.health--
            this.health--
            return Pair(true, field.ship!!.health == 0)
        }
        return Pair(false, false)
    }

    override fun isReady() : Boolean {
        var count = 0
        board.forEach { field -> if (field.isShip()) ++count }
        return count == health
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBoardListeners() {
        val board = opponentBoard

        board.layout?.setOnTouchListener { _, event ->
            if (!board.active) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    board.forEach { field, tv ->

                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (!board.isInside(event.rawX, event.rawY)) {
                            field.background?.color = Color.TRANSPARENT
                        }
                        else if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            field.background?.color = Color.DKGRAY
                        }
                        else if ((event.rawX >= x && event.rawX <= x + tv.width) ||
                            (event.rawY >= y && event.rawY <= y + tv.height)) {

                            field.background?.color = Color.LTGRAY
                        }
                        else {
                            field.background?.color = Color.TRANSPARENT
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    board.forEach { field, tv ->
                        field.background?.color = Color.TRANSPARENT

                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            this.target = field
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }
}