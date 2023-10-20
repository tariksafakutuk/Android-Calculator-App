package com.example.calculatorapp

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import com.example.calculatorapp.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val inputList = ArrayList<String>()
    private var inputAction = ""
    private var decimalControl = false
    private var actionControl = false
    private var numberControl = false
    private var resultControl = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editTextInput.showSoftInputOnFocus = false

        val appCompatButtonList = arrayListOf(
            binding.buttonZero,
            binding.buttonOne,
            binding.buttonTwo,
            binding.buttonThree,
            binding.buttonFour,
            binding.buttonFive,
            binding.buttonSix,
            binding.buttonSeven,
            binding.buttonEight,
            binding.buttonNine,
            binding.buttonDecimal
        )

        val floatingActionButtonList = arrayListOf(
            binding.buttonPercent,
            binding.buttonMultiply,
            binding.buttonSubstract,
            binding.buttonAdd
        )

        appCompatButtonList.forEach {
            setNumberButton(it)
        }

        floatingActionButtonList.forEach {
            setActionButton(it)
        }

        binding.buttonDivide.setOnClickListener {
            addActionToList("÷")
        }

        binding.buttonClear.setOnClickListener {
            resetCalculator()
        }

        binding.buttonBackspace.setOnClickListener {
            deleteLastItem()
        }

        binding.buttonEquals.setOnClickListener {
            calculateResult()
        }
    }

    private fun setNumberButton(button: AppCompatButton) {
        button.setOnClickListener {
            addNumberToList(button.text.toString())
        }
    }

    private fun setActionButton(button: FloatingActionButton) {
        when (button) {
            binding.buttonPercent -> button.setOnClickListener { addActionToList("%") }
            binding.buttonMultiply -> button.setOnClickListener { addActionToList("×") }
            binding.buttonSubstract -> button.setOnClickListener { addActionToList("-") }
            binding.buttonAdd -> button.setOnClickListener { addActionToList("+") }
        }
    }

    private fun addNumberToList(str: String) {
        if (resultControl) {
            resetCalculator()
        }

        if (str == "." && !decimalControl && numberControl) {
            decimalControl = true
            inputList.add(str)
        } else if (str != ".") {
            if (inputList.size != 0 && (decimalControl || numberControl)) {
                val lastItem = inputList.last()
                inputList.removeLast()
                inputList.add(lastItem + str)
            } else {
                inputList.add(str)
            }
            numberControl = true
            actionControl = true
        }

        inputAction = ""
        printEditTextInput()
    }

    private fun addActionToList(str: String) {
        if (resultControl) {
            binding.editTextInput.setText(inputList[0])
            binding.editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
            binding.editTextInput.isCursorVisible = true
            binding.textViewResult.text = ""
            resultControl = false
            actionControl = true
            numberControl = true
        }

        if (numberControl) {
            numberControl = false
            decimalControl = false
        }

        if ((inputAction == "÷" || inputAction == "×") && str == "-" && !decimalControl) {
            inputList.removeLast()
            inputAction += str
            inputList.add(inputAction)
            decimalControl = false
        } else if ((inputAction == "÷-" || inputAction == "×-") && !decimalControl) {
            if ((str == "+" || str == "÷" || str == "×")) {
                inputAction = str
                inputList.removeLast()
                inputList.add(inputAction)
            }
            decimalControl = false
        } else if (actionControl && !decimalControl) {
            if ((inputAction != "" && inputAction != "%") || (inputAction == "%" && str == "%")) {
                inputList.removeLast()
            }
            decimalControl = false
            inputAction = str
            inputList.add(inputAction)
        }

        printEditTextInput()
    }

    private fun printEditTextInput() {
        var inputText = ""

        inputList.forEach {
            inputText += if (it == "÷" || it == "×" || it == "-" || it == "+" || it == "÷-" || it == "×-") {
                "<font color=\"#00A6CE\">$it</font>"
            } else {
                it
            }
        }
        @Suppress("DEPRECATION")
        binding.editTextInput.setText(Html.fromHtml(inputText))
        binding.editTextInput.setSelection(binding.editTextInput.length())
    }

    private fun resetCalculator() {
        inputAction = ""
        decimalControl = false
        numberControl = false
        actionControl = false
        resultControl = false
        binding.textViewResult.text = ""
        binding.editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
        binding.editTextInput.isCursorVisible = true
        inputList.clear()
        printEditTextInput()
    }

    private fun deleteLastItem() {
        if (resultControl) {
            binding.textViewResult.text = ""
            resetCalculator()
            return
        }

        if (inputList.size != 0) {
            inputList.removeAt(binding.editTextInput.selectionStart - 1)
            printEditTextInput()
        }
    }

    private fun calculateResult() {
        if (resultControl) {
            return
        }

        createDecimalNumber()
        calculatePercent()
        calculateDivideAndMultiply()
        calculateSubstractAndAdd()

        val result = inputList[0].toDouble()
        if (result % 1 == 0.0) {
            inputList[0] = result.toInt().toString()
            binding.textViewResult.text = "=${result.toInt().toString()}"
        } else {
            binding.textViewResult.text = result.toString()
        }

        binding.editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        binding.editTextInput.isCursorVisible = false
        resultControl = true
    }

    private fun calculateSubstractAndAdd() {
        var i = 0
        while (i < inputList.size) {
            if (inputList[i] == "-" || inputList[i] == "+") {
                if (inputList[i] == "-") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() - inputList[i + 1].toDouble()).toString()
                } else if (inputList[i] == "+") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() + inputList[i + 1].toDouble()).toString()
                }
                inputList.removeAt(i + 1)
                inputList.removeAt(i - 1)
                i--
            } else {
                i++
            }
        }
    }

    private fun calculateDivideAndMultiply() {
        var i = 0
        while (i < inputList.size) {
            if ((inputList[i] == "×" || inputList[i] == "÷" || inputList[i] == "×-" || inputList[i] == "÷-")) {
                if (inputList[i] == "×") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() * inputList[i + 1].toDouble()).toString()
                } else if (inputList[i] == "÷") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() / inputList[i + 1].toDouble()).toString()
                } else if (inputList[i] == "×-") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() * -inputList[i + 1].toDouble()).toString()
                } else if (inputList[i] == "÷-") {
                    inputList[i] =
                        (inputList[i - 1].toDouble() / -inputList[i + 1].toDouble()).toString()
                }
                inputList.removeAt(i + 1)
                inputList.removeAt(i - 1)
                i--
            } else {
                i++
            }
        }
    }

    private fun calculatePercent() {
        var i = 0
        while (i < inputList.size) {
            if (inputList[i] == "%") {
                inputList[i] = (inputList[i - 1].toDouble() / 100).toString()
                inputList.removeAt(i - 1)
                i--
            } else {
                i++
            }
        }
    }

    private fun createDecimalNumber() {
        var i = 0
        while (i < inputList.size) {
            if (inputList[i].first() == '.') {
                if (inputList[i - 1].toIntOrNull() != null) {
                    inputList[i] = inputList[i - 1] + inputList[i]
                    inputList.removeAt(i - 1)
                    i--
                }
            } else {
                i++
            }
        }
    }
}