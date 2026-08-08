package com.shravanthi.split_bill.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Item;
import com.shravanthi.split_bill.model.Member;

public class BillSplitterTest {

    private final BillSplitter billSplitter = new BillSplitter();

    @Test
    void splitsItemEvenlyWithNoExclusion() {
        Member akash = new Member("Akash");
        Member priya = new Member("Priya");
        Member ravi = new Member("Ravi");

        Item pizza = new Item("Pizza", 30.0, new HashSet<>());

        Bill bill = new Bill(null, new ArrayList<>(List.of(pizza)), new ArrayList<>(List.of(akash, priya, ravi)));
        Map<Member, Double> totals = billSplitter.split(bill);

        assertEquals(10.0, totals.get(akash), 0.001);
        assertEquals(10.0, totals.get(priya), 0.001);
        assertEquals(10.0, totals.get(ravi), 0.001);

    }

    @Test
    void excludesMemberFromSpecificItem() {
        Member akash = new Member("Akash");
        Member priya = new Member("Priya");
        Member ravi = new Member("Ravi");

        Item pizza = new Item("Pizza", 30.0, new HashSet<>());
        Item iceCream = new Item("Ice Cream", 10.0, new HashSet<>(Set.of(akash)));

        Bill bill = new Bill(null, new ArrayList<>(List.of(pizza, iceCream)), new ArrayList<>(List.of(akash, priya, ravi)));

        Map<Member, Double> totals = billSplitter.split(bill);

        assertEquals(10.0, totals.get(akash), 0.001);
        assertEquals(15.0, totals.get(priya), 0.001);
        assertEquals(15.0, totals.get(ravi), 0.001);

    }

    @Test
    void throwsWhenAllMembersExcludedFromAnItem() {
        Member akash = new Member("Akash");
        Member priya = new Member("Priya");

        Item cake = new Item("Cake", 20.0, new HashSet<>(Set.of(akash, priya)));

        Bill bill = new Bill(null, new ArrayList<>(List.of(cake)), new ArrayList<>(List.of(akash, priya)));

        assertThrows(IllegalStateException.class, () -> billSplitter.split(bill));

    }

}
