package com.shravanthi.split_bill.service;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Item;
import com.shravanthi.split_bill.model.Member;
import com.shravanthi.split_bill.model.User;
import com.shravanthi.split_bill.repository.BillRepository;
import com.shravanthi.split_bill.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final BillSplitter billSplitter = new BillSplitter();

    public BillService(BillRepository billRepository, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Logged-in user not found"));
    }

    public Bill createBill(CreateBillRequest request, String username) {
        Bill bill = new Bill();
        bill.setName(request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : "Untitled Bill");
        bill.setOwner(resolveUser(username));

        List<Member> members = request.getMemberNames().stream()
                .map(name -> new Member(null, name))
                .collect(Collectors.toList());
        bill.setMembers(members);

        return billRepository.save(bill);
    }

    public Bill getBill(Long billId, String username) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new NoSuchElementException("No bill found with id " + billId));

        if (!bill.getOwner().getUsername().equals(username)) {
            throw new NoSuchElementException("No bill found with id " + billId);
        }

        return bill;
    }

    public List<Bill> getAllBills(String username) {
        return billRepository.findAll().stream()
                .filter(bill -> bill.getOwner() != null && bill.getOwner().getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public Bill addItem(Long billId, AddItemRequest request, String username) {
        Bill bill = getBill(billId, username);

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

    public Bill addItemsBulk(Long billId, List<AddItemRequest> items, String username) {
        for (AddItemRequest item : items) {
            addItem(billId, item, username);
        }
        return getBill(billId, username);
    }

    public Map<String, Double> split(Long billId, String username) {
        Bill bill = getBill(billId, username);
        Map<Member, Double> totals = billSplitter.split(bill);

        Map<String, Double> byName = new LinkedHashMap<>();
        for (Map.Entry<Member, Double> entry : totals.entrySet()) {
            byName.put(entry.getKey().getName(), entry.getValue());
        }
        return byName;
    }

    public BillSummaryResponse getSummary(List<Long> billIds, String username) {
        Map<String, Map<String, Double>> perBill = new LinkedHashMap<>();
        Map<String, Double> combined = new HashMap<>();

        for (Long billId : billIds) {
            Map<String, Double> byName = split(billId, username);

            perBill.put(billId.toString(), byName);
            for (Map.Entry<String, Double> entry : byName.entrySet()) {
                combined.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        return new BillSummaryResponse(perBill, combined);
    }
}