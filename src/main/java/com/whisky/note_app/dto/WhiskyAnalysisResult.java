package com.whisky.note_app.dto;

import java.util.List;

public record WhiskyAnalysisResult(
        List<String> like,
        List<String> dislike
) {}
