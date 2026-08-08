package com.shravanthi.split_bill.service;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillServiceTest {

    private BillRepository billRepository;
    private BillService billService;

    // Acts as a fake in-memory "table" so our mock behaves consistently across calls
    private final Map<Long, Bill> fakeDb = new HashMap<>();
    private long nextId = 1L;

    @BeforeEach
    void setUp() {
        billRepository = mock(BillRepository.class);
        billService = new BillService(billRepository);
        fakeDb.clear();
        nextId = 1L;

        // save(): assign an id if missing, store it, return it
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            if (bill.getId() == null) {
                bill.setId(nextId++);
            }
            fakeDb.put(bill.getId(), bill);
            return bill;
        });

        // findById(): look it up in our fake table
        when(billRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.ofNullable(fakeDb.get(id));
        });
    }

    @Test
    void createBillAssignsIdAndMembers() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"));

        assertNotNull(bill.getId());
        assertEquals(3, bill.getMembers().size());
    }

    @Test
    void addItemAndSplitEvenly() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"));

        billService.addItem(bill.getId(), itemRequest("Pizza", 30.0));

        Map<String, Double> totals = billService.split(bill.getId());

        assertEquals(10.0, totals.get("Akash"), 0.001);
        assertEquals(10.0, totals.get("Priya"), 0.001);
        assertEquals(10.0, totals.get("Ravi"), 0.001);
    }

    @Test
    void addItemWithExclusion() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"));

        billService.addItem(bill.getId(), itemRequest("Pizza", 30.0));
        billService.addItem(bill.getId(), itemWithExclusion("Ice Cream", 10.0, "Akash"));

        Map<String, Double> totals = billService.split(bill.getId());

        assertEquals(10.0, totals.get("Akash"), 0.001);
        assertEquals(15.0, totals.get("Priya"), 0.001);
        assertEquals(15.0, totals.get("Ravi"), 0.001);
    }

    @Test
    void splitThrowsForUnknownBillId() {
        assertThrows(NoSuchElementException.class, () -> billService.split(999L));
    }

    @Test
    void addItemThrowsForUnknownExcludedMember() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya"));

        assertThrows(NoSuchElementException.class, () ->
                billService.addItem(bill.getId(), itemWithExclusion("Cake", 20.0, "Someone Else")));
    }

    @Test
    void summaryCombinesTotalsAcrossBills() {
        Bill bill1 = billService.createBill(billRequest("Akash", "Priya", "Ravi"));
        billService.addItem(bill1.getId(), itemRequest("Pizza", 30.0));
        billService.addItem(bill1.getId(), itemWithExclusion("Ice Cream", 10.0, "Akash"));

        Bill bill2 = billService.createBill(billRequest("Akash", "Priya", "Ravi"));
        billService.addItem(bill2.getId(), itemRequest("Coffee", 9.0));

        BillSummaryResponse summary = billService.getSummary(List.of(bill1.getId(), bill2.getId()));

        assertEquals(13.0, summary.getCombinedTotals().get("Akash"), 0.001);
        assertEquals(18.0, summary.getCombinedTotals().get("Priya"), 0.001);
        assertEquals(18.0, summary.getCombinedTotals().get("Ravi"), 0.001);
    }

    // --- helper methods ---

    private CreateBillRequest billRequest(String... names) {
        CreateBillRequest request = new CreateBillRequest();
        request.setMemberNames(List.of(names));
        return request;
    }

    private AddItemRequest itemRequest(String name, double price) {
        AddItemRequest request = new AddItemRequest();
        request.setName(name);
        request.setPrice(price);
        request.setExcludedMemberNames(List.of());
        return request;
    }

    private AddItemRequest itemWithExclusion(String name, double price, String excludedName) {
        AddItemRequest request = new AddItemRequest();
        request.setName(name);
        request.setPrice(price);
        request.setExcludedMemberNames(List.of(excludedName));
        return request;
    }
}