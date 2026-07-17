package com.hitalo.glosai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_translation")
public class LogTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "input", nullable = false, length = 255)
    private String input;

    @Column(name = "output", length = 500)
    private String output;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "is_cached", nullable = false)
    private Boolean isCached;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public LogTranslation(String input, String output, Boolean success, Boolean isCached) {
        this.input = input;
        this.output = output;
        this.success = success;
        this.isCached = isCached;
    }

    public Long getId() { return id; }

    public String getInput() { return input; }

    public String getOutput() { return output; }

    public Boolean getSuccess() { return success; }

    public Boolean getIsCached() { return isCached; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
