package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import java.util.Stack
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorApp()
                }
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = input,
                color = Color.White,
                fontSize = 48.sp, // Reduced font size
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = result,
                color = Color.Gray,
                fontSize = 32.sp, // Reduced font size
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        val buttonRows = listOf(
            listOf("C", "DEL", "(", ")"),
            listOf("sin⁻¹", "cos⁻¹", "tan⁻¹", "%"),
            listOf("sin", "cos", "tan", "log"),
            listOf("ln", "√", "x!", "x^y"),
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ".", "=", "+")
        )

        buttonRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { buttonText ->
                    CalculatorButton(
                        text = buttonText,
                        modifier = Modifier
                            .weight(1f),
                        onClick = {
                            onButtonClick(buttonText, { input = it }, { result = it }, input)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when (text) {
        in "0".."9", "." -> Color(0xFF424242)
        "C", "DEL" -> Color(0xFFD32F2F)
        "=" -> Color(0xFF388E3C)
        else -> MaterialTheme.colorScheme.secondary
    }
    val contentColor = Color.White

    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp), // Reduced button height
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold) // Reduced font size
    }
}

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
                if (expressionResult == expressionResult.toLong().toDouble()) {
                    updateResult(expressionResult.toLong().toString())
                } else {
                    updateResult(expressionResult.toString())
                }
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
        else -> updateInput(currentInput + button)
    }
}


fun evaluateExpression(expression: String): Double {
    val tokens = expression.replace(" ", "")

    val values: Stack<Double> = Stack()
    val ops: Stack<Char> = Stack()
    var i = 0

    while (i < tokens.length) {
        val token = tokens[i]

        when {
            token.isDigit() || token == '.' -> {
                val sb = StringBuilder()
                while (i < tokens.length && (tokens[i].isDigit() || tokens[i] == '.')) {
                    sb.append(tokens[i++])
                }
                i--
                values.push(sb.toString().toDouble())
            }
            // If it's a function name
            token.isLetter() -> {
                val sb = StringBuilder()
                while (i < tokens.length && tokens[i].isLetter()) {
                    sb.append(tokens[i++])
                }
                i-- // correct index
                ops.push(sb.toString()[0]) // simplified for this example
            }
            token == '(' -> ops.push(token)
            token == ')' -> {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.pop()
            }
            isOperator(token) -> {
                while (ops.isNotEmpty() && hasPrecedence(token, ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.push(token)
            }
        }
        i++
    }

    while (ops.isNotEmpty()) {
        values.push(applyOp(ops.pop(), values.pop(), values.pop()))
    }
    return values.pop()
}

fun isOperator(c: Char) = c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' || c == '!'

fun hasPrecedence(op1: Char, op2: Char): Boolean {
    if (op2 == '(' || op2 == ')') return false
    if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false
    if (op1 == '^' && (op2 == '*' || op2 == '/' || op2 == '+' || op2 == '-')) return false
    if (op1 == '!' && op2 != '^') return false
    return true
}

fun applyOp(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> {
            if (b == 0.0) throw UnsupportedOperationException("Cannot divide by zero")
            a / b
        }
        '^' -> a.pow(b)
        '%' -> a % b
        else -> 0.0
    }
}


fun applyFunc(func: Char, a: Double): Double {
    return when (func) {
        's' -> sin(Math.toRadians(a)) // Assuming sin is for 's'
        'c' -> cos(Math.toRadians(a)) // cos for 'c'
        't' -> tan(Math.toRadians(a)) // tan for 't'
        else -> 0.0
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalculatorTheme {
        CalculatorApp()
    }
}

