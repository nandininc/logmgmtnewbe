package com.swajyot.log.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swajyot.log.model.InspectionForm;
import com.swajyot.log.repository.InspectionFormRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionFormService {

    private final InspectionFormRepository inspectionFormRepository;
    
    public List<InspectionForm> getAllForms() {
        return inspectionFormRepository.findAll();
    }
    
    public InspectionForm getFormById(Long id) {
        return inspectionFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inspection Form not found with id: " + id));
    }
    
    public List<InspectionForm> getFormsByStatus(InspectionForm.FormStatus status) {
        return inspectionFormRepository.findByStatus(status);
    }
    
    public List<InspectionForm> getFormsBySubmitter(String submittedBy) {
        return inspectionFormRepository.findBySubmittedBy(submittedBy);
    }
    
    public List<InspectionForm> getFormsByReviewer(String reviewedBy) {
        return inspectionFormRepository.findByReviewedBy(reviewedBy);
    }
    
    public List<InspectionForm> getFormsByDateRange(LocalDate startDate, LocalDate endDate) {
        return inspectionFormRepository.findByInspectionDateBetween(startDate, endDate);
    }
    
    @Transactional
    public InspectionForm createForm(InspectionForm form) {
        if (form.getStatus() == null) {
            form.setStatus(InspectionForm.FormStatus.DRAFT);
        }
        return inspectionFormRepository.save(form);
    }
    
    @Transactional
    public InspectionForm updateForm(Long id, InspectionForm updatedForm) {
        InspectionForm existingForm = getFormById(id);
        
        // Update the existing form fields with the new values
        existingForm.setDocumentNo(updatedForm.getDocumentNo());
        existingForm.setIssuanceNo(updatedForm.getIssuanceNo());
        existingForm.setIssueDate(updatedForm.getIssueDate());
        existingForm.setReviewedDate(updatedForm.getReviewedDate());
        existingForm.setPage(updatedForm.getPage());
        existingForm.setPreparedBy(updatedForm.getPreparedBy());
        existingForm.setApprovedBy(updatedForm.getApprovedBy());
        existingForm.setIssued(updatedForm.getIssued());
        existingForm.setInspectionDate(updatedForm.getInspectionDate());
        existingForm.setProduct(updatedForm.getProduct());
        existingForm.setSizeNo(updatedForm.getSizeNo());
        existingForm.setShift(updatedForm.getShift());
        existingForm.setVariant(updatedForm.getVariant());
        existingForm.setLineNo(updatedForm.getLineNo());
        existingForm.setCustomer(updatedForm.getCustomer());
        existingForm.setSampleSize(updatedForm.getSampleSize());
        existingForm.setLacquers(updatedForm.getLacquers());
        existingForm.setCharacteristics(updatedForm.getCharacteristics());
        existingForm.setQaExecutive(updatedForm.getQaExecutive());
        existingForm.setQaSignature(updatedForm.getQaSignature());
        existingForm.setProductionOperator(updatedForm.getProductionOperator());
        existingForm.setOperatorSignature(updatedForm.getOperatorSignature());
        existingForm.setFinalApprovalTime(updatedForm.getFinalApprovalTime());
        existingForm.setComments(updatedForm.getComments());
        
        return inspectionFormRepository.save(existingForm);
    }
    
    @Transactional
    public InspectionForm submitForm(Long id, String submittedBy) {
        InspectionForm form = getFormById(id);
        form.setStatus(InspectionForm.FormStatus.SUBMITTED);
        form.setSubmittedBy(submittedBy);
        form.setSubmittedAt(LocalDateTime.now());
        return inspectionFormRepository.save(form);
    }
    
    @Transactional
    public InspectionForm approveForm(Long id, String reviewedBy, String comments) {
        InspectionForm form = getFormById(id);
        form.setStatus(InspectionForm.FormStatus.APPROVED);
        form.setReviewedBy(reviewedBy);
        form.setReviewedAt(LocalDateTime.now());
        form.setComments(comments);
        return inspectionFormRepository.save(form);
    }
    
    @Transactional
    public InspectionForm rejectForm(Long id, String reviewedBy, String comments) {
        InspectionForm form = getFormById(id);
        form.setStatus(InspectionForm.FormStatus.REJECTED);
        form.setReviewedBy(reviewedBy);
        form.setReviewedAt(LocalDateTime.now());
        form.setComments(comments);
        return inspectionFormRepository.save(form);
    }
    
    @Transactional
    public void deleteForm(Long id) {
        inspectionFormRepository.deleteById(id);
    }
}
