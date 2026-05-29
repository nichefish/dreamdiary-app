-- journal data embedding sync job
INSERT IGNORE INTO journal_entry_embedding_sync_job (job_key,status,phase,processed_count,total_count,created_at) VALUES ('JOURNAL_ENTRY_EMBEDDING_SYNC', 'IDLE', 'IDLE', 0, 0, NOW());
