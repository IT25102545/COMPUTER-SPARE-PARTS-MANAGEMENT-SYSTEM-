package com.lankatech.spareparts.customer.service;
import com.lankatech.spareparts.customer.entity.Reservation;
import com.lankatech.spareparts.customer.repository.ReservationRepository;
import com.lankatech.spareparts.inventory.repository.StockRepository;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.common.entity.Location;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReservationStockTest {
    final ReservationRepository reservations = mock(ReservationRepository.class);
    final StockRepository stocks = mock(StockRepository.class);
    final CustomerService service = new CustomerService(null, null, reservations, null, stocks, null);
    final Reservation row = new Reservation();
    final Stock stock = new Stock();
    ReservationStockTest() {
        var part = new SparePart(); part.setSparePartId(1L);
        var location = new Location(); location.setLocationId(2L);
        row.setSparePart(part); row.setLocation(location); row.setQuantity(4); row.setStatus("PENDING");
        stock.setQuantity(10);
        when(reservations.findForUpdate(3L)).thenReturn(Optional.of(row));
        when(stocks.findForUpdate(1L, 2L)).thenReturn(Optional.of(stock));
    }
    @Test void acceptanceDeductsAndCompletionDoesNotDeductAgain() {
        service.acceptReservation(3L); assertEquals(6, stock.getQuantity()); assertTrue(row.getStockDeducted());
        service.completeReservation(3L); assertEquals(6, stock.getQuantity()); assertEquals("COMPLETED", row.getStatus());
        service.reconcileReservationStock(3L); assertEquals(6, stock.getQuantity());
        verify(stocks, times(1)).save(stock);
    }
    @Test void cancelRestoresExactlyOnce() {
        service.acceptReservation(3L); service.cancelReservation(3L);
        assertEquals(10, stock.getQuantity()); assertFalse(row.getStockDeducted());
        assertThrows(IllegalStateException.class, () -> service.cancelReservation(3L)); assertEquals(10, stock.getQuantity());
    }
    @Test void insufficientStockLeavesReservationPending() {
        stock.setQuantity(3);
        assertThrows(IllegalStateException.class, () -> service.acceptReservation(3L));
        assertEquals(3, stock.getQuantity()); assertEquals("PENDING", row.getStatus()); verify(stocks, never()).save(any());
    }
    @Test void rejectDoesNotChangeStock() {
        service.rejectReservation(3L); assertEquals(10, stock.getQuantity()); verifyNoInteractions(stocks);
    }
    @Test void oldCancellationDoesNotInventStock() {
        row.setStatus("ACTIVE"); row.setStockDeducted(null); service.cancelReservation(3L);
        assertEquals(10, stock.getQuantity()); verifyNoInteractions(stocks);
    }
    @Test void oldCompletedReservationCanBeReconciledOnce() {
        row.setStatus("COMPLETED"); row.setStockDeducted(null);
        service.reconcileReservationStock(3L); service.reconcileReservationStock(3L);
        assertEquals(6, stock.getQuantity()); assertEquals("COMPLETED", row.getStatus()); verify(stocks, times(1)).save(stock);
    }
}
