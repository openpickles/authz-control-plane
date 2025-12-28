package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content is required"));
        }

        try {
            evaluationService.validatePolicy(content);
            return ResponseEntity.ok(Map.of("status", "valid"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "invalid", "error", e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> test(@RequestBody TestRequest request) {
        try {
            Map<String, Object> result = evaluationService.testPolicy(
                    request.getPolicyContent(),
                    request.getPolicyId(),
                    request.getInput(),
                    request.getData());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // DTO
    public static class TestRequest {
        private String policyContent;
        private String policyId;
        private Map<String, Object> input;
        private Map<String, Object> data;

        public String getPolicyContent() {
            return policyContent;
        }

        public void setPolicyContent(String policyContent) {
            this.policyContent = policyContent;
        }

        public String getPolicyId() {
            return policyId;
        }

        public void setPolicyId(String policyId) {
            this.policyId = policyId;
        }

        public Map<String, Object> getInput() {
            return input;
        }

        public void setInput(Map<String, Object> input) {
            this.input = input;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
