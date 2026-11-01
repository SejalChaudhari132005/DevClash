package com.devclash.gateway.controller;

import com.devclash.common.rmi.QuizRemote;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthRestController {

    private final QuizRemote quizRemote;

    public HealthRestController(QuizRemote quizRemote) {
        this.quizRemote = quizRemote;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("gatewayStatus", "UP");
        status.put("service", "DevClash Gateway");
        status.put("timestamp", System.currentTimeMillis());

        boolean rmiReachable = false;
        try {
            rmiReachable = quizRemote.ping();
        } catch (Exception e) {
            status.put("rmiError", e.getMessage());
        }
        status.put("rmiServerStatus", rmiReachable ? "UP" : "DOWN");

        return ResponseEntity.ok(status);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(cat(1, "Programming & Languages", "programming", "Core OOP, Java, C++, Python, closures, and language idioms.", "Code"));
        categories.add(cat(2, "Data Structures & Algorithms", "dsa", "Arrays, Trees, Graphs, Dynamic Programming, and complexity analysis.", "BinaryTree"));
        categories.add(cat(3, "Database Management Systems", "dbms", "SQL, Indexing, ACID transactions, normalization, and locking protocols.", "Database"));
        categories.add(cat(4, "Operating Systems", "os", "Process scheduling, virtual memory, concurrency, deadlocks, and IPC.", "Cpu"));
        categories.add(cat(5, "Computer Networks", "networks", "TCP/IP stack, routing protocols, DNS, TLS/SSL, and sockets.", "Network"));
        categories.add(cat(6, "AI & Technology", "ai-tech", "Machine learning concepts, LLMs, neural networks, cloud, and distributed systems.", "Sparkles"));

        return ResponseEntity.ok(categories);
    }

    private Map<String, Object> cat(int id, String name, String slug, String desc, String icon) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("slug", slug);
        map.put("description", desc);
        map.put("icon", icon);
        return map;
    }
}
