import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private var isSending = false
    private lateinit var back : ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.aichat_layout, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        messageInput = view.findViewById(R.id.input_message)
        val sendButton = view.findViewById<ImageButton>(R.id.btn_send)
        val emptyStateContainer = view.findViewById<ViewGroup>(R.id.empty_state_container)

        back = view.findViewById(R.id.back_arrow)
        adapter = AIchatAdapter(messages)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        // Handle send button click
        sendButton.setOnClickListener {
            sendMessage(emptyStateContainer)
        }
        back.setOnClickListener {
            findNavController().navigateUp()
        }

        // Handle Enter key press (optional: send on Enter)
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(emptyStateContainer)
                true
            } else {
                false
            }
        }

        return view
    }

    private fun sendMessage(emptyStateContainer: ViewGroup) {
        val message = messageInput.text.toString().trim()

        if (message.isEmpty()) {
            return
        }

        if (isSending) {
            return
        }

        // Hide empty state
        emptyStateContainer.visibility = View.GONE

        // Add user message
        addMessage(message, true)

        // Clear input but keep keyboard open
        messageInput.text.clear()

        // Keep focus on input field to maintain keyboard
        messageInput.requestFocus()

        // Send to API
        sendMessageToApi(message)
    }

    private fun addMessage(message: String, isUser: Boolean) {
        messages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(messages.size - 1)

        // Scroll to bottom smoothly
        recyclerView.post {
            recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    private fun sendMessageToApi(message: String) {
        isSending = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getAuthService(requireContext()).sendMessage(ChatRequest(message))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val botReply = response.body()?.response ?: "No response"
                        addMessage(botReply, false)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isSending = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isSending = false
                }
            }
        }
    }
}