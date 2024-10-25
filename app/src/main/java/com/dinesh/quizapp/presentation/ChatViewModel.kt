package com.dinesh.quizapp.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinesh.quizapp.utils.Constants
import com.dinesh.quizapp.data.model.MessageModel
import com.dinesh.quizapp.data.model.Question
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val questionList by lazy {
        mutableStateListOf<Question>()
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Constants.apiKey
    )

    fun sendMessage(question : String){
        viewModelScope.launch {

            try{
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role){ text(it.message) }
                    }.toList()
                )

                messageList.add(MessageModel(question,"user"))
                messageList.add(MessageModel("Typing....","model"))

                val response = chat.sendMessage(question)
                val responseText = response.text.toString()
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(),"model"))

                // Extract and parse questions from the response
                parseQuestionsFromText(responseText)

                Log.i("Message", Gson().toJson(messageList))
            }catch (e : Exception){
                messageList.removeLast()
                messageList.add(MessageModel("Error : "+e.message.toString(),"model"))
            }


        }
    }

    fun parseQuestionsFromText(responseText: String) {
        try {
            // Parse the responseText as a JSON array
            val jsonArray = JsonParser.parseString(responseText).asJsonArray

            jsonArray.forEach { jsonElement ->
                val jsonObject = jsonElement.asJsonObject

                // Extract question text
                val questionText = jsonObject.getAsJsonPrimitive("Question 1")?.asString ?: ""

                // Extract options
                val optionsJsonArray = jsonObject.getAsJsonArray("options")
                val optionsList = optionsJsonArray.map { it.asString }

                // Extract correct answer
                val correctAnswer = jsonObject.getAsJsonPrimitive("Correct Answer")?.asString ?: ""

                // Add the parsed data to questionList
                val question = Question(
                    questionText = questionText,
                    options = optionsList,
                    answer = correctAnswer
                )
                questionList.add(question)
            }

            Log.i("Message4", "Parsed Questions: ${Gson().toJson(questionList)}")
        } catch (e: Exception) {
            Log.e("ParsingError", "Error parsing string to JSON: ${e.message}")
        }
    }

}