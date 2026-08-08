package com.shravanthi.split_bill.controller;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Member;
import com.shravanthi.split_bill.service.BillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public Bill createBill(@RequestBody CreateBillRequest request) {
        return billService.createBill(request);
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.getAllBills();
    }

    @GetMapping("/{id}")
    public Bill getBill(@PathVariable String id) {
        return billService.getBill(id);
    }

    @PostMapping("/{id}/items")
    public Bill addItem(@PathVariable String id, @RequestBody AddItemRequest request) {
        return billService.addItem(id, request);
    }

    @GetMapping("/{id}/split")
    public Map<String, Double> split(@PathVariable String id) {
        return billService.split(id);
    }

    @GetMapping("/summary")
    public BillSummaryResponse getSummary(@RequestParam List<String> ids) {
        return billService.getSummary(ids);
    }
}