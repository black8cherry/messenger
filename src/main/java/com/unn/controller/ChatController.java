package com.unn.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.unn.dto.ChatRequest;
import com.unn.model.Chat;
import com.unn.service.ChatService;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService service;

    @MessageMapping("/chat")
    @SendTo("/topic/chats")
    public List<Chat> createChat(ChatRequest request) {
        Chat newChat = new Chat();
        newChat.setName(request.getChatName());
        newChat.generateLink();
        newChat.setCreator(request.getUsername());

        service.addChat(newChat);

        return service.allChats();
    }

    @MessageMapping("/chats")
    @SendTo("/topic/chats")
    public List<Chat> getAllChats() {
        return service.allChats();
    }

    @MessageMapping("/chat/{name}/message")
    @SendTo("/topic/chat/{name}")
    public List<String> message(@DestinationVariable("name") String chatName, String text) {
        Optional<Chat> chat = service.findChat(chatName);
        String message = service.isCursed(text) ? ChatService.CURSED_MESSAGE : text;
        if (chat.isPresent()) {
            chat.get().newMessage(message);
        }

        return chat.isPresent() ? chat.get().messageHistory(50) : new ArrayList<>();
    }

    @MessageMapping("/chat/{name}/messages")
    @SendTo("/topic/chat/{name}")
    public List<String> messages(@DestinationVariable("name") String chatName) {
        Optional<Chat> chat = service.findChat(chatName);

        return chat.isPresent() ? chat.get().messageHistory() : new ArrayList<>();
    }
}
