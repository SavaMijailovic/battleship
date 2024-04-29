package app.battleship

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BotPlayer(
    name: String,
    board: Board = Board(random = true),
    private val delayTime: Long = 500)
    : DevicePlayer(name, board)
{

    private val fields = mutableListOf<Field>()

    private var position : Field? = null
    private var top : Field? = null
    private var bottom : Field? = null
    private var left: Field? = null
    private var right: Field? = null
    private var prevTop : Field? = null
    private var prevBottom : Field? = null
    private var prevLeft: Field? = null
    private var prevRight: Field? = null
    private var search : Boolean = false

    init {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                fields.add(Field(row, col))
            }
        }
        fields.shuffle()
    }

    private fun checkField(field : Field?) : Boolean {

        if(field != null && opponentBoard.isInside(field)) {
            if (opponentBoard[field.row][field.col].state == Field.State.UNKNOWN) {
                return true
            }
        }

        return false
    }

    override fun nextTarget() : Field {
        runBlocking { delay(delayTime) }

        fields.removeAll { filed -> opponentBoard[filed.row][filed.col].state == Field.State.EMPTY }

        if (position != null && opponentBoard[position!!.row][position!!.col].state == Field.State.DESTROYED_SHIP) {
            search = true
        }
        else {
            top = null
            bottom = null
            left = null
            right = null
        }

        if (prevTop != null && opponentBoard[prevTop!!.row][prevTop!!.col].state == Field.State.EMPTY) {
            top = null
            prevTop = null
        }
        if (prevBottom != null && opponentBoard[prevBottom!!.row][prevBottom!!.col].state == Field.State.EMPTY){
            bottom = null
            prevBottom = null
        }
        if (prevLeft != null && opponentBoard[prevLeft!!.row][prevLeft!!.col].state == Field.State.EMPTY) {
            left = null
            prevLeft = null
        }
        if (prevRight != null && opponentBoard[prevRight!!.row][prevRight!!.col].state == Field.State.EMPTY) {
            right = null
            prevRight = null
        }

        if(top == null && bottom == null && left == null && right == null) {
            position = null
            search = false
        }

        if(search) {

            if (checkField(top)){
                fields.remove(top)
                prevTop = Field(top!!.row,top!!.col)
                return prevTop!!.also { top!!.row-- }
            }
            else {
                top = null
            }

            if (checkField(bottom)) {
                fields.remove(bottom)
                prevBottom = Field(bottom!!.row,bottom!!.col)
                return prevBottom!!.also { bottom!!.row++ }
            }
            else {
                bottom = null
            }

            if (checkField(left)){
                fields.remove(left)

                prevLeft = Field(left!!.row,left!!.col)
                return  prevLeft!!.also { left!!.col-- }
            }
            else {
                left = null
            }

            if (checkField(right)){
                fields.remove(right)
                prevRight = Field(right!!.row,right!!.col)
                return prevRight!!.also { right!!.col++ }
            }
            else {
                right = null
            }
        }

        if (position == null) {
            position = fields.last()
            top = Field(position!!.row - 1, position!!.col)
            bottom = Field(position!!.row + 1, position!!.col)
            left = Field(position!!.row, position!!.col - 1)
            right = Field(position!!.row, position!!.col + 1)
        }

        return fields.last().also { fields.removeLast() }
    }

}