package com.lankatech.spareparts.customer.service;
import com.lankatech.spareparts.customer.entity.*;
import com.lankatech.spareparts.customer.repository.ComplaintRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ComplaintWorkflowTest {
    @Test void enforcesEveryTransitionAndPreservesResolutionDate() {
        ComplaintStatus[] expected = {ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED};
        ComplaintStatus[] next = {ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED};
        for (ComplaintStatus status : ComplaintStatus.values()) {
            for (int i = 0; i < expected.length; i++) {
                ComplaintRepository repository = mock(ComplaintRepository.class);
                CustomerService service = new CustomerService(null, repository, null, null, null, null);
                Complaint complaint = new Complaint(); complaint.setStatus(status);
                LocalDateTime previous = LocalDateTime.of(2026, 1, 1, 12, 0);
                if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) complaint.setResolvedAt(previous);
                when(repository.findForUpdate(1L)).thenReturn(Optional.of(complaint));
                final int action = i;
                Runnable call = () -> { switch (action) {
                    case 0 -> service.startComplaint(1L);
                    case 1 -> service.resolve(1L);
                    default -> service.closeComplaint(1L);
                }};
                if (status == expected[i]) {
                    call.run(); assertEquals(next[i], complaint.getStatus()); verify(repository).save(complaint);
                    if (i == 0) assertNull(complaint.getResolvedAt());
                    if (i == 1) assertNotNull(complaint.getResolvedAt());
                    if (i == 2) assertEquals(previous, complaint.getResolvedAt());
                } else {
                    assertThrows(IllegalStateException.class, call::run);
                    assertEquals(status, complaint.getStatus()); verify(repository, never()).save(any());
                }
            }
        }
    }
    @Test void missingComplaintIsRejected() {
        ComplaintRepository repository = mock(ComplaintRepository.class);
        when(repository.findForUpdate(1L)).thenReturn(Optional.empty());
        CustomerService service = new CustomerService(null, repository, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.startComplaint(1L));
        verify(repository, never()).save(any());
    }
}
