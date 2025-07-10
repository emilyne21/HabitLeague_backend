package com.example.habitleague.challenge.model;

import lombok.Getter;

@Getter
public enum ChallengeCategory {
    MINDFULNESS("Mindfulness", "🧘"),
    FITNESS("Fitness", "💪"), 
    PRODUCTIVITY("Productivity", "📈"),
    LIFESTYLE("Lifestyle", "🌟"),
    HEALTH("Health", "❤️"),
    CODING("Coding", "💻"),
    READING("Reading", "📚"),
    FINANCE("Finance", "💰"),
    LEARNING("Learning", "🎓"),
    WRITING("Writing", "✍️"),
    CREATIVITY("Creativity", "🎨");
    
    private final String displayName;
    private final String icon;
    
    ChallengeCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
} 