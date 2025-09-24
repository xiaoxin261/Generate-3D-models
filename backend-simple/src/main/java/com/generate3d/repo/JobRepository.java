package com.generate3d.repo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobRepository {
    public static class JobRecord {
        public String taskId;
        public String mode; // TEXT
        public String prompt;
        public String status; // PENDING/RUNNING/SUCCEEDED/FAILED
        public int progress;
        public String errorMsg;
        public Map<String, String> urls = new ConcurrentHashMap<>();
        public long createdAt = System.currentTimeMillis();
        public Long finishedAt;
    }

    private final Map<String, JobRecord> store = new ConcurrentHashMap<>();

    public void save(JobRecord r) { store.put(r.taskId, r); }
    public JobRecord find(String taskId) { return store.get(taskId); }
}


