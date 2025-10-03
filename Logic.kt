package com.example.calculator

import java.util.EmptyStackException
import java.util.Stack
import kotlin.math.*

fun onButtonClick(
    button: String,
    updateInput: (String) -> Unit,
    updateResult: (String) -> Unit,
    currentInput: String
) {
    when (button) {
        "C" -> {
            updateInput("")
            updateResult("")
        }
        "DEL" -> {
            if (currentInput.isNotEmpty()) {
                updateInput(currentInput.dropLast(1))
            }
        }
        "=" -> {
            try {
                val expressionResult = evaluateExpression(currentInput)
                val resultString = if (expressionResult == expressionResult.toLong().toDouble()) {
                    expressionResult.toLong().toString()
                } else {
                    expressionResult.toString()
                }
                updateInput(resultString)
                updateResult("")
            } catch (e: Exception) {
                updateResult("Error: ${e.message}")
            }
        }
        "sin⁻¹" -> updateInput("$currentInput asin(")
        "cos⁻¹" -> updateInput("$currentInput acos(")
        "tan⁻¹" -> updateInput("$currentInput atan(")
        "√" -> updateInput("$currentInput sqrt(")
        "x^y" -> updateInput("$currentInput^")
        "x!" -> updateInput("$currentInput!")
        "sin", "cos", "tan", "ln", "log" -> updateInput("$currentInput$button(")
        "×" -> updateInput("$currentInput*")
        "÷" -> updateInput("$currentInput/")
        else -> updateInput(currentInput + button)
    }
}

private fun evaluateTop(ops: Stack<String>, values: Stack<Double>) {
    val op = ops.pop()
    try {
        if (isFunction(op)) {
            val arg = values.pop()
            values.push(applyFunc(op, arg))
        } else {
            val val2 = values.pop()
            val val1 = values.pop()
            values.push(applyOp(op, val1, val2))
        }
    } catch (e: EmptyStackException) {
        throw IllegalArgumentException("Invalid expression")
    }
}

private fun factorial(n: Double): Double {
    if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial is only for non-negative integers.")
    if (n == 0.0) return 1.0
    var result = 1.0
    for (i in 1..n.toInt()) {
        result *= i.toDouble()
    }
    return result
}

private fun isFunction(token: String): Boolean =
    token in setOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt")

private fun getPrecedence(op: String): Int {
    return when {
        isFunction(op) -> 4
        op == "^" -> 3
        op == "*" || op == "/" || op == "%" -> 2
        op == "+" || op == "-" -> 1
        else -> 0
    }
}

fun evaluateExpression(expression: String): Double {
    val values: Stack<Double> = Stack()
    val ops: Stack<String> = Stack()
    var i = 0

    while (i < expression.length) {
        val char = expression[i]

        when {
            char.isWhitespace() -> { i++; continue }

            char.isDigit() || char == '.' -> {
                val sb = StringBuilder()
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    sb.append(expression[i++])
                }
                values.push(sb.toString().toDouble())
                continue
            }

            char.isLetter() -> {
                val sb = StringBuilder()
                while (i < expression.length && expression[i].isLetter()) {
                    sb.append(expression[i++])
                }
                ops.push(sb.toString())
                continue
            }

            char == '(' -> {
                ops.push("(")
                i++
                continue
            }

            char == ')' -> {
                while (ops.isNotEmpty() && ops.peek() != "(") {
                    evaluateTop(ops, values)
                }
                if (ops.isNotEmpty()) ops.pop()

                if (ops.isNotEmpty() && isFunction(ops.peek())) {
                    evaluateTop(ops, values)
                }
                i++
                continue
            }

            char == '!' -> {
                if (values.isEmpty()) throw IllegalArgumentException("Invalid factorial usage")
                values.push(factorial(values.pop()))
                i++
                continue
            }

            else -> {
                val currentOp = char.toString()
                if (currentOp == "-" && (i == 0 || expression[i - 1] == '(' || isOperator(expression[i-1].toString()))) {
                    val sb = StringBuilder("-")
                    i++
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        sb.append(expression[i++])
                    }
                    values.push(sb.toString().toDouble())
                    continue
                }

                while (ops.isNotEmpty() && getPrecedence(currentOp) <= getPrecedence(ops.peek())) {
                    if (currentOp == "^" && ops.peek() == "^") break
                    evaluateTop(ops, values)
                }
                ops.push(currentOp)
                i++
            }
        }
    }

    while (ops.isNotEmpty()) {
        evaluateTop(ops, values)
    }

    if (values.size != 1) throw IllegalArgumentException("Invalid expression")
    return values.pop()
}

fun isOperator(token: String) = token in setOf("+", "-", "*", "/", "^", "%")


fun applyOp(op: String, b: Double, a: Double): Double {
    return when (op) {
        "+" -> b + a
        "-" -> b - a
        "*" -> b * a
        "/" -> {
            if (a == 0.0) throw UnsupportedOperationException("Cannot divide by zero")
            b / a
        }
        "^" -> b.pow(a)
        "%" -> b % a
        else -> throw IllegalArgumentException("Unknown operator: $op")
    }
}

fun applyFunc(func: String, a: Double): Double {
    return when (func) {
        "sin" -> sin(Math.toRadians(a))
        "cos" -> cos(Math.toRadians(a))
        "tan" -> tan(Math.toRadians(a))
        "asin" -> Math.toDegrees(asin(a))
        "acos" -> Math.toDegrees(acos(a))
        "atan" -> Math.toDegrees(atan(a))
        "log" -> log10(a)
        "ln" -> ln(a)
        "sqrt" -> {
            if (a < 0) throw IllegalArgumentException("Cannot take sqrt of negative number")
            sqrt(a)
        }
        else -> throw IllegalArgumentException("Unknown function: $func")
    }
}
