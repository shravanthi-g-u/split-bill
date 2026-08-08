package com.shravanthi.split_bill.service;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.shravanthi.split_bill.model.Bill;
import com.shravanthi.split_bill.model.Item;
import com.shravanthi.split_bill.model.Member;


public class BillSplitter {

    public Map<Member, Double> split(Bill bill){
        Map<Member, Double> totals = new HashMap<>();
        for(Member member : bill.getMembers()){
            totals.put(member, 0.0);
        }

        for(Item item : bill.getItems()){
            Set<Member> participants = new HashSet<>(bill.getMembers());
            participants.removeAll(item.getExcludedMembers());

            if(participants.isEmpty()){
                throw new IllegalStateException("Item '" +item.getName() + "' has no participants to split among");

            }

            double share = item.getPrice() / participants.size();

            for(Member participant : participants){
                totals.merge(participant, share, Double::sum);
            }
        }
        return totals;
    }
    
}
