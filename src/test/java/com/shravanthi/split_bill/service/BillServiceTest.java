package com.shravanthi.split_bill.service;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.User;
import com.shravanthi.split_bill.repository.BillRepository;
import com.shravanthi.split_bill.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillServiceTest {

    private BillRepository billRepository;
    private UserRepository userRepository;
    private BillService billService;

    private final Map<Long, Bill> fakeDb = new HashMap<>();
    private long nextId = 1L;
    private static final String USERNAME = "akash123";

    @BeforeEach
    void setUp() {
        billRepository = mock(BillRepository.class);
        userRepository = mock(UserRepository.class);
        billService = new BillService(billRepository, userRepository);
        fakeDb.clear();
        nextId = 1L;

        User fakeUser = new User(USERNAME, "hashed-password-not-relevant-here");
        fakeUser.setId(1L);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(fakeUser));

        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            if (bill.getId() == null) {
                bill.setId(nextId++);
            }
            fakeDb.put(bill.getId(), bill);
            return bill;
        });

        when(billRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.ofNullable(fakeDb.get(id));
        });
    }

    @Test
    void createBillAssignsIdAndMembers() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"), USERNAME);

        assertNotNull(bill.getId());
        assertEquals(3, bill.getMembers().size());
    }

    @Test
    void addItemAndSplitEvenly() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"), USERNAME);

        billService.addItem(bill.getId(), itemRequest("Pizza", 30.0), USERNAME);

        Map<String, Double> totals = billService.split(bill.getId(), USERNAME);

        assertEquals(10.0, totals.get("Akash"), 0.001);
        assertEquals(10.0, totals.get("Priya"), 0.001);
        assertEquals(10.0, totals.get("Ravi"), 0.001);
    }

    @Test
    void addItemWithExclusion() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya", "Ravi"), USERNAME);

        billService.addItem(bill.getId(), itemRequest("Pizza", 30.0), USERNAME);
        billService.addItem(bill.getId(), itemWithExclusion("Ice Cream", 10.0, "Akash"), USERNAME);

        Map<String, Double> totals = billService.split(bill.getId(), USERNAME);

        assertEquals(10.0, totals.get("Akash"), 0.001);
        assertEquals(15.0, totals.get("Priya"), 0.001);
        assertEquals(15.0, totals.get("Ravi"), 0.001);
    }

    @Test
    void splitThrowsForUnknownBillId() {
        assertThrows(NoSuchElementException.class, () -> billService.split(999L, USERNAME));
    }

    @Test
    void addItemThrowsForUnknownExcludedMember() {
        Bill bill = billService.createBill(billRequest("Akash", "Priya"), USERNAME);

        assertThrows(NoSuchElementException.class, () ->
                billService.addItem(bill.getId(), itemWithExclusion("Cake", 20.0, "Someone Else"), USERNAME));
    }

    @Test
    void summaryCombinesTotalsAcrossBills() {
        Bill bill1 = billService.createBill(billRequest("Akash", "Priya", "Ravi"), USERNAME);
        billService.addItem(bill1.getId(), itemRequest("Pizza", 30.0), USERNAME);
        billService.addItem(bill1.getId(), itemWithExclusion("Ice Cream", 10.0, "Akash"), USERNAME);

        Bill bill2 = billService.createBill(billRequest("Akash", "Priya", "Ravi"), USERNAME);
        billService.addItem(bill2.getId(), itemRequest("Coffee", 9.0), USERNAME);

        BillSummaryResponse summary = billService.getSummary(List.of(bill1.getId(), bill2.getId()), USERNAME);

        assertEquals(13.0, summary.getCombinedTotals().get("Akash"), 0.001);
        assertEquals(18.0, summary.getCombinedTotals().get("Priya"), 0.001);
        assertEquals(18.0, summary.getCombinedTotals().get("Ravi"), 0.001);
    }

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