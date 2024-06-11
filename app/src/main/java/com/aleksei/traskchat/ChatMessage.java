package com.aleksei.traskchat;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessage {
    private String message;
    private String name;
    private String imageUrl;
    private String time;

}
