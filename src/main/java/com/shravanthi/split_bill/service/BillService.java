package com.shravanthi.split_bill.service;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Item;
import com.shravanthi.split_bill.model.Member;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final Map<String, Bill> billStore = new ConcurrentHashMap<>();
    private final BillSplitter billSplitter = new BillSplitter();

    public Bill createBill(CreateBillRequest request) {
        Bill bill = new Bill();
        bill.setId(UUID.randomUUID().toString());
        bill.setName(request.getName() != null && !request.getName().isBlank()
            ? request.getName()
            : "Untitled Bill");

        List<Member> members = request.getMemberNames().stream()
                .map(Member::new)
                .collect(Collectors.toList());
        bill.setMembers(members);

        billStore.put(bill.getId(), bill);
        return bill;
    }

    public Bill getBill(String billId) {
        Bill bill = billStore.get(billId);
        if (bill == null) {
            throw new NoSuchElementException("No bill found with id " + billId);
        }
        return bill;
    }

    public List<Bill> getAllBills() {
        return new ArrayList<>(billStore.values());
    }

    public Bill addItem(String billId, AddItemRequest request) {
        Bill bill = getBill(billId);

        Set<Member> excluded = new HashSet<>();
        if (request.getExcludedMemberNames() != null) {
            for (String name : request.getExcludedMemberNames()) {
                Member match = bill.getMembers().stream()
                        .filter(m -> m.getName().equals(name))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException(
                                "Member '" + name + "' not found on bill " + billId));
                excluded.add(match);
            }
        }

        Item item = new Item(request.getName(), request.getPrice(), excluded);
        bill.getItems().add(item);
        return bill;
    }

    public Map<String, Double> split(String billId) {
        Bill bill = getBill(billId);
        Map<Member, Double> totals = billSplitter.split(bill);

        Map<String, Double> byName = new LinkedHashMap<>();
        for (Map.Entry<Member, Double> entry : totals.entrySet()) {
            byName.put(entry.getKey().getName(), entry.getValue());
        }
        return byName;
    }

    public BillSummaryResponse getSummary(List<String> billIds) {
        Map<String, Map<String, Double>> perBill = new LinkedHashMap<>();
        Map<String, Double> combined = new HashMap<>();

        for (String billId : billIds) {
            Map<String, Double> byName = split(billId); // already Map<String, Double> now

            perBill.put(billId, byName);
            for (Map.Entry<String, Double> entry : byName.entrySet()) {
                combined.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        return new BillSummaryResponse(perBill, combined);
    }
}