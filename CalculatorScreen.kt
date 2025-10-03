package com.example.calculator

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

@Composable
fun CalculatorScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var inputFontSize by remember { mutableStateOf(48.sp) }

    LaunchedEffect(input) {
        if (input.isEmpty()) {
            inputFontSize = 48.sp
        }
    }

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
                fontSize = inputFontSize,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                softWrap = false,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        inputFontSize *= 0.95f
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = result,
                color = Color.Gray,
                fontSize = 32.sp,
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
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
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
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalculatorTheme {
        CalculatorScreen()
    }
}
