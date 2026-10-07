package com.lankatech.spareparts.customer.service;
import com.lankatech.spareparts.customer.entity.Reservation;
import com.lankatech.spareparts.customer.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReservationWorkflowTest {
    @Test void transitionMatrix() {
        String[] statuses = {"PENDING", "ACTIVE", "REJECTED", "CANCELLED", "COMPLETED"};
        String[] required = {"PENDING", "PENDING", "ACTIVE", "ACTIVE"};
        String[] next = {"ACTIVE", "REJECTED", "CANCELLED", "COMPLETED"};
        for (String status : statuses) {
            for (int action = 0; action < required.length; action++) {
                ReservationRepository repository = mock(ReservationRepository.class);
                com.lankatech.spareparts.inventory.repository.StockRepository stocks = mock(com.lankatech.spareparts.inventory.repository.StockRepository.class);
                CustomerService service = new CustomerService(null, null, repository, null, stocks, null);
                Reservation row = new Reservation(); row.setStatus(status); row.setQuantity(2);
                var part = new com.lankatech.spareparts.inventory.entity.SparePart(); part.setSparePartId(1L); row.setSparePart(part);
                var location = new com.lankatech.spareparts.common.entity.Location(); location.setLocationId(1L); row.setLocation(location);
                var stock = new com.lankatech.spareparts.inventory.entity.Stock(); stock.setQuantity(10);
                when(stocks.findForUpdate(1L, 1L)).thenReturn(Optional.of(stock));
                when(repository.findForUpdate(1L)).thenReturn(Optional.of(row));
                final int choice = action;
                Runnable call = () -> { switch(choice) {
                    case 0 -> service.acceptReservation(1L);
                    case 1 -> service.rejectReservation(1L);
                    case 2 -> service.cancelReservation(1L);
                    default -> service.completeReservation(1L);
                }};
                if (required[action].equals(status)) {
                    call.run(); assertEquals(next[action], row.getStatus()); verify(repository).save(row);
                } else {
                    assertThrows(IllegalStateException.class, call::run);
                    assertEquals(status, row.getStatus()); verify(repository, never()).save(any());
                }
            }
        }
    }
    @Test void defaultsToPending() {
        Reservation row = new Reservation(); row.onCreate(); assertEquals("PENDING", row.getStatus());
        row.setStatus("ACTIVE"); row.onCreate(); assertEquals("ACTIVE", row.getStatus());
    }
    @Test void missingReservationCannotBeAccepted() {
        ReservationRepository repository = mock(ReservationRepository.class);
        when(repository.findForUpdate(99L)).thenReturn(Optional.empty());
        CustomerService service = new CustomerService(null, null, repository, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.acceptReservation(99L));
        verify(repository, never()).save(any());
    }
}
