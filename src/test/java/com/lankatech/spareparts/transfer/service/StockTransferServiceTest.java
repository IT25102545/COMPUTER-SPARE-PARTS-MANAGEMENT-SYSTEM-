package com.lankatech.spareparts.transfer.service;

import com.lankatech.spareparts.auth.entity.Role;
import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.repository.StockTransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.lankatech.spareparts.transfer.exception.InvalidTransferStateException;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.inventory.repository.StockRepository;
import com.lankatech.spareparts.transfer.entity.StockTransferItem;
import com.lankatech.spareparts.transfer.exception.InsufficientStockException;
import com.lankatech.spareparts.transfer.dto.ReceiveTransferItemDTO;
import com.lankatech.spareparts.transfer.dto.ReceiveTransferRequestDTO;

import java.util.ArrayList;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock
    private StockTransferRepository stockTransferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockTransferService stockTransferService;

    @Test
    void approveTransfer_shouldApprovePendingTransfer() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(1L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Create Inventory Supervisor role
        Role role = new Role();
        role.setRoleName("INVENTORY_SUPERVISOR");

        // 3. Create approving user
        User supervisor = new User();
        supervisor.setUserId(2L);
        supervisor.setRole(role);

        // 4. Mock database results
        when(stockTransferRepository.findById(1L))
                .thenReturn(Optional.of(transfer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(supervisor));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 5. Call the service method
        StockTransfer result =
                stockTransferService.approveTransfer(1L, 2L);

        // 6. Check the result
        assertEquals(
                TransferStatus.APPROVED,
                result.getStatus()
        );

        assertEquals(
                supervisor,
                result.getApprovedBy()
        );

        assertNotNull(
                result.getApprovedDate()
        );
    }
    @Test
    void approveTransfer_shouldRejectNonInventorySupervisor() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(2L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Create Branch Supervisor role
        Role role = new Role();
        role.setRoleName("BRANCH_SUPERVISOR");

        // 3. Create user with wrong role
        User branchSupervisor = new User();
        branchSupervisor.setUserId(1L);
        branchSupervisor.setRole(role);

        // 4. Mock database results
        when(stockTransferRepository.findById(2L))
                .thenReturn(Optional.of(transfer));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(branchSupervisor));

        // 5. Try to approve using Branch Supervisor
        assertThrows(
                IllegalArgumentException.class,
                () -> stockTransferService.approveTransfer(2L, 1L)
        );

        // 6. Transfer must remain pending
        assertEquals(
                TransferStatus.PENDING,
                transfer.getStatus()
        );

        assertNull(
                transfer.getApprovedBy()
        );
    }
    @Test
    void dispatchTransfer_shouldRejectPendingTransfer() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(3L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(3L))
                .thenReturn(Optional.of(transfer));

        // 3. Try to dispatch a PENDING transfer
        assertThrows(
                InvalidTransferStateException.class,
                () -> stockTransferService.dispatchTransfer(3L)
        );

        // 4. Status must remain PENDING
        assertEquals(
                TransferStatus.PENDING,
                transfer.getStatus()
        );

        // 5. Dispatch date must remain empty
        assertNull(
                transfer.getDispatchDate()
        );
    }
    @Test
    void receiveTransfer_shouldRejectPendingTransfer() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(4L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(4L))
                .thenReturn(Optional.of(transfer));

        // 3. Try to receive a PENDING transfer
        assertThrows(
                InvalidTransferStateException.class,
                () -> stockTransferService.receiveTransfer(4L)
        );

        // 4. Status must remain PENDING
        assertEquals(
                TransferStatus.PENDING,
                transfer.getStatus()
        );

        // 5. Received date must remain empty
        assertNull(
                transfer.getReceivedDate()
        );
    }
    @Test
    void dispatchTransfer_shouldRejectWhenStockIsInsufficient() {

        // 1. Create source location
        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        // 2. Create spare part
        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);

        // 3. Source stock has only 5 items
        Stock sourceStock = new Stock();
        sourceStock.setStockId(1L);
        sourceStock.setLocation(sourceLocation);
        sourceStock.setSparePart(sparePart);
        sourceStock.setQuantity(5);

        // 4. Transfer requests 10 items
        StockTransferItem item = new StockTransferItem();
        item.setSparePart(sparePart);
        item.setQuantity(10);

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(5L);
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setSourceLocation(sourceLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        // 5. Mock database results
        when(stockTransferRepository.findById(5L))
                .thenReturn(Optional.of(transfer));

        when(
                stockRepository
                        .findBySparePartSparePartIdAndLocationLocationId(1L, 1L)
        ).thenReturn(Optional.of(sourceStock));

        // 6. Dispatch must be blocked
        assertThrows(
                InsufficientStockException.class,
                () -> stockTransferService.dispatchTransfer(5L)
        );

        // 7. Transfer must remain APPROVED
        assertEquals(
                TransferStatus.APPROVED,
                transfer.getStatus()
        );

        // 8. Stock must remain unchanged
        assertEquals(
                5,
                sourceStock.getQuantity()
        );

        assertNull(
                transfer.getDispatchDate()
        );
    }
    @Test
    void dispatchTransfer_shouldDeductStockAndSetDispatchedStatus() {

        // 1. Create source location
        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        // 2. Create spare part
        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);

        // 3. Source stock = 10
        Stock sourceStock = new Stock();
        sourceStock.setStockId(1L);
        sourceStock.setLocation(sourceLocation);
        sourceStock.setSparePart(sparePart);
        sourceStock.setQuantity(10);

        // 4. Transfer item quantity = 4
        StockTransferItem item = new StockTransferItem();
        item.setSparePart(sparePart);
        item.setQuantity(4);

        // 5. Create approved transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(6L);
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setSourceLocation(sourceLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        // 6. Mock database results
        when(stockTransferRepository.findById(6L))
                .thenReturn(Optional.of(transfer));

        when(
                stockRepository
                        .findBySparePartSparePartIdAndLocationLocationId(1L, 1L)
        ).thenReturn(Optional.of(sourceStock));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 7. Dispatch
        stockTransferService.dispatchTransfer(6L);

        // 8. Stock must reduce from 10 to 6
        assertEquals(
                6,
                sourceStock.getQuantity()
        );

        // 9. Transfer status must become DISPATCHED
        assertEquals(
                TransferStatus.DISPATCHED,
                transfer.getStatus()
        );

        // 10. Dispatch date must be created
        assertNotNull(
                transfer.getDispatchDate()
        );
    }

    @Test
    void receiveTransfer_shouldAddStockToDestination() {

        // 1. Create source location
        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        // 2. Create destination location
        Location destinationLocation = new Location();
        destinationLocation.setLocationId(2L);

        // 3. Create spare part
        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);

        // 4. Destination currently has 5 items
        Stock destinationStock = new Stock();
        destinationStock.setStockId(2L);
        destinationStock.setLocation(destinationLocation);
        destinationStock.setSparePart(sparePart);
        destinationStock.setQuantity(5);

        // 5. Transfer contains 3 items
        StockTransferItem item = new StockTransferItem();
        item.setSparePart(sparePart);
        item.setQuantity(3);

        // 6. Create IN_TRANSIT transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(7L);
        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setSourceLocation(sourceLocation);
        transfer.setDestinationLocation(destinationLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        // 7. Mock database results
        when(stockTransferRepository.findById(7L))
                .thenReturn(Optional.of(transfer));

        when(
                stockRepository
                        .findBySparePartSparePartIdAndLocationLocationId(1L, 2L)
        ).thenReturn(Optional.of(destinationStock));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 8. Receive transfer
        stockTransferService.receiveTransfer(7L);

        // 9. Destination stock must increase from 5 to 8
        assertEquals(
                8,
                destinationStock.getQuantity()
        );

        // 10. Status must become RECEIVED
        assertEquals(
                TransferStatus.RECEIVED,
                transfer.getStatus()
        );

        // 11. Received date must be set
        assertNotNull(
                transfer.getReceivedDate()
        );
    }
    @Test
    void rejectTransfer_shouldRejectPendingTransfer() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(8L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(8L))
                .thenReturn(Optional.of(transfer));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Reject transfer
        StockTransfer result =
                stockTransferService.rejectTransfer(8L);

        // 4. Status must become REJECTED
        assertEquals(
                TransferStatus.REJECTED,
                result.getStatus()
        );
    }
    @Test
    void cancelTransfer_shouldCancelPendingTransferWithReason() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(9L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(9L))
                .thenReturn(Optional.of(transfer));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Cancel transfer
        String reason = "Customer request cancelled";

        StockTransfer result =
                stockTransferService.cancelTransfer(9L, reason);

        // 4. Status must become CANCELLED
        assertEquals(
                TransferStatus.CANCELLED,
                result.getStatus()
        );

        // 5. Cancel reason must be saved
        assertEquals(
                reason,
                result.getCancelReason()
        );
    }
    @Test
    void cancelTransfer_shouldRejectReceivedTransfer() {

        // 1. Create a received transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(10L);
        transfer.setStatus(TransferStatus.RECEIVED);

        // 2. Mock database result
        when(stockTransferRepository.findById(10L))
                .thenReturn(Optional.of(transfer));

        // 3. Try to cancel a RECEIVED transfer
        assertThrows(
                InvalidTransferStateException.class,
                () -> stockTransferService.cancelTransfer(
                        10L,
                        "Invalid cancel attempt"
                )
        );

        // 4. Status must remain RECEIVED
        assertEquals(
                TransferStatus.RECEIVED,
                transfer.getStatus()
        );

        // 5. Cancel reason must remain empty
        assertNull(
                transfer.getCancelReason()
        );
    }
    @Test
    void rejectTransfer_shouldSaveRejectReason() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(11L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(11L))
                .thenReturn(Optional.of(transfer));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Reject transfer with reason
        String reason = "Request details are incorrect";

        StockTransfer result =
                stockTransferService.rejectTransfer(
                        11L,
                        reason
                );

        // 4. Status must become REJECTED
        assertEquals(
                TransferStatus.REJECTED,
                result.getStatus()
        );

        // 5. Reject reason must be saved
        assertEquals(
                reason,
                result.getRejectReason()
        );
    }
    @Test
    void receiveTransfer_shouldSaveDiscrepancyAndAddActualReceivedQuantity() {

        // 1. Create locations
        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        Location destinationLocation = new Location();
        destinationLocation.setLocationId(2L);

        // 2. Create spare part
        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);
        sparePart.setPartName("Kingston 16GB RAM");

        // 3. Destination stock currently has 5
        Stock destinationStock = new Stock();
        destinationStock.setStockId(2L);
        destinationStock.setLocation(destinationLocation);
        destinationStock.setSparePart(sparePart);
        destinationStock.setQuantity(5);

        // 4. Transfer item quantity = 10
        StockTransferItem item = new StockTransferItem();
        item.setTransferItemId(100L);
        item.setSparePart(sparePart);
        item.setQuantity(10);

        // 5. Create IN_TRANSIT transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(12L);
        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setSourceLocation(sourceLocation);
        transfer.setDestinationLocation(destinationLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        // 6. Create receive request
        ReceiveTransferItemDTO receiveItem =
                new ReceiveTransferItemDTO();

        receiveItem.setTransferItemId(100L);
        receiveItem.setReceivedQuantity(8);
        receiveItem.setDiscrepancyNote(
                "2 items damaged during transport"
        );

        ReceiveTransferRequestDTO request =
                new ReceiveTransferRequestDTO();

        request.setItems(
                new ArrayList<>()
        );

        request.getItems().add(
                receiveItem
        );

        // 7. Mock database results
        when(stockTransferRepository.findById(12L))
                .thenReturn(Optional.of(transfer));

        when(
                stockRepository
                        .findBySparePartSparePartIdAndLocationLocationId(
                                1L,
                                2L
                        )
        ).thenReturn(Optional.of(destinationStock));

        when(stockTransferRepository.save(any(StockTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 8. Receive transfer with discrepancy
        StockTransfer result =
                stockTransferService.receiveTransfer(
                        12L,
                        request
                );

        // 9. Destination stock must increase by ACTUAL received quantity
        assertEquals(
                13,
                destinationStock.getQuantity()
        );

        // 10. Actual received quantity must be saved
        assertEquals(
                8,
                item.getReceivedQuantity()
        );

        // 11. Discrepancy note must be saved
        assertEquals(
                "2 items damaged during transport",
                item.getDiscrepancyNote()
        );

        // 12. Transfer must become RECEIVED
        assertEquals(
                TransferStatus.RECEIVED,
                result.getStatus()
        );

        // 13. Received date must be created
        assertNotNull(
                result.getReceivedDate()
        );
    }
    @Test
    void receiveTransfer_shouldRejectDiscrepancyWithoutNote() {

        // 1. Create locations
        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        Location destinationLocation = new Location();
        destinationLocation.setLocationId(2L);

        // 2. Create spare part
        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);
        sparePart.setPartName("Kingston 16GB RAM");

        // 3. Create transfer item
        StockTransferItem item = new StockTransferItem();
        item.setTransferItemId(101L);
        item.setSparePart(sparePart);
        item.setQuantity(10);

        // 4. Create IN_TRANSIT transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(13L);
        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setSourceLocation(sourceLocation);
        transfer.setDestinationLocation(destinationLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        // 5. Actual received = 8
        // But discrepancy note is empty
        ReceiveTransferItemDTO receiveItem =
                new ReceiveTransferItemDTO();

        receiveItem.setTransferItemId(101L);
        receiveItem.setReceivedQuantity(8);
        receiveItem.setDiscrepancyNote("");

        ReceiveTransferRequestDTO request =
                new ReceiveTransferRequestDTO();

        request.setItems(new ArrayList<>());
        request.getItems().add(receiveItem);

        // 6. Mock transfer
        when(stockTransferRepository.findById(13L))
                .thenReturn(Optional.of(transfer));

        // 7. Must reject because discrepancy reason is missing
        assertThrows(
                IllegalArgumentException.class,
                () -> stockTransferService.receiveTransfer(
                        13L,
                        request
                )
        );

        // 8. Transfer must remain IN_TRANSIT
        assertEquals(
                TransferStatus.IN_TRANSIT,
                transfer.getStatus()
        );

        // 9. Received quantity must NOT be saved
        assertNull(
                item.getReceivedQuantity()
        );

        // 10. Received date must remain null
        assertNull(
                transfer.getReceivedDate()
        );
    }
    @Test
    void receiveTransfer_shouldRejectQuantityGreaterThanTransferred() {

        Location sourceLocation = new Location();
        sourceLocation.setLocationId(1L);

        Location destinationLocation = new Location();
        destinationLocation.setLocationId(2L);

        SparePart sparePart = new SparePart();
        sparePart.setSparePartId(1L);
        sparePart.setPartName("Kingston 16GB RAM");

        StockTransferItem item = new StockTransferItem();
        item.setTransferItemId(102L);
        item.setSparePart(sparePart);
        item.setQuantity(10);

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(14L);
        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setSourceLocation(sourceLocation);
        transfer.setDestinationLocation(destinationLocation);

        transfer.setItems(new ArrayList<>());
        transfer.addItem(item);

        ReceiveTransferItemDTO receiveItem =
                new ReceiveTransferItemDTO();

        receiveItem.setTransferItemId(102L);

        // Transferred quantity = 10
        // Trying to receive 12 -> invalid
        receiveItem.setReceivedQuantity(12);

        receiveItem.setDiscrepancyNote(
                "Invalid over-receive test"
        );

        ReceiveTransferRequestDTO request =
                new ReceiveTransferRequestDTO();

        request.setItems(new ArrayList<>());
        request.getItems().add(receiveItem);

        when(stockTransferRepository.findById(14L))
                .thenReturn(Optional.of(transfer));

        assertThrows(
                IllegalArgumentException.class,
                () -> stockTransferService.receiveTransfer(
                        14L,
                        request
                )
        );

        // Transfer must remain unchanged
        assertEquals(
                TransferStatus.IN_TRANSIT,
                transfer.getStatus()
        );

        assertNull(
                item.getReceivedQuantity()
        );

        assertNull(
                transfer.getReceivedDate()
        );
    }
    @Test
    void rejectTransfer_shouldRejectEmptyReason() {

        // 1. Create a pending transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferId(15L);
        transfer.setStatus(TransferStatus.PENDING);

        // 2. Mock database result
        when(stockTransferRepository.findById(15L))
                .thenReturn(Optional.of(transfer));

        // 3. Empty reject reason must be blocked
        assertThrows(
                IllegalArgumentException.class,
                () -> stockTransferService.rejectTransfer(
                        15L,
                        "   "
                )
        );

        // 4. Status must remain PENDING
        assertEquals(
                TransferStatus.PENDING,
                transfer.getStatus()
        );

        // 5. Reject reason must remain null
        assertNull(
                transfer.getRejectReason()
        );
    }
}