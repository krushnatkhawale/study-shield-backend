package com.studyshield.user.controller;

import com.studyshield.user.dto.ChildProfileRequest;
import com.studyshield.user.dto.ChildProfileResponse;
import com.studyshield.user.dto.auth.StudentRequest;
import com.studyshield.user.dto.auth.StudentResponse;
import com.studyshield.user.service.ChildProfileService;
import com.studyshield.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final ChildProfileService childProfileService;
    private final UserService userService;

    public StudentController(ChildProfileService childProfileService, UserService userService) {
        this.childProfileService = childProfileService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getCurrentUserStudents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        List<ChildProfileResponse> profiles = childProfileService.getByUserId(Long.parseLong(userId));
        List<StudentResponse> students = profiles.stream().map(StudentResponse::fromChildProfile).toList();
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@RequestBody StudentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();

        int age = 0;
        if (request.birthYear() != null) {
            age = Year.now().getValue() - request.birthYear();
        }

        ChildProfileRequest childRequest = new ChildProfileRequest(
                request.name(),
                age,
                Long.parseLong(userId),
                null,
                null,
                request.gender(),
                request.birthYear(),
                request.studentClass(),
                true);

        ChildProfileResponse saved = childProfileService.create(childRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(StudentResponse.fromChildProfile(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @RequestBody StudentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();

        ChildProfileResponse existing = childProfileService.getById(id);

        int age = existing.age();
        if (request.birthYear() != null) {
            age = Year.now().getValue() - request.birthYear();
        }

        ChildProfileRequest childRequest = new ChildProfileRequest(
                request.name() != null ? request.name() : existing.name(),
                age,
                Long.parseLong(userId),
                existing.boardId(),
                existing.classGradeId(),
                request.gender() != null ? request.gender() : existing.gender(),
                request.birthYear() != null ? request.birthYear() : existing.birthYear(),
                request.studentClass() != null ? request.studentClass() : existing.studentClass(),
                existing.active());

        ChildProfileResponse updated = childProfileService.update(id, childRequest);
        return ResponseEntity.ok(StudentResponse.fromChildProfile(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        childProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
