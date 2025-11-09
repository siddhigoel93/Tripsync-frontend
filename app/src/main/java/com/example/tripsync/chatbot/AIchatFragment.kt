import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.chatbot.AIchatAdapter
import com.example.tripsync.chatbot.ChatMessage
import com.example.tripsync.chatbot.ChatRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIchatFragment : Fragment() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: AIchatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.aichat_layout, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        val sendButton = view.findViewById<Button>(R.id.sendButton)

        adapter = AIchatAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(message, true)
                messageInput.text.clear()
                sendMessageToApi(message)
            }
        }

        return view
    }

    private fun addMessage(message: String, isUser: Boolean) {
        messages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(messages.size - 1)
    }

    private fun sendMessageToApi(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getAuthService(requireContext()).sendMessage(ChatRequest(message))
                if (response.isSuccessful && response.body()?.success == true) {
                    val botReply = response.body()?.response ?: "No response"
                    withContext(Dispatchers.Main) { addMessage(botReply, false) }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
