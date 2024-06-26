package com.aleksei.traskchat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatUser {
    private String name;
    private String email;
    private String id;
}
