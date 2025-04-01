package com.swajyot.log.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swajyot.log.model.InspectionForm;
import com.swajyot.log.service.InspectionFormService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inspection-forms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InspectionFormController {

    private final InspectionFormService inspectionFormService;

    @GetMapping
    public ResponseEntity<List<InspectionForm>> getAllForms() {
        return ResponseEntity.ok(inspectionFormService.getAllForms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionForm> getFormById(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionFormService.getFormById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InspectionForm>> getFormsByStatus(@PathVariable String status) {
        try {
            // Explicitly provide the enum class as the first parameter
            InspectionForm.FormStatus formStatus = Enum.valueOf(InspectionForm.FormStatus.class, status.toUpperCase());
            return ResponseEntity.ok(inspectionFormService.getFormsByStatus(formStatus));
        } catch (IllegalArgumentException e) {
            // Handle invalid status values
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/submitter/{submitter}")
    public ResponseEntity<List<InspectionForm>> getFormsBySubmitter(@PathVariable String submitter) {
        return ResponseEntity.ok(inspectionFormService.getFormsBySubmitter(submitter));
    }

    @GetMapping("/reviewer/{reviewer}")
    public ResponseEntity<List<InspectionForm>> getFormsByReviewer(@PathVariable String reviewer) {
        return ResponseEntity.ok(inspectionFormService.getFormsByReviewer(reviewer));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<InspectionForm>> getFormsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(inspectionFormService.getFormsByDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<InspectionForm> createForm(@RequestBody InspectionForm form) {
        return new ResponseEntity<>(inspectionFormService.createForm(form), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InspectionForm> updateForm(@PathVariable Long id, @RequestBody InspectionForm form) {
        return ResponseEntity.ok(inspectionFormService.updateForm(id, form));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<InspectionForm> submitForm(@PathVariable Long id, @RequestParam String submittedBy) {
        return ResponseEntity.ok(inspectionFormService.submitForm(id, submittedBy));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<InspectionForm> approveForm(
            @PathVariable Long id,
            @RequestParam String reviewedBy,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(inspectionFormService.approveForm(id, reviewedBy, comments));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<InspectionForm> rejectForm(
            @PathVariable Long id,
            @RequestParam String reviewedBy,
            @RequestParam String comments) {
        return ResponseEntity.ok(inspectionFormService.rejectForm(id, reviewedBy, comments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        inspectionFormService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }
}
