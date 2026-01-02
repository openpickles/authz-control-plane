package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.event.WebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    @Autowired
    private WebSocketEventListener webSocketEventListener;

    @GetMapping("/clients")
    public Map<String, Object> getClientStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeConnections", webSocketEventListener.getActiveConnectionCount());
        return stats;
    }
}
