package com.shravanthi.split_bill.controller;

import com.shravanthi.split_bill.dto.AddItemRequest;
import com.shravanthi.split_bill.dto.BillSummaryResponse;
import com.shravanthi.split_bill.dto.BulkAddItemsRequest;
import com.shravanthi.split_bill.dto.CreateBillRequest;
import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.service.BillService;
import com.shravanthi.split_bill.service.OcrItemDraft;
import com.shravanthi.split_bill.service.OcrService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final OcrService ocrService;

    public BillController(BillService billService, OcrService ocrService) {
        this.billService = billService;
        this.ocrService = ocrService;
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
    public Bill getBill(@PathVariable Long id) {
        return billService.getBill(id);
    }

    @PostMapping("/{id}/items")
    public Bill addItem(@PathVariable Long id, @RequestBody AddItemRequest request) {
        return billService.addItem(id, request);
    }

    @PostMapping("/{id}/items/bulk")
    public Bill addItemsBulk(@PathVariable Long id, @RequestBody BulkAddItemsRequest request) {
        return billService.addItemsBulk(id, request.getItems());
    }

    @GetMapping("/{id}/split")
    public Map<String, Double> split(@PathVariable Long id) {
        return billService.split(id);
    }

    @GetMapping("/summary")
    public BillSummaryResponse getSummary(@RequestParam List<Long> ids) {
        return billService.getSummary(ids);
    }

    @PostMapping("/{id}/scan")
    public List<OcrItemDraft> scanBillImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        billService.getBill(id);
        String rawText = ocrService.extractText(file);
        return ocrService.extractItemDrafts(rawText);
    }
}