package com.example.onlinetalaba.service;

import com.example.onlinetalaba.config.LiveKitProps;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LiveKitTokenService {

    private final LiveKitProps liveKitProps;

    public String createParticipantToken(String roomName, String identity, String participantName) {
        AccessToken token = new AccessToken(liveKitProps.apiKey(), liveKitProps.apiSecret());
        token.setIdentity(identity);
        token.setName(participantName);
        token.setTtl(1000L * 60 * 60 * 2); // 2 soat

        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName)
        );

        return token.toJwt();
    }

    public String serverUrl() {
        return liveKitProps.url();
    }
}