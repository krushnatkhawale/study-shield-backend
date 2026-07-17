package com.studyshield.content.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Option shape aligned with mobile assets:
 * {@code { "id": "a", "text": "...", "imageUrl": null }}.
 * <p>
 * For FITB, options may be empty; {@code correctAnswers} holds accepted text values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOption {

    private String id;
    private String text;
    private String imageUrl;

    public QuestionOption() {}

    public QuestionOption(String id, String text, String imageUrl) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
