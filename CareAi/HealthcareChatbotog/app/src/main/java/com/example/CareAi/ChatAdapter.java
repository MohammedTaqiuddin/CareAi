package com.example.CareAi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the message layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override

    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.isUserMessage()) {
            // Align user message to the right
            holder.userMessage.setText(message.getText());
            holder.userMessage.setVisibility(View.VISIBLE);
            holder.userMessage.setGravity(Gravity.END); // Align text to the right
            holder.aiMessage.setVisibility(View.GONE);
        } else {
            // Align AI message to the left
            holder.aiMessage.setText(message.getText());
            holder.aiMessage.setVisibility(View.VISIBLE);
            holder.aiMessage.setGravity(Gravity.START); // Align text to the left
            holder.userMessage.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, aiMessage;

        MessageViewHolder(View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
            aiMessage = itemView.findViewById(R.id.aiMessage);
        }
    }
}

