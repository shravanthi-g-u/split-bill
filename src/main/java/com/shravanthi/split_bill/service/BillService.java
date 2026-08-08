package com.shravanthi.split_bill.service;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Item;
import com.shravanthi.split_bill.model.Member;
import com.shravanthi.split_bill.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillSplitter billSplitter = new BillSplitter();

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill createBill(CreateBillRequest request) {
        Bill bill = new Bill();
        bill.setName(request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : "Untitled Bill");

        List<Member> members = request.getMemberNames().stream()
                .map(name -> new Member(null, name))
                .collect(Collectors.toList());
        bill.setMembers(members);

        return billRepository.save(bill);
    }

    public Bill getBill(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new NoSuchElementException("No bill found with id " + billId));
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Bill addItem(Long billId, AddItemRequest request) {
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

        Item item = new Item();
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setExcludedMembers(excluded);
        item.setBill(bill);

        bill.getItems().add(item);
        return billRepository.save(bill);
    }

    public Bill addItemsBulk(Long billId, List<AddItemRequest> items) {
        for (AddItemRequest item : items) {
            addItem(billId, item);
        }
        return getBill(billId);
    }

    public Map<String, Double> split(Long billId) {
        Bill bill = getBill(billId);
        Map<Member, Double> totals = billSplitter.split(bill);

        Map<String, Double> byName = new LinkedHashMap<>();
        for (Map.Entry<Member, Double> entry : totals.entrySet()) {
            byName.put(entry.getKey().getName(), entry.getValue());
        }
        return byName;
    }

    public BillSummaryResponse getSummary(List<Long> billIds) {
        Map<String, Map<String, Double>> perBill = new LinkedHashMap<>();
        Map<String, Double> combined = new HashMap<>();

        for (Long billId : billIds) {
            Map<String, Double> byName = split(billId);

            perBill.put(billId.toString(), byName);
            for (Map.Entry<String, Double> entry : byName.entrySet()) {
                combined.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        return new BillSummaryResponse(perBill, combined);
    }
}