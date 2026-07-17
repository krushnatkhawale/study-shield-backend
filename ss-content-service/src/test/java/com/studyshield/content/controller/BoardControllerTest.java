package com.studyshield.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.content.dto.BoardRequest;
import com.studyshield.content.entity.Board;
import com.studyshield.content.repository.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        boardRepository.deleteAll();
    }

    @Test
    void shouldCreateBoard() throws Exception {
        BoardRequest request = new BoardRequest("CBSE", "CBSE", "Central Board of Secondary Education", true);

        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("CBSE"))
                .andExpect(jsonPath("$.code").value("CBSE"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void shouldGetBoardById() throws Exception {
        Board board = boardRepository.save(new Board("ICSE", "ICSE", "Indian Certificate of Secondary Education", true));

        mockMvc.perform(get("/api/v1/boards/{id}", board.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ICSE"))
                .andExpect(jsonPath("$.code").value("ICSE"));
    }

    @Test
    void shouldGetAllBoards() throws Exception {
        boardRepository.save(new Board("CBSE", "CBSE", null, true));
        boardRepository.save(new Board("ICSE", "ICSE", null, true));

        mockMvc.perform(get("/api/v1/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldUpdateBoard() throws Exception {
        Board board = boardRepository.save(new Board("CBSE", "CBSE", null, true));

        BoardRequest request = new BoardRequest("CBSE Updated", "CBSE", "Updated description", true);

        mockMvc.perform(put("/api/v1/boards/{id}", board.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CBSE Updated"));
    }

    @Test
    void shouldDeleteBoard() throws Exception {
        Board board = boardRepository.save(new Board("CBSE", "CBSE", null, true));

        mockMvc.perform(delete("/api/v1/boards/{id}", board.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenBoardNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/boards/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenBoardNameMissing() throws Exception {
        BoardRequest request = new BoardRequest(null, "TEST", null, true);

        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
